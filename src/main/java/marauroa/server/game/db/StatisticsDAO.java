/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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
import marauroa.server.game.Statistics.Variables;

/**
 * data access object for statistics.
 *
 * @author miguel, hendrik
 */
public class StatisticsDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(StatisticsDAO.class);

	/**
	 * Creates a new StatisticsDAO
	 */
	protected StatisticsDAO() {
		// hide constructor as this class should only be instantiated by DAORegister
	}

	/**
	 * adds an statistics sample to the database log
	 * 
	 * @param transaction DBTransaction
	 * @param var Variables
	 * @throws SQLException in case of an database error
	 */
	public void addStatisticsEvent(DBTransaction transaction, Variables var) throws SQLException {
		String query = "insert into statistics(bytes_send, bytes_recv, players_login, players_logout, players_timeout, players_online, ips_online) "
			+ " values([Bytes send], [Bytes recv], [Players login], [Players logout], [Players timeout], [Players online], [Ips online])";
		Map<String, Object> params = new HashMap<String, Object>();
		for (String key : var) {
			params.put(key, var.get(key));
		}
		transaction.execute(query, params);
	}

	/**
	 * adds an statistics sample to the database log
	 * 
	 * @param var Variables
	 */
	public void addStatisticsEvent(Variables var) {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addStatisticsEvent(transaction, var);
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			logger.error(e, e);
			TransactionPool.get().rollback(transaction);
		} catch (RuntimeException e) {
			logger.error(e, e);
			TransactionPool.get().rollback(transaction);
		}
	}
}
