/***************************************************************************
 *                   (C) Copyright 2009-2013 - Marauroa                    *
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
import java.sql.SQLException;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.net.Channel;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.messagehandler.DelayedEventHandler;

/**
 * asynchronously loads a charcater's RPObject if the character is active 
 * and belongs to the account.
 *
 * @author hendrik
 */
public class LoadActiveCharacterCommand extends LoadCharacterCommand {
	private static Logger logger = Log4J.getLogger(LoadActiveCharacterCommand.class);

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param username name of account
	 * @param character name of character
	 */
	public LoadActiveCharacterCommand(String username, String character) {
		super(username, character);
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
	public LoadActiveCharacterCommand(String username, String character,
			DelayedEventHandler callback, int clientid, Channel channel, int protocolVersion) {
		super(username, character, callback, clientid, channel, protocolVersion);
	}


	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		if (DAORegister.get().get(CharacterDAO.class).hasActiveCharacter(transaction, getUsername(), getCharacterName())) {
			super.execute(transaction);
		} else {
			logger.warn("Trying to load a non active character. username: " + getUsername() + " charactername: " + getCharacterName());
		}
	}
}
