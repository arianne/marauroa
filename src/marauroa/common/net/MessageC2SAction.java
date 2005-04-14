/* $Id: MessageC2SAction.java,v 1.2 2005/04/14 09:59:06 quisar Exp $ */
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
  
/** This message indicate the server the action the client wants to perform.
 *  @see marauroa.common.net.Message
 */
public class MessageC2SAction extends Message
  {
  private RPAction action;
  /** Constructor for allowing creation of an empty message */
  public MessageC2SAction()
    {
    super(MessageType.C2S_ACTION,null);
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param action the username of the user that wants to login
   */
  public MessageC2SAction(InetSocketAddress source,RPAction action)
    {
    super(MessageType.C2S_ACTION,source);
    this.action=action;
    }
  
  /** This method returns the action
   *  @return the action */
  public RPAction getRPAction()
    {
    return action;
    }
    
  /** This method returns a String that represent the object
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (C2S Action) from ("+source.getAddress().getHostAddress()+") CONTENTS: ("+action.toString()+")";
    }
      
  public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    action.writeObject(out); 
    }
    
  public void readObject(marauroa.common.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    action=(RPAction)in.readObject(new RPAction());
    if(type!=MessageType.C2S_ACTION)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }
  }


;
