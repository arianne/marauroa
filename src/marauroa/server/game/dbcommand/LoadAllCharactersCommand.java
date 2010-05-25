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
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.messagehandler.DelayedEventHandler;
import marauroa.server.game.messagehandler.DelayedEventHandlerThread;

/**
 * asynchronously loads a list of all character for a user.
 *
 * @author hendrik
 */
public class LoadAllCharactersCommand  extends AbstractDBCommand {
	private String username;
	private String character;
	private Map<String, RPObject> characters;

	private int clientid;
	private SocketChannel channel;
	private DelayedEventHandler callback;

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param username name of account
	 * @param character name of character
	 */
	public LoadAllCharactersCommand(String username, String character) {
		super();
		this.username = username;
		this.character = character;
	}

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param username name of account
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 */
	public LoadAllCharactersCommand(String username,
			DelayedEventHandler callback, int clientid, SocketChannel channel) {
		this.username = username;
		this.callback = callback;
		this.clientid = clientid;
		this.channel = channel;
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

	/**
	 * gets the name of the character
	 *
	 * @return name of character
	 */
	public String getCharacterName() {
		return character;
	}

	/**
	 * gets the clientid
	 *
	 * @return clientid
	 */
	public int getClientid() {
		return clientid;
	}

	/**
	 * gets the SocketChannel
	 *
	 * @return SocketChannel
	 */
	public SocketChannel getChannel() {
		return channel;
	}
}
