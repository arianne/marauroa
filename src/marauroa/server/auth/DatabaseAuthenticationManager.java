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
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandPriority;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.container.SecuredLoginInfo;
import marauroa.server.game.dbcommand.LoginCommand;
import marauroa.server.game.messagehandler.DelayedEventHandler;

/**
 * authenticates against the database (e. g. account or loginseeds tables).
 */
public class DatabaseAuthenticationManager implements AuthenticationManager {

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
			Channel channel, int protocolVersion) {

		DBCommand command = new LoginCommand(info, callback, clientid, channel, protocolVersion);
		DBCommandQueue.get().enqueue(command, DBCommandPriority.CRITICAL);
	}
}
