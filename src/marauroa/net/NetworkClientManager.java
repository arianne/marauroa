/* $Id: NetworkClientManager.java,v 1.17 2004/04/30 12:24:59 arianne_rpg Exp $ */
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
import marauroa.*;

/** The NetworkClientManager is in charge of sending and recieving the packages
 *  from the network. */
public class NetworkClientManager
  {
  private DatagramSocket socket;
  private InetSocketAddress address;
  private int clientid;
  private MessageFactory msgFactory;
  
  static private class PacketContainer
    {
    public byte signature;
    public byte remaining;
    public byte[] content;
    public InetSocketAddress address;
    public Date timestamp;
    }
    
  private Map pendingPackets;
  private List processedMessages;
  
  /** Constructor that opens the socket on the marauroa_PORT and start the thread
   to recieve new messages from the network. */
  public NetworkClientManager(String host) throws SocketException
    {
    clientid=0;
    address=new InetSocketAddress(host,NetConst.marauroa_PORT);
    socket=new DatagramSocket();
    socket.setSoTimeout(TimeoutConf.SOCKET_TIMEOUT);
    socket.setTrafficClass(0x08|0x10);
      
    msgFactory=MessageFactory.getFactory();
    pendingPackets=new LinkedHashMap();
    processedMessages=new LinkedList();
    }
    
  /** This method notify the thread to finish it execution */
  public void finish()
    {
    socket.close();
    }
    
  /** This method returns a message if it is available or null
   *  @return a Message*/
  public Message getMessage() throws MessageFactory.InvalidVersionException
    {
    try
      {
      if(processedMessages.size()>0)
        {
        Message choosenMsg=((Message)processedMessages.get(0));
        int smallestTimestamp=choosenMsg.getMessageTimestamp();

        Iterator messages=processedMessages.iterator();
        while(messages.hasNext())
          {
          Message msg=(Message)messages.next();
          if(msg.getMessageTimestamp()<smallestTimestamp)
            {
            choosenMsg=msg;
            smallestTimestamp=msg.getMessageTimestamp();
            }
          }
        
        processedMessages.remove(choosenMsg);      
        return choosenMsg;
        }
        
      Iterator it=pendingPackets.entrySet().iterator();

      while(it.hasNext())
        {
        Map.Entry entry=(Map.Entry)it.next();
        PacketContainer message=(PacketContainer)entry.getValue();

        if(new Date().getTime()-message.timestamp.getTime()>TimeoutConf.CLIENT_MESSAGE_DROPPED_TIMEOUT)
          {
          marauroad.trace("NetworkClientManager::getMessage","D","deleted incompleted message after timedout");
          pendingPackets.remove(new Byte(message.signature));
          it = pendingPackets.entrySet().iterator();
          }

        if(message.remaining==0)
          {
          // delete the message from queue to prevent loop if it is a bad message
          pendingPackets.remove(new Byte(message.signature));

          Message msg=msgFactory.getMessage(message.content,message.address);

          marauroad.trace("NetworkClientManager::getMessage","D","receive message(type="+msg.getType()+") from "+msg.getClientID());
          if(msg.getType()==Message.TYPE_S2C_LOGIN_ACK)
            {
            clientid=msg.getClientID();
            }
            
          processedMessages.add(msg);
          break;
          }
        }
      }
    catch(MessageFactory.InvalidVersionException e)
      {
      e.printStackTrace();
      marauroad.trace("NetworkClientManager::getMessage","X",e.getMessage());
      throw e;
      }
    catch(Exception e)
      {
      /* Report the exception */
      e.printStackTrace();
      marauroad.trace("NetworkClientManager::getMessage","X",e.getMessage());
      // delete the bad message from queue
      return null;
      }
        
    byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
    DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
    int i=0;
        
    try
      {
      /** We want to avoid this to block the whole client recieving messages */
      while(i<TimeoutConf.CLIENT_NETWORK_NUM_READ)
        {
        ++i;
        socket.receive(packet);

        byte[] data=packet.getData();
        /* A multipart message. We try to read the rest now.
         * We need to check on the list if the message exist and it exist we add this one. */
        /* TODO: Looks like hardcoded, write it in a better way */
        byte total=data[0];
        byte position=data[1];
        byte signature=data[2];

        marauroad.trace("NetworkClientManager::getMessage","D","receive"+(total>1?" multipart ":" ")+"message("+signature+"): "+(position+1)+" of "+total);
        if(!pendingPackets.containsKey(new Byte(signature)))
          {
          /** This is the first packet */
          PacketContainer message=new PacketContainer();

          message.signature=signature;
          message.remaining=(byte)(total-1);
          message.address=(InetSocketAddress)packet.getSocketAddress();
          message.content=new byte[(NetConst.UDP_PACKET_SIZE-3)*total];
          message.timestamp=new Date();
          System.arraycopy(data,3,message.content,(NetConst.UDP_PACKET_SIZE-3)*position,data.length-3);
          pendingPackets.put(new Byte(signature),message);
          }
        else
          {
          PacketContainer message=(PacketContainer)pendingPackets.get(new Byte(signature));

          --message.remaining;
          if(message.remaining<0)
            {
            marauroad.trace("NetworkClientManager::getMessage","D","ERROR: We confused the messages("+message.signature+")");
            return null;
            }
          System.arraycopy(data,3,message.content,(NetConst.UDP_PACKET_SIZE-3)*position,data.length-3);
          }
        }
      return null;
      }
    catch(java.net.SocketTimeoutException e)
      {
      /* We need the thread to check from time to time if user has requested an exit */
      return null;
      }
    catch(IOException e)
      {
      /* Report the exception */
      e.printStackTrace();
      marauroad.trace("NetworkClientManager::getMessage","X",e.getMessage());
      return null;
      }
    }
    
  /** This method add a message to be delivered to the client the message is pointed to.
   *  @param msg the message to ve delivered. */
  public synchronized void addMessage(Message msg)
    {
    try
      {
      /* We enforce the remote endpoint */
      msg.setAddress(address);
      msg.setClientID(clientid);
            
      ByteArrayOutputStream out=new ByteArrayOutputStream();
      OutputSerializer s=new OutputSerializer(out);
            
      marauroad.trace("NetworkClientManager::addMessage","D","send message("+msg.getType()+") from "+msg.getClientID());
      s.write(msg);
            
      byte[] buffer=out.toByteArray();
      DatagramPacket pkt=new DatagramPacket(buffer,buffer.length,msg.getAddress());
            
      socket.send(pkt);
      }
    catch(IOException e)
      {
      /* Report the exception */
      marauroad.report(e.getMessage());
      }
    }
  }
