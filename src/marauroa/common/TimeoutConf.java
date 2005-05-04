/* $Id: TimeoutConf.java,v 1.5 2005/05/04 20:48:05 arianne_rpg Exp $ */
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
package marauroa.common;

public class TimeoutConf
  {
  /** This indicate when we stop to wait on the socket. The lower the slower. */
  final public static int SOCKET_TIMEOUT=10;
  
  /** This indicate how many time we wait for a message to arrive. The lower the slower. */
  final public static int GAMESERVER_MESSAGE_GET_TIMEOUT=1000;
  /** This indicate when the client remove the incomplete packet from its queue */
  final public static int CLIENT_MESSAGE_DROPPED_TIMEOUT=60000;
  /** Indicate how many packets can be read from network before returing */
  final public static int CLIENT_NETWORK_NUM_READ=20;
  
  /** This indicate that the player is totally dead and must be removed.
   *  Should be related to Turn Duration, around 4-10 times bigger at least.  */
  final public static int GAMESERVER_PLAYER_TIMEOUT=30000;
  /** This indicate that how often the player is stored on database.*/
  final public static int GAMESERVER_PLAYER_STORE_LAPSUS=3600000;
  
  /** Maximum size of bytes on a message (128KB) */
  final public static int MAX_BYTE_ARRAY_ELEMENTS=128*1024;
  /** Maximum size of elements on a array (65536) */
  final public static int MAX_ARRAY_ELEMENTS=64*1024;
  }
