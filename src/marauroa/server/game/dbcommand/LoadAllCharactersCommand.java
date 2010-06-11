/***************************************************************************
 *                   (C) Copyright 2009-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.dbcommand;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Map;

import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.messagehandler.DelayedEventHandler;
import marauroa.server.game.messagehandler.DelayedEventHandlerThread;

/**
 * asynchronously loads a list of all character for a user.
 *
 * @author hendrik
 */
public class LoadAllCharactersCommand  extends DBCommandWithCallback {
	private String username;
	private Map<String, RPObject> characters;

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param username name of account
	 */
	public LoadAllCharactersCommand(String username) {
		super();
		this.username = username;
	}

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param username name of account
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion version of protocol
	 */
	public LoadAllCharactersCommand(String username,
			DelayedEventHandler callback, int clientid, SocketChannel channel, int protocolVersion) {
		super(callback, clientid, channel, protocolVersion);
		this.username = username;
	}


	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		characters = DAORegister.get().get(CharacterDAO.class).loadAllCharacters(transaction, username);
		if (callback != null) {
			DelayedEventHandlerThread.get().addDelayedEvent(callback, this);
		}
	}

	/**
	 * Gets the characters map
	 *
	 * @return characters
	 */
	public Map<String, RPObject> getCharacters() {
		return characters;
	}
}
