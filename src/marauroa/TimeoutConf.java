/* $Id: TimeoutConf.java,v 1.1 2003/12/08 23:10:01 arianne_rpg Exp $ */
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
  final public static int SOCKET_TIMEOUT=100;
  final public static int GAMESERVER_MESSAGE_GET_TIMEOUT=1000;
  
  final public static int CLIENT_MESSAGE_DROPPED_TIMEOUT=30000;
  final public static int CLIENT_NETWORK_NUM_READ=5;
  
  final public static int GAMESERVER_PLAYER_TIMEOUT=30000;
  }