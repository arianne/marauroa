/***************************************************************************
 *                   (C) Copyright 2003-2020 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package marauroa.server.auth;

import marauroa.common.net.Channel;
import marauroa.server.game.container.SecuredLoginInfo;
import marauroa.server.game.messagehandler.DelayedEventHandler;

/**
 * Authentication interface
 */
public interface AuthenticationManager {
	
	/**
	 * Verify that a player is whom he/she says it is. Either the username and credentials match.
	 * (for example username and password). 
	 * 
	 * Or this method sets info.username with a username derived from the credentials.
	 * (for example the client provided an oauth token and this method requested user information
	 * from an OAuth resource server).
	 * 
	/**
	 * Verify that a player is whom he/she says it is.
	 *
	 * @param info SecuredLoginInfo
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion protocolVersion
	 */
	public void verify(SecuredLoginInfo info, DelayedEventHandler callback, int clientid,
			Channel channel, int protocolVersion);
}
