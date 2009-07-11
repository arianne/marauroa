/* $Id: GameEventDAO.java,v 1.4 2009/07/11 13:38:48 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;

/**
 * data access object for game events
 *
 * @author miguel, hendrik
 */
public class GameEventDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameEventDAO.class);

	public void addGameEvent(DBTransaction transaction, String source, String event, String... params) {
		try {
			String firstParam = (params.length > 0 ? params[0] : "");
			StringBuffer param = new StringBuffer();
			if (params.length > 1) {
				for (int i = 1; i < params.length; i++) {
					param.append(params[i]);
					param.append(" ");
				}
			}
			String param2 = param.toString();

			// write the row to the database, escaping and cutting the parameters to column size
			String query = "insert into gameEvents(timedate, source, event, param1, param2)"
				+ " values(NULL, '[source]', '[event]', '[param1]', '[param2]');";

			Map<String, Object> sqlParams = new HashMap<String, Object>();
			sqlParams.put("source", source);
			sqlParams.put("event", event);
			sqlParams.put("param1", (firstParam == null ? null : firstParam.substring(0, Math.min(127, firstParam.length()))));
			sqlParams.put("param2", (param2 == null ? null : param2.substring(0, Math.min(255, param2.length()))));

			transaction.execute(query, sqlParams);
		} catch (SQLException sqle) {
			logger.warn("Error adding game event: " + event, sqle);
		}
	}

	public void addGameEvent(String source, String event, String... params) {
		DBTransaction transaction = TransactionPool.get().beginWork();
		addGameEvent(transaction, source, event, params);
		try {
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			logger.warn("Error adding game event: " + event, e);
		}
	}

}
