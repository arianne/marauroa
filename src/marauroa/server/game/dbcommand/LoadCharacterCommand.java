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

import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.messagehandler.DelayedEventHandler;
import marauroa.server.game.messagehandler.DelayedEventHandlerThread;

/**
 * asynchronously loads a character's RPObject.
 *
 * @author hendrik
 */
public class LoadCharacterCommand extends DBCommandWithCallback {
	private String username;
	private String character;
	private RPObject object;

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param username name of account
	 * @param character name of character
	 */
	public LoadCharacterCommand(String username, String character) {
		super();
		this.username = username;
		this.character = character;
	}

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param username name of account
	 * @param character name of character
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion version of protocol
	 */
	public LoadCharacterCommand(String username, String character,
			DelayedEventHandler callback, int clientid, SocketChannel channel, int protocolVersion) {
		super(callback, clientid, channel, protocolVersion);
		this.username = username;
		this.character = character;
	}


	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		object = DAORegister.get().get(CharacterDAO.class).loadCharacter(transaction, username, character);
		if (callback != null) {
			DelayedEventHandlerThread.get().addDelayedEvent(callback, this);
		}
	}

	/**
	 * Gets the RPObject
	 *
	 * @return RPObject
	 */
	public RPObject getObject() {
		return object;
	}

	/**
	 * gets the name of the character
	 *
	 * @return name of character
	 */
	public String getCharacterName() {
		return character;
	}

	/**
	 * gets the username
	 *
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "LoadCharacterCommand [username=" + username + ", character="
				+ character + "]";
	}
}
