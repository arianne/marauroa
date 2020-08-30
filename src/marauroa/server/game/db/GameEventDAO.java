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
package marauroa.server.game.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.rp.GameEvent;

/**
 * data access object for game events
 *
 * @author miguel, hendrik
 */
public class GameEventDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameEventDAO.class);

	/**
	 * Creates a new GameEventDAO
	 */
	protected GameEventDAO() {
		// hide constructor as this class should only be instantiated by DAORegister
	}

	/**
	 * adds an game event to the log
	 *
	 * @param transaction DBTransaction
	 * @param source player name
	 * @param event event type
	 * @param params parameters
	 * @throws SQLException in case of an database error
	 */
	@Deprecated
	public void addGameEvent(DBTransaction transaction, String source, String event, String... params) throws SQLException {
		this.addGameEvent(transaction, new Timestamp(new Date().getTime()), source, event, params);
	}

	/**
	 * adds an game event to the log
	 *
	 * @param transaction DBTransaction
	 * @param timestamp timestamp
	 * @param source player name
	 * @param event event type
	 * @param params parameters
	 * @throws SQLException in case of an database error
	 */
	public void addGameEvent(DBTransaction transaction, Timestamp timestamp, String source, String event, String... params) throws SQLException {
		String firstParam = (params.length > 0 ? params[0] : "");
		StringBuilder param = new StringBuilder();
		if (params.length > 1) {
			for (int i = 1; i < params.length; i++) {
				param.append(params[i]);
				param.append(" ");
			}
		}
		String param2 = param.toString();

		// write the row to the database, escaping and cutting the parameters to column size
		String query = "insert into gameEvents(source, event, param1, param2, timedate)"
			+ " values('[source]', '[event]', '[param1]', '[param2]', '[timedate');";

		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("source", source);
		sqlParams.put("event", event);
		sqlParams.put("param1", (firstParam == null ? null : firstParam.substring(0, Math.min(127, firstParam.length()))));
		sqlParams.put("param2", param2.substring(0, Math.min(255, param2.length())));
		sqlParams.put("timedate", timestamp);

		transaction.execute(query, sqlParams);
	}

	/**
	 * adds an list of game event to the log
	 *
	 * @param transaction DBTransaction
	 * @param gameEvents list of GameEvents
	 * @throws SQLException in case of an database error
	 */
	public void addGameEvents(DBTransaction transaction, List<GameEvent> gameEvents)  throws SQLException {
		String query = "INSERT INTO gameEvents(source, event, timedate, param1, param2)"
				+ " values(?, ?, ?, ?, ?);"; 
			PreparedStatement stmt = transaction.prepareStatement(query, null);
			logger.debug("ActionsDAO is executing query " + query);

			for (GameEvent gameEvent : gameEvents) {
				String[] params = gameEvent.getParams();
				String firstParam = (params.length > 0 ? params[0] : "");
				StringBuilder param = new StringBuilder();
				if (params.length > 1) {
					for (int i = 1; i < params.length; i++) {
						param.append(params[i]);
						param.append(" ");
					}
				}
				String param2 = param.toString();


				stmt.setString(1, gameEvent.getSource());
				stmt.setString(2, gameEvent.getEvent());
				stmt.setTimestamp(3, gameEvent.getTimestamp());
				stmt.setString(4, (firstParam == null ? null : firstParam.substring(0, Math.min(127, firstParam.length()))));
				stmt.setString(5, param2.substring(0, Math.min(255, param2.length())));
				stmt.addBatch();
			}
			stmt.executeBatch();
			stmt.close();
	}

	/**
	 * adds an game event to the log
	 *
	 * @param source player name
	 * @param event event type
	 * @param params parameters
	 */
	@Deprecated
	public void addGameEvent(String source, String event, String... params) {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addGameEvent(transaction, source, event, params);
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			logger.error("Error adding game event: " + event, e);
			TransactionPool.get().rollback(transaction);
		} catch (RuntimeException e) {
			logger.error("Error adding game event: " + event, e);
			TransactionPool.get().rollback(transaction);
		}
	}
}
