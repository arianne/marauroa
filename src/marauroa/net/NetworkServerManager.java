/* $Id: NetworkServerManager.java,v 1.23 2004/05/27 22:44:11 root777 Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.net;

import java.net.*;
import java.util.*;
import java.io.*;
import java.math.*;
import marauroa.*;

/** The NetworkServerManager is the active entity of the marauroa.net package,
 *  it is in charge of sending and recieving the packages from the network. */
public final class NetworkServerManager
  {
  /** The server socket from where we recieve the packets. */
  private DatagramSocket socket;
  /** While keepRunning is true, we keep recieving messages */
  private boolean keepRunning;
  /** isFinished is true when the thread has really exited. */
  private boolean isfinished;
  /** A List of Message objects: List<Message> */
  private List messages;
  private List banList;
  private MessageFactory msgFactory;
  private NetworkServerManagerRead readManager;
  private NetworkServerManagerWrite writeManager;
  private Statistics stats;
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
   to recieve new messages from the network. */
  public NetworkServerManager() throws SocketException
    {
    marauroad.trace("NetworkServerManager",">");
    try
      {
      
      /* read ban list from configuration */
      initBanList();
      
      /* Create the socket and set a timeout of 1 second */
      socket=new DatagramSocket(NetConst.marauroa_PORT);
      socket.setSoTimeout(1000);
      socket.setTrafficClass(0x08|0x10);
      socket.setSendBufferSize(1500*16);
      
      msgFactory=MessageFactory.getFactory();
      keepRunning=true;
      isfinished=false;
      /* Because we access the list from several places we create a synchronized list. */
      messages=Collections.synchronizedList(new LinkedList());
      stats=Statistics.getStatistics();
      readManager=new NetworkServerManagerRead();
      readManager.start();
      writeManager=new NetworkServerManagerWrite();
      }
    finally
      {
      marauroad.trace("NetworkServerManager","<");
      }
    }
  /** This method adds a new netmask which is banned */
  public InetAddressMask addBan(InetAddressMask bannedMask)
    {
      if(banList==null)
      {
        banList=new ArrayList();
      }
      banList.add(bannedMask);
      return(bannedMask);
    }
    
  /** This method notify the thread to finish it execution */
  public void finish()
    {
    marauroad.trace("NetworkServerManager::finish",">");
    keepRunning=false;
    while(isfinished==false)
      {
      try
        {
        Thread.sleep(1000);
        }
      catch(java.lang.InterruptedException e)
        {
        }
      }
    socket.close();
    marauroad.trace("NetworkServerManager::finish","<");
    }
    
  private synchronized void newMessageArrived()
    {
    notifyAll();
    }
 
  /** This method returns a Message from the list or block for timeout milliseconds
   *  until a message is available or null if timeout happens.
   *  @param timeout timeout time in milliseconds
   *  @return a Message or null if timeout happens */
  public synchronized Message getMessage(int timeout)
    {
    marauroad.trace("NetworkServerManager::getMessage",">");
    try
      {
      if(messages.size()==0)
        {
        try
          {
          wait(timeout);
          }
        catch(InterruptedException e)
          {
          }
        }
      if(messages.size()==0)
        {
        marauroad.trace("NetworkServerManager::getMessage","D","Message not available.");
        return null;
        }
      else
        {
        marauroad.trace("NetworkServerManager::getMessage","D","Message returned.");
        return (Message)messages.remove(0);
        }
      }
    finally
      {
      marauroad.trace("NetworkServerManager::getMessage","<");
      }
    }

  /** This method blocks until a message is available
   *  @return a Message*/
  public synchronized Message getMessage()
    {
    marauroad.trace("NetworkServerManager::getMessage",">");
    try
      {
      while(messages.size()==0)
        {
        try
          {
          wait();
          }
        catch(InterruptedException e)
          {
          }
        }
      return (Message)messages.remove(0);
      }
    finally
      {
      marauroad.trace("NetworkServerManager::getMessage","<");
      }
    }
    
  /** This method add a message to be delivered to the client the message is pointed to.
   *  @param msg the message to ve delivered. */
  public synchronized void addMessage(Message msg)
    {
    marauroad.trace("NetworkServerManager::addMessage",">");
    writeManager.write(msg);
    marauroad.trace("NetworkServerManager::addMessage","<");
    }
  /** The active thread in charge of recieving messages from the network. */
  class NetworkServerManagerRead extends Thread
    {
    public NetworkServerManagerRead()
      {
      super("NetworkServerManagerRead");
      }
    
    /** Method that execute the reading. It runs as a active thread forever. */
    public void run()
      {
      marauroad.trace("NetworkServerManagerRead::run",">");
      while(keepRunning)
        {
        byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
        DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
        
        try
          {
          socket.receive(packet);
	  boolean banned = checkBanned(packet);
          marauroad.trace("NetworkServerManagerRead::run","D","Received UDP Packet");	  	  
          /*** Statistics ***/
          stats.addBytesRecv(packet.getLength());
          stats.addMessageRecv();
          if(!banned)
	    {
            try
              {
              Message msg=msgFactory.getMessage(packet.getData(),(InetSocketAddress)packet.getSocketAddress());
              marauroad.trace("NetworkServerManagerRead::run","D","Received message: "+msg.toString());
              messages.add(msg);
              newMessageArrived();
              }
            catch(MessageFactory.InvalidVersionException e)
              {
              MessageS2CInvalidMessage msg=new MessageS2CInvalidMessage((InetSocketAddress)packet.getSocketAddress(),"Invalid client version: Update client");
              addMessage(msg);
              }
	    }
	    else
	    {
	      marauroad.trace("NetworkServerManagerRead::run","D","UDP Packet discarded - client is banned.");
	    }
          }
        catch(java.net.SocketTimeoutException e)
          {
          /* We need the thread to check from time to time if user has requested
           * an exit */
          }
        catch(IOException e)
          {
          stats.addMessageIncorrect();
          /* Report the exception */
          marauroad.trace("NetworkServerManagerRead::run","X",e.getMessage());
          }
        }
      isfinished=true;
      marauroad.trace("NetworkServerManagerRead::run","<");
      }
    }
    

  /** A wrapper class for sending messages to clients */
  class NetworkServerManagerWrite
    {
    private int last_signature;
    public NetworkServerManagerWrite()
      {
      last_signature=0;
      }
    
    /** Method that execute the writting */
    public void write(Message msg)
      {
      marauroad.trace("NetworkServerManagerWrite::write",">");
      try
        {
        /* TODO: Looks like hardcoded, write it in a better way */
        if(keepRunning)
          {
          ByteArrayOutputStream out=new ByteArrayOutputStream();
          OutputSerializer s=new OutputSerializer(out);     

          s.write(msg);

          byte[] buffer=out.toByteArray();
  
          /*** Statistics ***/
          stats.addBytesSend(buffer.length);
          stats.addMessageSend();
          marauroad.trace("NetworkServerManagerWrite::write","D","Message size in bytes: "+buffer.length);

          int total=buffer.length/(NetConst.UDP_PACKET_SIZE-3)+1;

          ++last_signature;

          int remaining=buffer.length;
		  
          for(int i=0;i<total;++i)
            {
            int size=0;

            if((NetConst.UDP_PACKET_SIZE-3)>remaining)
              {
              size=remaining;
              }
            else
              {
              size=NetConst.UDP_PACKET_SIZE-3;
              }
            remaining-=size;
            marauroad.trace("NetworkServerManagerWrite::write","D","Packet size: "+size);
            marauroad.trace("NetworkServerManagerWrite::write","D","Bytes remaining: "+remaining);
		      
            byte[] data=new byte[size+3];

            data[0]=(byte)total;
            data[1]=(byte)i;
            data[2]=(byte)last_signature;
            System.arraycopy(buffer,(NetConst.UDP_PACKET_SIZE-3)*i,data,3,size);

            DatagramPacket pkt=new DatagramPacket(data,data.length,msg.getAddress());

            socket.send(pkt);
            marauroad.trace("NetworkServerManagerWrite::write","D","Sent packet "+(i+1)+" of "+total);
            }
          
          if(marauroad.loggable("NetworkServerManagerWrite::write","D"))
            {
            marauroad.trace("NetworkServerManagerWrite::write","D","Sent message: "+msg.toString());
            }
          }
        }
      catch(IOException e)
        {
        /* Report the exception */
        marauroad.trace("NetworkServerManagerWrite::write","X",e.getMessage());
        }
      finally
        {
        marauroad.trace("NetworkServerManagerWrite::write","<");
        }
      }
    }
    
    /** reads ban list from configuration 
    * the ban list looks like: 192.168.100.100/255.255.255.0;192.168.90.100/255.255.255.0
    */
    private void initBanList()
    {
      marauroad.trace("NetworkServerManager::initBanList",">");
      try
      {
        Configuration conf = Configuration.getConfiguration();
        String str_banlist = conf.get("banlist");
	if(str_banlist!=null&&!"".equals(str_banlist))
	{
	  String bans[] = str_banlist.split(";");	
	  banList = new ArrayList(bans.length);
	  for(int i=0; i<bans.length; i++)
	  {
	    String address_mask_pair [] = bans[i].split("\\/");
	    InetAddressMask iam = null;
	    if(address_mask_pair.length==2)
	    {
	    
	      iam = new InetAddressMask(address_mask_pair[0].trim(),address_mask_pair[1].trim());
	    }
	    else if (address_mask_pair.length==1)
	    {
	      //single host
	      iam = new InetAddressMask(address_mask_pair[0].trim(),"255.255.255.255");
	    }
	    else
	    {
	      marauroad.trace("NetworkServerManager::initBanList","D","Ignoring invalid entry "+bans[i]);
	    }
	    if(iam!=null)
	    {
	      marauroad.trace("NetworkServerManager::initBanList","D","Adding new ban entry: "+iam);
	      banList.add(iam);
	    }
	  }
	}
      }
      catch(Exception e)
      {
        marauroad.trace("NetworkServerManager::initBanList","X","Error initializing ban list :"+e.getMessage());      
      }
      finally
      {
        marauroad.trace("NetworkServerManager::initBanList","<");
      }
    }
    
    /** returns true if the source ip is banned */
    private boolean checkBanned(DatagramPacket packet)
    {
      boolean banned = false;
      try
      {
        marauroad.trace("NetworkServerManager::checkBanned",">");
        for(int i=0; !banned && banList!=null && i<banList.size() ; i++)
        {
          banned = ((InetAddressMask)banList.get(i)).matches(packet.getAddress());
	  if(banned)
	  {
	    String msg = "packet from "+ packet.getAddress()+" is banned by "+banList.get(i);
	    marauroad.trace("NetworkServerManager::checkBanned","D",msg);
	  }
        }
      }
      finally
      {
        marauroad.trace("NetworkServerManager::checkBanned","<");
      }
      return(banned);
    }
  }
