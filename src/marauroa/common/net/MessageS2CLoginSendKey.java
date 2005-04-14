/* $Id: MessageS2CLoginSendKey.java,v 1.1 2005/04/14 09:59:07 quisar Exp $ */
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
import java.math.BigInteger;
import marauroa.common.crypto.*;
  
/** This message indicate the server that the client wants to login and send the
 *  needed info: username and password to login to server.
 *  @see marauroa.common.net.Message
 */
public class MessageS2CLoginSendKey extends Message
  {
  private RSAPublicKey key;
  /** Constructor for allowing creation of an empty message */
  public MessageS2CLoginSendKey()
    {
    super(MessageType.S2C_LOGIN_SENDKEY,null);
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param username the username of the user that wants to login
   *  @param password the plain password of the user that wants to login
   */
  public MessageS2CLoginSendKey(InetSocketAddress source,RSAPublicKey key)
    {
    super(MessageType.S2C_LOGIN_SENDKEY,source);
    this.key = key;
    }  
  
  /** This method returns the username
   *  @return the username */
  public RSAPublicKey getKey()
    {
    return key;
    }
    
  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Login Send key) from ("+source.getAddress().getHostAddress()+") CONTENTS: (n:" + key.getN() + "\te:" + key.getE() + ")";
    }
      
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(key.getN().toByteArray());
    out.write(key.getE().toByteArray());
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    BigInteger n = new BigInteger(in.readByteArray());
    BigInteger e = new BigInteger(in.readByteArray());
    key = new RSAPublicKey(n,e);
    if(type!=MessageType.S2C_LOGIN_SENDKEY)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }
