/* $Id: MessageS2CLoginNACK.java,v 1.2 2003/12/08 01:08:30 arianne_rpg Exp $ */
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
  
/** This message indicate the client that the server has reject its login Message
 *  @see marauroa.net.Message
 */
public class MessageS2CLoginNACK extends Message
  {
  public static byte UNKNOWN_REASON=0;
  public static byte USERNAME_WRONG=1;
  public static byte SERVER_IS_FULL=2;  
  
  static private String[] text=
    {
    "Unknown reason",
    "Username/Password incorrect.",
    "Server is full."    
    };
    
  private byte reason;  
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CLoginNACK()
    {
    super(null);
    
    type=TYPE_S2C_LOGIN_NACK;
    }

  /** Constructor with a TCP/IP source/destination of the message 
   *  @param source The TCP/IP address associated to this message
   *  @param resolution the reason to deny the login */
  public MessageS2CLoginNACK(InetSocketAddress source, byte resolution)
    {
    super(source);
    
    type=TYPE_S2C_LOGIN_NACK;
    
    reason=resolution;
    }  
  
  /** This method returns the resolution of the login event 
   *  @return a byte representing the resolution given.*/
  public byte getResolutionCode()
    {
    return reason;
    }
  
  /** This method returns a String that represent the resolution given to the login event
   *  @return a string representing the resolution.*/
  public String getResolution()
    {
    return text[reason];    
    }

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Login NACK) from ("+source.toString()+") CONTENTS: ("+getResolution()+")";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(reason);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    reason=in.readByte();
    
    if(type!=TYPE_S2C_LOGIN_NACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  };


  