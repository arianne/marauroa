/* $Id: MessageS2CChooseCharacterACK.java,v 1.1 2005/01/23 21:00:44 arianne_rpg Exp $ */
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
import marauroa.common.game.*;
  
/** This message indicate the client that the server has accepted its ChooseCharacter Message
 *  @see marauroa.common.net.Message
 */
public class MessageS2CChooseCharacterACK extends Message
  {
  /** Constructor for allowing creation of an empty message */
  public MessageS2CChooseCharacterACK()
    {
    super(null);
    type=TYPE_S2C_CHOOSECHARACTER_ACK;
    }

  /** Constructor with a TCP/IP source/destination of the message
   *  @param source The TCP/IP address associated to this message */
  public MessageS2CChooseCharacterACK(InetSocketAddress source)
    {
    super(source);
    type=TYPE_S2C_CHOOSECHARACTER_ACK;
    }
   
  /** This method returns a String that represent the object
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Choose Character ACK) from ("+source.getAddress().getHostAddress()+") CONTENTS: ()";
    }
      
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);

    if(type!=TYPE_S2C_CHOOSECHARACTER_ACK)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }
  }


;
