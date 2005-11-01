/* $Id: MessageS2CServerInfo.java,v 1.5 2005/11/01 10:09:29 mtotz Exp $ */
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
  
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import marauroa.common.game.RPClass;

/** The ServerInfo message is sent from server to client to inform client about
 *  any relevant info the server has to transmit. They are in the form of 
 *  <attribute>=<value> */  
public class MessageS2CServerInfo extends Message
  {
  private String[] contents;
  /** Constructor for allowing creation of an empty message */
  public MessageS2CServerInfo()
    {
    super(MessageType.S2C_SERVERINFO,null);
    }

  /** Constructor with a TCP/IP source/destination of the message 
   * and the content.
   *  @param source The TCP/IP address associated to this message
   *  @param contents the list of strings to describe the server.
   */
  public MessageS2CServerInfo(InetSocketAddress source,String[] contents)
    {    
    super(MessageType.S2C_SERVERINFO,source);
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
    return "Message (S2C Server Info) from ("+source.getAddress().getHostAddress()+") CONTENTS: ("+text.substring(0,text.length()-1)+")";
    }
      
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
    ByteArrayOutputStream array=new ByteArrayOutputStream();
    DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
    OutputSerializer serializer=new OutputSerializer(out_stream);
        
    serializer.write(contents);
    int size = RPClass.size();

    //sort out the default rp class if it is there
    for(Iterator<RPClass> it = RPClass.iterator(); it.hasNext();)
      {
      RPClass rp_class = it.next();
      if("".equals(rp_class.getName()))
        {
        size--;
        break;
        }
      }
     
    serializer.write(size);    
    for(Iterator<RPClass> it = RPClass.iterator(); it.hasNext();)
      {
      RPClass rp_class = it.next();
      if(!"".equals(rp_class.getName())) //sort out default class if it is there
        {
        serializer.write(rp_class);
        }
      }

    out_stream.close();
         
    out.write(array.toByteArray());
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    
    ByteArrayInputStream array=new ByteArrayInputStream(in.readByteArray());
    java.util.zip.InflaterInputStream szlib=new java.util.zip.InflaterInputStream(array,new java.util.zip.Inflater());
    InputSerializer serializer=new InputSerializer(szlib);

    contents=serializer.readStringArray();
    
    int size=serializer.readInt();
    for(int i=0;i<size;++i)
      {
      serializer.readObject(new RPClass());
      }
    
    if(type!=MessageType.S2C_SERVERINFO)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }


;
