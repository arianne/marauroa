/* $Id: MessageS2CServerInfo.java,v 1.2 2004/03/24 15:25:34 arianne_rpg Exp $ */
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

/** The CharacterListMessage is sent from server to client to inform client about
 *  any relevant info the server has to transmit. They are in the form of 
 *  <attribute>=<value> */  
public class MessageS2CServerInfo extends Message
  {
  private String[] contents;
  /** Constructor for allowing creation of an empty message */
  public MessageS2CServerInfo()
    {
    super(null);
    type=TYPE_S2C_SERVERINFO;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param contents the list of strings to describe the server.
   *  @see marauroa.net.MessageS2CCharacters
   */
  public MessageS2CServerInfo(InetSocketAddress source,String[] contents)
    {    
    super(source);
    type=TYPE_S2C_SERVERINFO;
    this.contents=contents;
    }  
  
  /** This method returns the list of string that describe the server
   *  @return the list of strings to describe the server */
  public String[] getContents()
    {
    return contents;    
    }

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    StringBuffer text=new StringBuffer(" ");

    for(int i=0;i<contents.length;++i)
      {
      text.append("["+contents[i]+"],");
      }
    return "Message (S2C Server Info) from ("+source.toString()+") CONTENTS: ("+text.substring(0,text.length()-1)+")";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(contents);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    contents=in.readStringArray();
    if(type!=TYPE_S2C_SERVERINFO)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }


;
