/* $Id: MessageS2CInvalidMessage.java,v 1.3 2004/04/30 13:48:44 arianne_rpg Exp $ */
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
  
/** This message indicate the server that the client wants to login and send the
 *  needed info: username and password to login to server.
 *  @see marauroa.net.Message
 */
public class MessageS2CInvalidMessage extends Message
  {
  private String reason;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CInvalidMessage()
    {
    super(null);
    type=TYPE_S2C_INVALIDMESSAGE;
    reason="";
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param username the username of the user that wants to login
   *  @param password the plain password of the user that wants to login
   */
  public MessageS2CInvalidMessage(InetSocketAddress source,String reason)
    {
    super(source);
    type=TYPE_S2C_INVALIDMESSAGE;
    this.reason=reason;
    }  
  
  /** This method returns the reason
   *  @return the reason */
  public String getReason()
    {
    return reason;    
    }
    
  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Message Invalid) from ("+source.getAddress().getHostAddress()+") CONTENTS: (reason:"+reason+")";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(reason);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    reason=in.readString();

    if(type!=TYPE_S2C_INVALIDMESSAGE)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  };  

