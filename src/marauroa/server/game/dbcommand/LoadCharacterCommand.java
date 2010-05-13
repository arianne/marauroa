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
import java.sql.SQLException;

import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * asynchronously loads a charcter's RPObject.
 *
 * @author hendrik
 */
public class LoadCharacterCommand  extends AbstractDBCommand {
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

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		object = DAORegister.get().get(CharacterDAO.class).loadCharacter(username, character);
		
	}

	/**
	 * Gets the RPObject
	 *
	 * @return RPObject
	 */
	protected RPObject getObject() {
		return object;
	}

}
