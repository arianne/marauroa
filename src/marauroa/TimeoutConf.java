/* $Id: TimeoutConf.java,v 1.19 2004/06/04 13:52:12 arianne_rpg Exp $ */
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
package marauroa;

public class TimeoutConf
  {
  final public static boolean TIMEOUT_ENABLE=true;
  /** This indicate when we stop to wait on the socket. The lower the slower. */
  final public static int SOCKET_TIMEOUT=100;
  /** This indicate how many time we wait for a message to arrive. The lower the slower. */
  final public static int GAMESERVER_MESSAGE_GET_TIMEOUT=1000;
  /** This indicate when the client remove the incomplete packet from its queue */
  final public static int CLIENT_MESSAGE_DROPPED_TIMEOUT=60000;
  /** Indicate how many packets can be read from network before returing */
  final public static int CLIENT_NETWORK_NUM_READ=5;
  /** This indicate that the player is totally dead and must be removed.
   *  Should be related to Turn Duration, around 4-10 times bigger at least.  */
  final public static int GAMESERVER_PLAYER_TIMEOUT=20000;
  final public static int GAMESERVER_PLAYER_STORE_LAPSUS=600000;
  
  final public static int MAX_BYTE_ARRAY_ELEMENTS=8*1024;
  final public static int MAX_ARRAY_ELEMENTS=1024;
  }
