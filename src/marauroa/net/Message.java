/* $Id: Message.java,v 1.16 2004/07/07 10:07:21 arianne_rpg Exp $ */
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

import java.net.InetSocketAddress;
import java.io.*;

/** Message is a class to represent all the kind of messages that are possible
 *  to exist in marauroa.
 */
public class Message implements marauroa.net.Serializable
  {
  public final static byte CLIENTID_INVALID=-1;
  public final static byte TYPE_INVALID=-1;
  public final static byte TYPE_C2S_LOGIN=1;
  public final static byte TYPE_S2C_LOGIN_ACK=10;
  public final static byte TYPE_S2C_LOGIN_NACK=11;
  public final static byte TYPE_S2C_CHARACTERLIST=2;
  public final static byte TYPE_C2S_CHOOSECHARACTER=3;
  public final static byte TYPE_S2C_CHOOSECHARACTER_ACK=30;
  public final static byte TYPE_S2C_CHOOSECHARACTER_NACK=31;
  public final static byte TYPE_C2S_LOGOUT=4;
  public final static byte TYPE_S2C_LOGOUT_ACK=40;
  public final static byte TYPE_S2C_LOGOUT_NACK=41;
  public final static byte TYPE_C2S_ACTION=5;
  public final static byte TYPE_S2C_ACTION_ACK=50;
  public final static byte TYPE_S2C_PERCEPTION=6;
  public final static byte TYPE_C2S_PERCEPTION_ACK=61;
  public final static byte TYPE_S2C_MAP=62;
  public final static byte TYPE_C2S_OUTOFSYNC=63;  
  public final static byte TYPE_S2C_SERVERINFO=7;  
  public final static byte TYPE_S2C_INVALIDMESSAGE=8;
  
  protected byte type;
  protected int clientid;
  protected int timestampMessage;
  
  protected InetSocketAddress source;
  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message */
  public Message(InetSocketAddress source)
    {
    this.type=TYPE_INVALID;
    this.clientid=CLIENTID_INVALID;
    this.source=source;
    timestampMessage=(int)(System.currentTimeMillis());
    }

  /** Sets the TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message  */
  public void setAddress(InetSocketAddress source)
    {
    this.source=source;
    }
    
  /** Returns the TCP/IP address associatted with this message 
   *  @return the TCP/IP address associatted with this message*/  
  public InetSocketAddress getAddress()
    {
    return source;
    }

  /** Returns the type of the message
   *  @return the type of the message  */
  public byte getType()
    {
    return type;
    }
    
  /** Set the clientID so that we can identify the client to which the
   message is target, as only IP is easy to Fake
   @param clientid a int that reprents the client id. */    
  public void setClientID(int clientid)
    {
    this.clientid=clientid;
    }
  
  /** Returns the clientID of the Message.
   @return the ClientID */
  public int getClientID()
    {
    return clientid;
    }
    
  /** Returns the timestamp of the message. Usually milliseconds */
  public int getMessageTimestamp()
    {
    return timestampMessage;
    }

  /** Serialize the object into an ObjectOutput 
   *  @exception IOException if the serializations fails */
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    out.write(NetConst.NETWORK_PROTOCOL_VERSION);
    out.write(type);
    out.write(clientid);
    out.write(timestampMessage);
    }
    
  /** Serialize the object from an ObjectInput 
   *  @exception IOException if the serializations fails 
   *  @exception java.lang.ClassNotFoundException if the serialized class doesn't exist. */
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    if(in.readByte()!=NetConst.NETWORK_PROTOCOL_VERSION)
      {
      throw new IOException();
      }
      
    type=in.readByte();
    clientid=in.readInt();
    timestampMessage=in.readInt();
    }  
  }
