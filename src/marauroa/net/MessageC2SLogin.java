/* $Id: MessageC2SLogin.java,v 1.4 2004/04/30 13:48:44 arianne_rpg Exp $ */
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
public class MessageC2SLogin extends Message
  {
  private String username;
  private String password;
  /** Constructor for allowing creation of an empty message */
  public MessageC2SLogin()
    {
    super(null);
    type=TYPE_C2S_LOGIN;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param username the username of the user that wants to login
   *  @param password the plain password of the user that wants to login
   */
  public MessageC2SLogin(InetSocketAddress source,String username, String password)
    {
    super(source);
    type=TYPE_C2S_LOGIN;
    this.username=username;
    this.password=password;
    }  
  
  /** This method returns the username
   *  @return the username */
  public String getUsername()
    {
    return username;    
    }
    
  /** This method returns the password
   *  @return the password */
  public String getPassword()
    {
    return password;
    }

  /** This method returns a String that represent the object 
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (C2S Login) from ("+source.getAddress().getHostAddress()+") CONTENTS: (username:"+username+"\tpassword:"+password+")";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(username);
    out.write(password);
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    super.readObject(in);
    username=in.readString();
    password=in.readString();
    if(type!=TYPE_C2S_LOGIN)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }    
  }


;  

