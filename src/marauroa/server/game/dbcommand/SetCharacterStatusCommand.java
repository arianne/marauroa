/***************************************************************************
 *                      (C) Copyright 2018 - Marauroa                      *
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

import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * Asynchronously set a character's status.
 *
 * @author hendrik
 */
public class SetCharacterStatusCommand extends AbstractDBCommand {
	private final String username;
	private final String character;
	private final String status;


	/**
	 * Asynchronously stores a character's progress
	 *
	 * @param username  username
	 * @param character character name
	 * @param status    status
	 */
	public SetCharacterStatusCommand(String username, String character, String status) {
		this.username = username;
		this.character = character;
		this.status = status;
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		DAORegister.get().get(CharacterDAO.class).setCharacterStatus(username, character, status);
	}

}
