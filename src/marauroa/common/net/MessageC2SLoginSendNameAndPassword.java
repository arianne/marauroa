/* $Id: MessageC2SLoginSendNameAndPassword.java,v 1.1 2005/04/14 09:59:06 quisar Exp $ */
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
public class MessageC2SLoginSendNameAndPassword extends MessageSendByteArray
  {
  private String username;
  /** Constructor for allowing creation of an empty message */
  public MessageC2SLoginSendNameAndPassword()
    {
    super(MessageType.C2S_LOGIN_SENDNAMEANDPASSWORD);
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param username the username of the user that wants to login
   *  @param password the plain password of the user that wants to login
   */
  public MessageC2SLoginSendNameAndPassword(InetSocketAddress source,String username, byte[] password)
    {
    super(MessageType.C2S_LOGIN_SENDNAMEANDPASSWORD,source,password);
    this.username=username;
    }  
  
  /** This method returns the username
   *  @return the username */
  public String getUsername()
    {
    return username;    
    }
    
  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (C2S Login) from ("+source.getAddress().getHostAddress()+") CONTENTS: (username:"+username+"\tpassword:"+byteArrayToString()+")";
    }
      
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(username);
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    username=in.readString();
    if(type!=MessageType.C2S_LOGIN_SENDNAMEANDPASSWORD)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }


;  

