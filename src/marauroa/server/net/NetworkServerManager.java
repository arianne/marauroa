/* $Id: NetworkServerManager.java,v 1.15 2006/01/26 18:59:48 arianne_rpg Exp $ */
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import marauroa.common.Log4J;
import marauroa.common.CRC;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.Message;
import marauroa.common.net.MessageFactory;
import marauroa.common.net.MessageS2CInvalidMessage;
import marauroa.common.net.NetConst;
import marauroa.common.net.OutputSerializer;
import marauroa.server.game.Statistics;
import org.apache.log4j.Logger;


/** The NetworkServerManager is the active entity of the marauroa.net package,
 *  it is in charge of sending and recieving the packages from the network. */
public final class NetworkServerManager
  {
  /** the logger instance. */
  private static final Logger logger = Log4J.getLogger(NetworkServerManager.class);
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
    Log4J.startMethod(logger, "NetworkServerManager");
    /* init the packet validater (which can now only check if the address is banned)*/
    packetValidator = new PacketValidator();

    /* Create the socket and set a timeout of 1 second */
    socket=new DatagramSocket(NetConst.marauroa_PORT);
    socket.setSoTimeout(1000);
    socket.setTrafficClass(0x08|0x10);
    socket.setSendBufferSize(1500*64);

    msgFactory=MessageFactory.getFactory();
    keepRunning=true;
    isfinished=false;
    /* Because we access the list from several places we create a synchronized list. */
    messages=Collections.synchronizedList(new LinkedList<Message>());
    stats=Statistics.getStatistics();
    readManager=new NetworkServerManagerRead();
    readManager.start();
    writeManager=new NetworkServerManagerWrite();
    logger.debug("NetworkServerManager started successfully");
    }
        
  /** This method notify the thread to finish it execution */
  public void finish()
    {
    logger.debug("shutting down NetworkServerManager");
    keepRunning=false;
    while(isfinished==false)
      {
      Thread.yield();
      }
      
    socket.close();
    logger.debug("NetworkServerManager is down");
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
    Log4J.startMethod(logger, "getMessage");
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

    Message message;
    if(messages.size()==0)
      {
      logger.debug("Message not available.");
      message = null;
      }
    else
      {
      logger.debug("Message returned.");
      message = (Message)messages.remove(0);
      }
    Log4J.finishMethod(logger, "getMessage");
    return message;
    }

  /** This method blocks until a message is available
   *  @return a Message*/
  public synchronized Message getMessage()
    {
    Log4J.startMethod(logger, "getMessage[blocking]");
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

    Log4J.finishMethod(logger, "getMessage[blocking]");
    return (Message)messages.remove(0);
    }
    
  /** This method add a message to be delivered to the client the message is pointed to.
   *  @param msg the message to be delivered. */
  public void addMessage(Message msg)
    {
    Log4J.startMethod(logger, "addMessage");
    writeManager.write(msg);
    Log4J.finishMethod(logger, "addMessage");
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
      logger.debug("run()");
      while(keepRunning)
        {
        byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
        DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
        
        try
          {
          socket.receive(packet);
          logger.debug("Received UDP Packet");
          
          /*** Statistics ***/
          stats.add("Bytes recv",packet.getLength());
          stats.add("Message recv",1);
          
          if(!packetValidator.checkBanned(packet))
            {
            try
              {
              Message msg=msgFactory.getMessage(packet.getData(),(InetSocketAddress)packet.getSocketAddress());
              logger.debug("Received message: "+msg.toString());
              messages.add(msg);
              newMessageArrived();
              }
            catch(InvalidVersionException e)
              {
              stats.add("Message invalid version",1);
              MessageS2CInvalidMessage msg=new MessageS2CInvalidMessage((InetSocketAddress)packet.getSocketAddress(),"Invalid client version: Update client");
              addMessage(msg);
              }
            }
          else
            {
            logger.debug("UDP Packet discarded - client("+packet+") is banned.");
            }
          }
        catch(java.net.SocketTimeoutException e)
          {
          /* We need the thread to check from time to time if user has requested
           * an exit */
          }
        catch(IOException e)
          {
          /* Report the exception */
          logger.error("error while processing udp-packets",e);
          }
        }
        
      isfinished=true;
      logger.debug("run() finished");
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
    
    final private int PACKET_SIGNATURE_SIZE=4;
    final private int CONTENT_PACKET_SIZE=NetConst.UDP_PACKET_SIZE-PACKET_SIGNATURE_SIZE;
      
    /** Method that execute the writting */
    public void write(Message msg)
      {
      Log4J.startMethod(logger, "write");
      try
        {
        /* TODO: Looks like hardcoded, write it in a better way */
        if(keepRunning)
          {
          byte[] buffer=serializeMessage(msg);
          short used_signature;
  
          /*** Statistics ***/
          used_signature=CRC.cmpCRC(buffer); //++last_signature;

          stats.add("Bytes send",buffer.length);
          stats.add("Message send",1);
          
          logger.debug("Message("+msg.getType()+") size in bytes: "+buffer.length);
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
            
            logger.debug("Packet size: "+packetSize);
            logger.debug("Bytes remaining: "+bytesRemaining);
              
            data[0]=(byte)totalNumberOfPackets;
            data[1]=(byte)i;
            data[2]=(byte)(used_signature&255);
            data[3]=(byte)((used_signature>>8)&255);
            
            System.arraycopy(buffer,CONTENT_PACKET_SIZE*i,data,PACKET_SIGNATURE_SIZE,packetSize);

            DatagramPacket pkt=new DatagramPacket(data,packetSize+PACKET_SIGNATURE_SIZE,msg.getAddress());

            socket.send(pkt);
            logger.debug("Sent packet("+used_signature+") "+(i+1)+" of "+totalNumberOfPackets);
            }
          
          if(logger.isDebugEnabled())
            {
            logger.debug("Sent message: "+msg);
            }
          }
        Log4J.finishMethod(logger, "write");
        }
      catch(IOException e)
        {
        /* Report the exception */
        logger.error("error while sending a packet (msg=("+msg+"))",e);
        }
      }
    }
  }
