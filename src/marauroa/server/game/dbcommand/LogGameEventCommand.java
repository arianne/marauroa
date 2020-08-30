/***************************************************************************
 *                   (C) Copyright 2009-2020 - Marauroa                    *
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.GameEventDAO;
import marauroa.server.game.rp.GameEvent;

/**
 * logs gameEvents
 *
 * @author hendrik
 */
public class LogGameEventCommand extends AbstractDBCommand {
	private final List<GameEvent> gameEvents;

	private String source;
	private String event;
	private String[] params;

	/**
	 * creates a new LogGameEventCommand.
	 *
	 * @param gameEvents list of GameEvent
	 */
	public LogGameEventCommand(List<GameEvent> gameEvents) {
		this.gameEvents = new LinkedList<GameEvent>(gameEvents);
	}

	/**
	 * creates a new LogGameEventCommand.
	 *
	 * @param source player name
	 * @param event event type
	 * @param params parameters
	 */
	public LogGameEventCommand(String source, String event, String... params) {
		this.gameEvents = null;
		this.source = source;
		this.event = event;
		this.params = new String[params.length];
		System.arraycopy(params, 0, this.params, 0, params.length);
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException {
		if (gameEvents == null) {
			DAORegister.get().get(GameEventDAO.class).addGameEvent(transaction, this.getEnqueueTime(), source, event, params);
		} else {
			DAORegister.get().get(GameEventDAO.class).addGameEvents(transaction, gameEvents);
		}
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "LogGameEventCommand [source=" + source + ", event=" + event
				+ ", params=" + Arrays.toString(params) + "]";
	}
}
