/* $Id: NetworkClientManager.java,v 1.8 2005/04/15 07:06:37 quisar Exp $ */
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
package marauroa.client.net;

import java.net.*;
import java.util.*;
import java.io.*;

import marauroa.common.*;
import marauroa.common.net.*;

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

  private Map<Byte,PacketContainer> pendingPackets;
  private List<Message> processedMessages;

  /** Constructor that opens the socket on the marauroa_PORT and start the thread
   to recieve new messages from the network. */
  public NetworkClientManager(String host, int port) throws SocketException
    {
    Logger.trace("NetworkClientManager::NetworkClientManager",">");
    clientid=0;
    address=new InetSocketAddress(host,port);
    socket=new DatagramSocket();
    socket.setSoTimeout(TimeoutConf.SOCKET_TIMEOUT);
    socket.setTrafficClass(0x08|0x10);
    socket.setReceiveBufferSize(128*1024);

    msgFactory=MessageFactory.getFactory();
    pendingPackets=new LinkedHashMap<Byte,PacketContainer>();
    processedMessages=new LinkedList<Message>();
    Logger.trace("NetworkClientManager::NetworkClientManager","<");
    }

  public InetSocketAddress getAddress()
    {
    return address;
    }

  /** This method notify the thread to finish it execution */
  public void finish()
    {
    Logger.trace("NetworkClientManager::finish",">");
    socket.close();
    Logger.trace("NetworkClientManager::finish","<");
    }

  private Message getOldestProcessedMessage()
    {
    Message choosenMsg=((Message)processedMessages.get(0));
    int smallestTimestamp=choosenMsg.getMessageTimestamp();

    for(Message msg: processedMessages)
      {
      if(msg.getMessageTimestamp()<smallestTimestamp)
        {
        choosenMsg=msg;
        smallestTimestamp=msg.getMessageTimestamp();
        }
      }

    Logger.trace("NetworkClientManager::getOldestProcessedMessage","D",processedMessages.size()+" message available");
    Logger.trace("NetworkClientManager::getOldestProcessedMessage","D",choosenMsg.toString());

    processedMessages.remove(choosenMsg);
    return choosenMsg;
    }

  private void processPendingPackets() throws IOException, InvalidVersionException
    {
    for(Iterator<PacketContainer> it = pendingPackets.values().iterator(); it.hasNext();)
      {
      PacketContainer message=it.next();

      if(System.currentTimeMillis()-message.timestamp.getTime()>TimeoutConf.CLIENT_MESSAGE_DROPPED_TIMEOUT)
        {
        Logger.trace("NetworkClientManager::processPendingPackets","D","deleted incompleted message after timedout");
        it.remove();
        continue;
        }

      if(message.remaining==0)
        {
        // delete the message from queue to prevent loop if it is a bad message
        it.remove();

        Message msg=msgFactory.getMessage(message.content,message.address);

        Logger.trace("NetworkClientManager::processPendingPackets","D","receive message(type="+msg.getType()+") from "+msg.getClientID());
        Logger.trace("NetworkClientManager::processPendingPackets","D",msg.toString());
        if(msg.getType()==Message.MessageType.S2C_LOGIN_SENDNONCE)
          {
          clientid=msg.getClientID();
          }

        processedMessages.add(msg);
        // NOTE: Break??? Why not run all the array...
        break;
        }
      }
    }

  final static private int PACKET_SIGNATURE_SIZE=3;
  final static private int CONTENT_PACKET_SIZE=NetConst.UDP_PACKET_SIZE-PACKET_SIGNATURE_SIZE;

  private void storePacket(InetSocketAddress address, byte[] data)
    {
    /* A multipart message. We try to read the rest now.
     * We need to check on the list if the message exist and it exist we add this one. */
    byte total=data[0];
    byte position=data[1];
    byte signature=data[2];

    Logger.trace("NetworkClientManager::storePacket","D","receive"+(total>1?" multipart ":" ")+"message("+signature+"): "+(position+1)+" of "+total);
    if(!pendingPackets.containsKey(new Byte(signature)))
      {
      /** This is the first packet */
      PacketContainer message=new PacketContainer();

      message.signature=signature;
      message.remaining=(byte)(total-1);
      message.address=address;
      message.content=new byte[CONTENT_PACKET_SIZE*total];
      message.timestamp=new Date();
      System.arraycopy(data,PACKET_SIGNATURE_SIZE,message.content,CONTENT_PACKET_SIZE*position,data.length-3);

      pendingPackets.put(new Byte(signature),message);
      }
    else
      {
      PacketContainer message=(PacketContainer)pendingPackets.get(new Byte(signature));

      --message.remaining;
      if(message.remaining<0)
        {
        Logger.trace("NetworkClientManager::storePacket","D","ERROR: We confused the messages("+message.signature+")");
        }
      else
        {
        System.arraycopy(data,3,message.content,(NetConst.UDP_PACKET_SIZE-3)*position,data.length-3);
        }
      }
    }

  /** This method returns a message if it is available or null
   *  @return a Message*/
  public Message getMessage() throws InvalidVersionException
    {
    Logger.trace("NetworkClientManager::getMessage",">");
    try
      {
      if(processedMessages.size()>0)
        {
        return getOldestProcessedMessage();
        }

      processPendingPackets();
      }
    catch(InvalidVersionException e)
      {
      Logger.thrown("NetworkClientManager::getMessage","X",e);
      throw e;
      }
    catch(Exception e)
      {
      /* Report the exception */
      Logger.thrown("NetworkClientManager::getMessage","X",e);
      }

    try
      {
      byte[] buffer=new byte[NetConst.UDP_PACKET_SIZE];
      DatagramPacket packet=new DatagramPacket(buffer,buffer.length);
      int i=0;

      /** We want to avoid this to block the whole client recieving messages */
      while(i<TimeoutConf.CLIENT_NETWORK_NUM_READ)
        {
        ++i;
        socket.receive(packet);

        byte[] data=packet.getData();
        storePacket((InetSocketAddress)packet.getSocketAddress(),data);
        }
      }
    catch(java.net.SocketTimeoutException e)
      {
      /* We need the thread to check from time to time if user has requested an exit */
      }
    catch(IOException e)
      {
      /* Report the exception */
      Logger.thrown("NetworkClientManager::getMessage","X",e);
      }
    finally
      {
      Logger.trace("NetworkClientManager::getMessage","<");
      }

    return null;
    }

  /** This method add a message to be delivered to the client the message is pointed to.
   *  @param msg the message to ve delivered. */
  public synchronized void addMessage(Message msg)
    {
    Logger.trace("NetworkClientManager::addMessage",">");
    try
      {
      /* We enforce the remote endpoint */
      msg.setAddress(address);
      msg.setClientID(clientid);

      ByteArrayOutputStream out=new ByteArrayOutputStream();
      OutputSerializer s=new OutputSerializer(out);

      Logger.trace("NetworkClientManager::addMessage","D","send message("+msg.getType()+") from "+msg.getClientID());
      s.write(msg);

      byte[] buffer=out.toByteArray();
      DatagramPacket pkt=new DatagramPacket(buffer,buffer.length,msg.getAddress());

      socket.send(pkt);
      }
    catch(IOException e)
      {
      /* Report the exception */
      Logger.thrown("NetworkClientManager::addMessage","X",e);
      }
    finally
      {
      Logger.trace("NetworkClientManager::addMessage","<");
      }
    }
  }
