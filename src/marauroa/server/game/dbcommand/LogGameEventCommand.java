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

import java.sql.SQLException;

import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.GameEventDAO;

/**
 * logs an gameEvent
 *
 * @author hendrik
 */
public class LogGameEventCommand extends AbstractDBCommand {
	private String source;
	private String event;
	private String[] params;

	/**
	 * creates a new LogGameEventCommand.
	 *
	 * @param source player name
	 * @param event event type
	 * @param params parameters
	 */
	public LogGameEventCommand(String source, String event, String... params) {
		this.source = source;
		this.event = event;
		this.params = new String[params.length];
		System.arraycopy(params, 0, this.params, 0, params.length);
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException {
		DAORegister.get().get(GameEventDAO.class).addGameEvent(transaction, source, event, params);
	}
}
