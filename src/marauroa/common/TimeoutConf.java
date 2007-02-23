/* $Id: TimeoutConf.java,v 1.10 2007/02/23 10:52:02 arianne_rpg Exp $ */
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

/**
 * This class stores some constants about server timeout values.
 *
 * @author miguel
 *
 */
public class TimeoutConf {
	/** This indicate when we stop to wait on the socket. The lower the slower. */
	final public static int SOCKET_TIMEOUT = 10;

	/**
	 * It is possible for a player to start login procedure but not complete it with a
	 * hacked client and if connection is not closed, that player is taking a "slot" of
	 * the game and so disallowing other real players to play.
	 */
	final public static long UNCOMPLETED_LOGIN_TIMEOUT=60000;

	/**
	 * This indicate how many time we wait for a message to arrive. The lower
	 * the slower.
	 */
	final public static int GAMESERVER_MESSAGE_GET_TIMEOUT = 1000;

	/** Maximum size of bytes on a message (256KB) */
	final public static int MAX_BYTE_ARRAY_ELEMENTS = 256 * 1024;

	/** Maximum size of elements on a array (256K) */
	final public static int MAX_ARRAY_ELEMENTS = 256 * 1024;
}
