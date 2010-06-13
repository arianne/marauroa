/* $Id: TimeoutConf.java,v 1.20 2010/06/13 20:16:44 nhnb Exp $ */
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

	/** This indicates when we stop waiting for the socket. The lower, the slower. */
	public static final int SOCKET_TIMEOUT = 10;

	/**
	 * It is possible for a player to start login procedure but not complete it
	 * with a hacked client and if connection is not closed, that player is
	 * taking a "slot" of the game and so disallowing other real players to
	 * play.
	 */
	public static final long UNCOMPLETED_LOGIN_TIMEOUT = 60000;

	/**
	 * This indicates how much time we wait for a message to arrive. The lower,
	 * the slower.
	 */
	public static final int GAMESERVER_MESSAGE_GET_TIMEOUT = 1000;

	/** Maximum size of bytes on a message (256KB) */
	public static final int MAX_BYTE_ARRAY_ELEMENTS = 256 * 1024;

	/** Maximum size of elements on a array (256K) */
	public static final int MAX_ARRAY_ELEMENTS = 256 * 1024;

	/**
	 * The amount of failed login tries before considering the account blocked.
	 */
	public static final int FAILED_LOGIN_ATTEMPTS_ACCOUNT = 10;

	/**
	 * The amount of failed login tries before considering the ip blocked.
	 */
	public static final int FAILED_LOGIN_ATTEMPTS_IP = 3;
	
	/**
	 * The amount of seconds until being able to retry login after N failed
	 * attempts.
	 */
	public static final int FAILED_LOGIN_BLOCKTIME = 600;

	/**
	 * The timeframe to check when counting account creations.
	 */
	public static final int ACCOUNT_CREATION_COUNTINGTIME = 3600;

	/**
	 * The number of accounts that may be created within the timeframe
	 * from one ip-address
	 */
	public static final int ACCOUNT_CREATION_LIMIT = 5;

	/**
	 * The timeframe to check when counting character creations.
	 */
	public static final int CHARACTER_CREATION_COUNTINGTIME = 3600;

	/**
	 * The number of characters that may be created within the timeframe
	 * from one ip-address
	 */
	public static final int CHARACTER_CREATION_LIMIT = 5;

	/**
	 * The number of parallel connections that may be used from
	 * one ip-address.
	 */
	public static final int PARALLEL_CONNECTION_LIMIT = 10;

}
