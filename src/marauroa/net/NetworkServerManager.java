/* $Id: NetworkServerManager.java,v 1.32 2004/11/26 23:10:12 root777 Exp $ */
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
import marauroa.game.*;

/** The NetworkServerManager is the active entity of the marauroa.net package,
 *  it is in charge of sending and recieving the packages from the network. */
public final class NetworkServerManager
  {
  /** The server socket from where we recieve the packets. */
  private DatagramSocket socket;
  /** While keepRunning is true, we keep recieving messages */
  private volatile boolean keepRunning;
  /** isFinished is true when the thread has really exited. */
  private boolean isfinished;
  /** A List of Message objects: List<Message> */
  private List<Message> messages;

  /** A queue for messages to send: List<Message> */
  private List<Message> messagesToSend;

  private MessageFactory msgFactory;
  private NetworkServerManagerRead readManager;
  private NetworkServerManagerWrite[] writeManager;
  private Statistics stats;
  private PacketValidater packetValidater;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
   to recieve new messages from the network. */
  public NetworkServerManager() throws SocketException
    {
    marauroad.trace("NetworkServerManager",">");
    try
      {
      /* init the packet validater (which can now only check if the address is banned)*/
      packetValidater = new PacketValidater();
      
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

      messagesToSend = new LinkedList<Message>();

      int writer_count = 5;
      writeManager = new NetworkServerManagerWrite[writer_count];
      for(int i = 0; i<writeManager.length; i++)
        {
        writeManager[i]=new NetworkServerManagerWrite();
        writeManager[i].setName("NetworkServerManagerWrite_"+(i+1));
        writeManager[i].start();
        }     
      } 
    finally
      {
      marauroad.trace("NetworkServerManager","<");
      }
    }
        
  /** This method notify the thread to finish it execution */
  public void finish()
    {
    marauroad.trace("NetworkServerManager::finish",">");
    keepRunning=false;
    while(isfinished==false)
      {
      Thread.yield();
      }

    //wait until writer threads are shutted down
    for(int i = 0; i<writeManager.length; i++)
      {
      try{writeManager[i].join();}catch(InterruptedException ie){}
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
   *  @param msg the message to be delivered. */
  public void addMessage(Message msg)
    {
    marauroad.trace("NetworkServerManager::addMessage",">");
    synchronized(messagesToSend)
    {
      messagesToSend.add(msg);
      //wake up waiting Writer-Threads
      messagesToSend.notifyAll();
    }
    //writeManager.write(msg);
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
          marauroad.trace("NetworkServerManagerRead::run","D","Received UDP Packet");
          
          /*** Statistics ***/
          synchronized(stats)
            {  
            stats.addBytesRecv(packet.getLength());
            stats.addMessageRecv();
            }
          
          if(!packetValidater.checkBanned(packet))
            {
            try
              {
              Message msg=msgFactory.getMessage(packet.getData(),(InetSocketAddress)packet.getSocketAddress());
              marauroad.trace("NetworkServerManagerRead::run","D","Received message: "+msg.toString());
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
            marauroad.trace("NetworkServerManagerRead::run","D","UDP Packet discarded - client("+packet+") is banned.");
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
  class NetworkServerManagerWrite extends Thread
    {
    private int last_signature;
    public NetworkServerManagerWrite()
      {
      last_signature=0;
      }

    public void run()
      {
      marauroad.trace("NetworkServerManagerWrite::run",">");
      while(keepRunning)
        {
        Message msg = null;
        synchronized(messagesToSend)
          {
          if(messagesToSend.size()==0)
            {
            //sleep max 1 second until some one wake me up...
            try{messagesToSend.wait(1000);}catch(InterruptedException ie){}
            }
          if(messagesToSend.size()>0)
            {
            msg = messagesToSend.remove(0);
            }
          }
          if(msg!=null)
            {
            write(msg);
            }
        }
      marauroad.trace("NetworkServerManagerWrite::run","<");
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
          synchronized(stats) //we can have many threads now...
            {
            stats.addBytesSend(buffer.length);
            stats.addMessageSend();
            }
          
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
        marauroad.thrown("NetworkServerManagerWrite::write","X",e);
        }
      finally
        {
        marauroad.trace("NetworkServerManagerWrite::write","<");
        }
      }
    }
  }
