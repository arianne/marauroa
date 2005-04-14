/* $Id: MessageC2SPerceptionACK.java,v 1.2 2005/04/14 09:59:06 quisar Exp $ */
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
  
/** This message indicate the server that the client has accepted its Perception Message
 *  @see marauroa.common.net.Message
 */
public class MessageC2SPerceptionACK extends Message
  {
  /** Constructor for allowing creation of an empty message */
  public MessageC2SPerceptionACK()
    {
    super(MessageType.C2S_PERCEPTION_ACK,null);
    }

  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message */
  public MessageC2SPerceptionACK(InetSocketAddress source)
    {
    super(MessageType.C2S_PERCEPTION_ACK,source);
    }  

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (C2S Perception ACK) from ("+source.getAddress().getHostAddress()+") CONTENTS: ()";
    }
      
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    if(type!=MessageType.C2S_PERCEPTION_ACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }


;  

