/* $Id: MessageS2CMap.java,v 1.7 2004/05/19 16:38:35 arianne_rpg Exp $ */
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

import java.util.*;
import java.net.*;
import java.io.*;
import marauroa.game.*;
import marauroa.*;

/** This message indicate the client the map/s that the server has determined that
 *  this client is able to see.
 *
 *  @see marauroa.net.Message
 *  @see marauroa.game.RPZone
 */
public class MessageS2CMap extends Message
  {
  private List mapObjects;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CMap()
    {
    super(null);
    type=TYPE_S2C_MAP;
    }
  
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param modifiedRPObjects the list of object that has been modified.
   *  @param deletedRPObjects the list of object that has been deleted since the last perception.
   */
  public MessageS2CMap(InetSocketAddress source,List mapObjects)
    {
    super(source);
    type=TYPE_S2C_MAP;
    
    this.mapObjects=mapObjects;
    }
  
  public List getMapObjects()
    {
    return mapObjects;
    }
  
  public String toString()
    {
    return "Message (S2C Map) from ("+source.getAddress().getHostAddress()+") CONTENTS: ("+mapObjects.size()+")";
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);

    out.write((int)mapObjects.size());
    Iterator it=mapObjects.iterator();
    while(it.hasNext())
      {
      RPObject object=(RPObject)it.next();
      out.write(object);
      }
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws IOException, ClassNotFoundException
    {
    super.readObject(in);
    
    int mapObjectsSize=in.readInt();
    mapObjects=new LinkedList();
    
    if(mapObjectsSize>TimeoutConf.MAX_ARRAY_ELEMENTS)
      {
      throw new IOException("Illegal request of an list of "+String.valueOf(mapObjectsSize)+" size");
      }

    for(int i=0;i<mapObjectsSize;++i)
      {
      mapObjects.add(in.readObject(new RPObject()));
      }

    if(type!=TYPE_S2C_MAP)
      {
      throw new java.lang.ClassNotFoundException();
      }
    }
  }
