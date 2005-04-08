/* $Id: NetworkServerManager.java,v 1.8 2005/04/08 11:38:28 arianne_rpg Exp $ */
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
package marauroa.server.net;

/***
 *** NOTE: I have again downgrade to 1.5 to disable multithread send that is
 ***   making CPU usage to reach 100% 
 */

import java.net.*;
import java.util.*;
import java.io.*;
import java.math.*;

import marauroa.common.*;
import marauroa.common.net.*;
import marauroa.server.game.*;


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
  private List<Message> messages;
  private MessageFactory msgFactory;
  private NetworkServerManagerRead readManager;
  private NetworkServerManagerWrite writeManager;
  private Statistics stats;
  private PacketValidator packetValidator;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
   to recieve new messages from the network. */
  public NetworkServerManager() throws SocketException
    {
    Logger.trace("NetworkServerManager",">");
    try
      {
      /* init the packet validater (which can now only check if the address is banned)*/
      packetValidator = new PacketValidator();
      
      /* Create the socket and set a timeout of 1 second */
      socket=new DatagramSocket(NetConst.marauroa_PORT);
      socket.setSoTimeout(1000);
      socket.setTrafficClass(0x08|0x10);
      socket.setSendBufferSize(1500*16);
      
      msgFactory=MessageFactory.getFactory();
      keepRunning=true;
      isfinished=false;
      /* Because we access the list from several places we create a synchronized list. */
      messages=Collections.synchronizedList(new LinkedList<Message>());
      stats=Statistics.getStatistics();
      readManager=new NetworkServerManagerRead();
      readManager.start();
      writeManager=new NetworkServerManagerWrite();
      }
    finally
      {
      Logger.trace("NetworkServerManager","<");
      }
    }
        
  /** This method notify the thread to finish it execution */
  public void finish()
    {
    Logger.trace("NetworkServerManager::finish",">");
    keepRunning=false;
    while(isfinished==false)
      {
      Thread.yield();
      }
      
    socket.close();
    Logger.trace("NetworkServerManager::finish","<");
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
    Logger.trace("NetworkServerManager::getMessage",">");
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
        Logger.trace("NetworkServerManager::getMessage","D","Message not available.");
        return null;
        }
      else
        {
        Logger.trace("NetworkServerManager::getMessage","D","Message returned.");
        return (Message)messages.remove(0);
        }
      }
    finally
      {
      Logger.trace("NetworkServerManager::getMessage","<");
      }
    }

  /** This method blocks until a message is available
   *  @return a Message*/
  public synchronized Message getMessage()
    {
    Logger.trace("NetworkServerManager::getMessage",">");
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
      Logger.trace("NetworkServerManager::getMessage","<");
      }
    }
    
  /** This method add a message to be delivered to the client the message is pointed to.
   *  @param msg the message to be delivered. */
  public void addMessage(Message msg)
    {
    Logger.trace("NetworkServerManager::addMessage",">");
    writeManager.write(msg);
    Logger.trace("NetworkServerManager::addMessage","<");
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
      Logger.trace("NetworkServerManagerRead::run",">");
      while(keepRunning)
        {
        byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
        DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
        
        try
          {
          socket.receive(packet);
          Logger.trace("NetworkServerManagerRead::run","D","Received UDP Packet");
          
          /*** Statistics ***/
          stats.addBytesRecv(packet.getLength());
          stats.addMessageRecv();
          
          if(!packetValidator.checkBanned(packet))
            {
            try
              {
              Message msg=msgFactory.getMessage(packet.getData(),(InetSocketAddress)packet.getSocketAddress());
              Logger.trace("NetworkServerManagerRead::run","D","Received message: "+msg.toString());
              messages.add(msg);
              newMessageArrived();
              }
            catch(InvalidVersionException e)
              {
              MessageS2CInvalidMessage msg=new MessageS2CInvalidMessage((InetSocketAddress)packet.getSocketAddress(),"Invalid client version: Update client");
              addMessage(msg);
              }
            }
          else
            {
            Logger.trace("NetworkServerManagerRead::run","D","UDP Packet discarded - client("+packet+") is banned.");
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
          Logger.trace("NetworkServerManagerRead::run","X",e.getMessage());
          }
        }
        
      isfinished=true;
      Logger.trace("NetworkServerManagerRead::run","<");
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
    
    private byte[] serializeMessage(Message msg) throws IOException    
      {
      ByteArrayOutputStream out=new ByteArrayOutputStream();
      OutputSerializer s=new OutputSerializer(out);

      s.write(msg);
      return out.toByteArray();
      }
    
    final private int PACKET_SIGNATURE_SIZE=3;
    final private int CONTENT_PACKET_SIZE=NetConst.UDP_PACKET_SIZE-PACKET_SIGNATURE_SIZE;
      
    /** Method that execute the writting */
    public void write(Message msg)
      {
      Logger.trace("NetworkServerManagerWrite::write",">");
      try
        {
        /* TODO: Looks like hardcoded, write it in a better way */
        if(keepRunning)
          {
          byte[] buffer=serializeMessage(msg);
          int used_signature;
  
          /*** Statistics ***/
          used_signature=++last_signature;

          stats.addBytesSend(buffer.length);
          stats.addMessageSend();
          
          Logger.trace("NetworkServerManagerWrite::write","D","Message size in bytes: "+buffer.length);
          int totalNumberOfPackets=(buffer.length/CONTENT_PACKET_SIZE)+1;
          int bytesRemaining=buffer.length;
          
          byte[] data=new byte[CONTENT_PACKET_SIZE+PACKET_SIGNATURE_SIZE];
          
          for(int i=0;i<totalNumberOfPackets;++i)
            {
            int packetSize=CONTENT_PACKET_SIZE;

            if((CONTENT_PACKET_SIZE)>bytesRemaining)
              {
              packetSize=bytesRemaining;
              }

            bytesRemaining-=packetSize;
            
            Logger.trace("NetworkServerManagerWrite::write","D","Packet size: "+packetSize);
            Logger.trace("NetworkServerManagerWrite::write","D","Bytes remaining: "+bytesRemaining);
              
            data[0]=(byte)totalNumberOfPackets;
            data[1]=(byte)i;
            data[2]=(byte)used_signature;
            
            System.arraycopy(buffer,CONTENT_PACKET_SIZE*i,data,PACKET_SIGNATURE_SIZE,packetSize);

            DatagramPacket pkt=new DatagramPacket(data,packetSize+PACKET_SIGNATURE_SIZE,msg.getAddress());

            socket.send(pkt);
            Logger.trace("NetworkServerManagerWrite::write","D","Sent packet "+(i+1)+" of "+totalNumberOfPackets);
            }
          
          if(Logger.loggable("NetworkServerManagerWrite::write","D"))
            {
            Logger.trace("NetworkServerManagerWrite::write","D","Sent message: "+msg.toString());
            }
          }
        }
      catch(IOException e)
        {
        /* Report the exception */
        Logger.thrown("NetworkServerManagerWrite::write","X",e);
        }
      finally
        {
        Logger.trace("NetworkServerManagerWrite::write","<");
        }
      }
    }
  }
