/* $Id: MessageS2CPerception.java,v 1.12 2004/01/27 17:03:12 arianne_rpg Exp $ */
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
  
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import marauroa.game.RPObject;
import marauroa.game.RPZone;
import marauroa.*;


/** This message indicate the client the objects that the server has determined that
 *  this client is able to see.
 *
 *  @see marauroa.net.Message
 *  @see marauroa.game.RPZone
 */
public class MessageS2CPerception extends Message
  {
  private byte typePerception;
  private List modifiedRPObjects;
  private List deletedRPObjects;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CPerception()
    {
    super(null);
    type=TYPE_S2C_PERCEPTION;
    }

  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param modifiedRPObjects the list of object that has been modified.
   *  @param deletedRPObjects the list of object that has been deleted since the last perception.
   */
  public MessageS2CPerception(InetSocketAddress source,byte typePerception, List modifiedRPObjects, List deletedRPObjects)
    {
    super(source);
    
    type=TYPE_S2C_PERCEPTION;

    /** TODO: Make this choosable */
    this.typePerception=typePerception;
    this.modifiedRPObjects=modifiedRPObjects;
    this.deletedRPObjects=deletedRPObjects;
    }
  
  public byte getTypePerception()
    {
    return typePerception;
    }
  
  /** This method returns the list of modified objects
   *  @return List<RPObject> of modified objects */
  public List getModifiedRPObjects()
    {
    return modifiedRPObjects;
    }

  /** This method returns the list of deleted objects
   *  @return List<RPObject> of deleted objects */
  public List getDeletedRPObjects()
    {
    return deletedRPObjects;
    }
  
  /** This method returns a String that represent the object
   *  @return a string representing the object.*/
  public String toString()
    {
    return "Message (S2C Perception) from ("+source.toString()+") CONTENTS: ("+modifiedRPObjects.size()+" modified objects and "+
           deletedRPObjects.size()+" deleted objects)";
    }
      
  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    
    out.write(typePerception);
    out.write((int)modifiedRPObjects.size());
    
    Iterator it_mod=modifiedRPObjects.iterator();
    while(it_mod.hasNext())
      {
      out.write((RPObject)it_mod.next());
      }

    out.write((int)deletedRPObjects.size());

    Iterator it_del=deletedRPObjects.iterator();
    while(it_del.hasNext())
      {
      out.write((RPObject)it_del.next());
      }
    }
    
  public void readObject(marauroa.net.InputSerializer in) throws IOException, java.lang.ClassNotFoundException
    {
    marauroad.trace("MessageS2CPerception::readObject()",">");
    super.readObject(in);
    
    typePerception=in.readByte();
    modifiedRPObjects=new LinkedList();
    deletedRPObjects=new LinkedList();
    
    int mod=in.readInt();

    if(mod>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(mod)+" size");
      }

    marauroad.trace("MessageS2CPerception::readObject()","D",mod + " modified objects..");
    for(int i=0;i<mod;++i)
      {
      modifiedRPObjects.add(in.readObject(new RPObject()));
      }

    int del=in.readInt();

    if(del>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(del)+" size");
      }

    marauroad.trace("MessageS2CPerception::readObject()","D",del + " deleted objects..");
    for(int i=0;i<del;++i)
      {
      deletedRPObjects.add(in.readObject(new RPObject()));
      }
    
    marauroad.trace("MessageS2CPerception::readObject()","<");
    }
    
  };


  
