/* $Id: MessageS2CMap.java,v 1.3 2004/04/25 01:19:33 arianne_rpg Exp $ */
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

import java.io.*;
import java.net.*;
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
  private byte[] mapByteArray;
  
  /** Constructor for allowing creation of an empty message */
  public MessageS2CMap()
    {
    super(null);
    type=TYPE_S2C_MAP;
    
    mapByteArray=new byte[0];
    }
  
  /** Constructor with a TCP/IP source/destination of the message and the name
   *  of the choosen character.
   *  @param source The TCP/IP address associated to this message
   *  @param modifiedRPObjects the list of object that has been modified.
   *  @param deletedRPObjects the list of object that has been deleted since the last perception.
   */
  public MessageS2CMap(InetSocketAddress source,byte[] mapByteArray)
    {
    super(source);
    type=TYPE_S2C_MAP;
    
    this.mapByteArray=mapByteArray;
    }
  
  public byte[] getMapData()
    {
    return mapByteArray;
    }

  public void writeObject(marauroa.net.OutputSerializer out) throws IOException
    {
    super.writeObject(out);
    out.write(mapByteArray);
    }
  
  public void readObject(marauroa.net.InputSerializer in) throws IOException, ClassNotFoundException
    {
    super.readObject(in);
    mapByteArray=in.readByteArray();
    }
  }
