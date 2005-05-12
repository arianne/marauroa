/* $Id: MessageC2SLoginRequestKey.java,v 1.2 2005/05/12 19:34:36 arianne_rpg Exp $ */
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
  
/** This message indicate the client want the server to send his public RSA key.
 *  @see marauroa.common.net.Message
 */
public class MessageC2SLoginRequestKey extends Message
  {
  private String game;
  private String version;
  
  /** Constructor for allowing creation of an empty message */
  public MessageC2SLoginRequestKey()
    {
    super(MessageType.C2S_LOGIN_REQUESTKEY,null);
    }

  /** Constructor with a TCP/IP source/destination of the message 
   *  @param source The TCP/IP address associated to this message */
  public MessageC2SLoginRequestKey(InetSocketAddress source, String game, String version)
    {
    super(MessageType.C2S_LOGIN_REQUESTKEY,source);
    this.game=game;
    this.version=version;
    }  
   
  public String getGame()
    {
    return game;
    }
  
  public String getVersion()
    {
    return version;
    }

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (C2S Login Request Key) from ("+source.getAddress().getHostAddress()+") CONTENTS: ()";
    }
      
  public void writeObject(OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write255LongString(game);
    out.write255LongString(version);
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    game=in.read255LongString();
    version=in.read255LongString();
    
    if(type!=MessageType.C2S_LOGIN_REQUESTKEY)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }
