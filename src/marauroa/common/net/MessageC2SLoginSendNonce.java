/* $Id: MessageC2SLoginSendNonce.java,v 1.1 2005/04/14 09:59:06 quisar Exp $ */
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
package marauroa.common.net;
  
import java.net.InetSocketAddress;
import java.io.*;
  
/** This message indicate the server that the client wants to login and send the
 *  needed info: username and password to login to server.
 *  @see marauroa.common.net.Message
 */
public class MessageC2SLoginSendNonce extends MessageSendByteArray
  {
  /** Constructor for allowing creation of an empty message */
  public MessageC2SLoginSendNonce()
    {
    super(MessageType.C2S_LOGIN_SENDNONCE);
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param hash The nonce to send to the client.
   */
  public MessageC2SLoginSendNonce(InetSocketAddress source,byte[] hash)
    {
    super(MessageType.C2S_LOGIN_SENDNONCE,source, hash);
    }  
  
  public String toString()
    {
    return "Message (C2S Login Send Nonce) from ("+source.getAddress().getHostAddress()+") CONTENTS: (hash:" + byteArrayToString() +")";
    }

  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    if(type!=MessageType.C2S_LOGIN_SENDNONCE)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }

  }
