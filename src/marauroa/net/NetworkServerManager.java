/* $Id: NetworkServerManager.java,v 1.7 2003/12/08 01:08:30 arianne_rpg Exp $ */
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

import marauroa.marauroad;

/** The NetworkServerManager is the active entity of the marauroa.net package,
 *  it is in charge of sending and recieving the packages from the network. */
public class NetworkServerManager
  {
  /** The server socket from where we recieve the packets. */
  private DatagramSocket socket;
  /** While keepRunning is true, we keep recieving messages */
  private boolean keepRunning;  
  /** isFinished is true when the thread has really exited. */
  private boolean isfinished;
  
  /** A List of Message objects: List<Message> */
  private List messages;    
  
  private MessageFactory msgFactory;  
  private NetworkServerManagerRead readManager;
  private NetworkServerManagerWrite writeManager;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
      to recieve new messages from the network. */
  public NetworkServerManager() throws SocketException
    {    
    marauroad.trace("NetworkServerManager",">");
    try
      {
      /* Create the socket and set a timeout of 1 second */
      socket=new DatagramSocket(NetConst.marauroa_PORT);
      socket.setSoTimeout(1000);
       
      msgFactory=MessageFactory.getFactory();
    
      keepRunning=true;
      isfinished=false;

	  /* Because we access the list from several places we create a synchronized list. */    
      messages=Collections.synchronizedList(new LinkedList());
    
      readManager=new NetworkServerManagerRead();
      readManager.start();
    
      writeManager=new NetworkServerManagerWrite();
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
    notify();
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
          marauroad.trace("NetworkServerManagerRead::run","D","Received UDP Packet");

          Message msg=msgFactory.getMessage(packet.getData(),(InetSocketAddress)packet.getSocketAddress());
          marauroad.trace("NetworkServerManagerRead::run","D","Received message: "+msg.toString());
         
          messages.add(msg);
          newMessageArrived();
          }
        catch(java.net.SocketTimeoutException e)
          {
          /* We need the thread to check from time to time if user has requested
           * an exit */
          }
        catch(IOException e)
          {
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
    private String name;
    private Random rand;
    
    public NetworkServerManagerWrite()
      {
      name="NetworkServerManagerWrite";
      rand=new Random();
      rand.setSeed(new Date().getTime());
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
 	      marauroad.trace("NetworkServerManagerWrite::write","D","Message size in bytes: "+buffer.length);

		  int total=buffer.length/(NetConst.UDP_PACKET_SIZE-3)+1;
		  byte signature=(byte)rand.nextInt();
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
		    data[2]=signature;
		    System.arraycopy(buffer,(NetConst.UDP_PACKET_SIZE-3)*i,data,3,size);

   	        DatagramPacket pkt=new DatagramPacket(data,data.length,msg.getAddress()); 	     
            socket.send(pkt);
            marauroad.trace("NetworkServerManagerWrite::write","D","Sent packet "+(i+1)+" of "+total);
		    }
		    
          marauroad.trace("NetworkServerManagerWrite::write","D","Sent message: "+msg.toString());
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
  }