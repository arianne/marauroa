/* $Id: MessageS2CLogoutACK.java,v 1.3 2004/03/24 15:25:34 arianne_rpg Exp $ */
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
  
/** This message indicate the client that the server has accepted its Logout Message
 *  @see marauroa.net.Message
 */
public class MessageS2CLogoutACK extends Message
  {
  /** Constructor for allowing creation of an empty message */
  public MessageS2CLogoutACK()
    {
    super(null);
    type=TYPE_S2C_LOGOUT_ACK;
    }

  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message */
  public MessageS2CLogoutACK(InetSocketAddress source)
    {
    super(source);
    type=TYPE_S2C_LOGOUT_ACK;
    }  

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Logout ACK) from ("+source.toString()+") CONTENTS: ()";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    if(type!=TYPE_S2C_LOGOUT_ACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }


;  

