/* $Id: MessageC2SChooseCharacter.java,v 1.1 2005/01/23 21:00:44 arianne_rpg Exp $ */
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

/** This message indicate the server what of the available characters is chosen
 *  for the session to play.
 *  @see marauroa.common.net.Message
 */
public class MessageC2SChooseCharacter extends Message
  {
  private String character;
  /** Constructor for allowing creation of an empty message */
  public MessageC2SChooseCharacter()
    {
    super(null);
    type=TYPE_C2S_CHOOSECHARACTER;
    }  
    
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param character The name of the choosen character that <b>MUST</b> be one
   *  of the returned by the marauroa.common.net.MessageS2CCharacters
   *  @see marauroa.common.net.MessageS2CCharacterList
   */
  public MessageC2SChooseCharacter(InetSocketAddress source,String character)
    {
    super(source);
    type=TYPE_C2S_CHOOSECHARACTER;
    this.character=character;
    }  
  
  /** This methods returns the name of the chosen character 
   @return the character name*/
  public String getCharacter()
    {
    return character;    
    }

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (C2S ChooseCharacter) from ("+source.getAddress().getHostAddress()+") CONTENTS: ("+character+")";
    }
      
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);    
    out.write(character);
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    character=in.readString();
    if(type!=TYPE_C2S_CHOOSECHARACTER)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }


;
