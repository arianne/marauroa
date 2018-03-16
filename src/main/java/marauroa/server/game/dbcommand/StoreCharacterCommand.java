/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
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

import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * Asynchronously stores a character's progress.
 *
 * @author hendrik
 */
public class StoreCharacterCommand extends AbstractDBCommand {

	private final String username;
	private final String character;
	private final RPObject frozenObject;

	/**
	 * Asynchronously stores a character's progress
	 *
	 * @param username  username
	 * @param character charactername
	 * @param object    character object
	 */
	public StoreCharacterCommand(String username, String character, RPObject object) {
		this.username = username;
		this.character = character;
		this.frozenObject = (RPObject) object.clone();
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		DAORegister.get().get(CharacterDAO.class).storeCharacter(transaction, username, character, frozenObject);
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "StoreCharacterCommand [username=" + username + ", character="
				+ character + ", frozenObject=" + frozenObject + "]";
	}
}
