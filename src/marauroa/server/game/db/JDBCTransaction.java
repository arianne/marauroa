/* $Id: JDBCTransaction.java,v 1.3 2007/02/03 17:33:41 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import marauroa.common.Log4J;

import org.apache.log4j.Logger;

public class JDBCTransaction {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(JDBCTransaction.class);

	private Connection connection;

	public JDBCTransaction(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Sets Connection
	 * 
	 * @param Connection
	 *            a Connection
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Returns Connection
	 * 
	 * @return a Connection
	 */
	public Connection getConnection() {
		return connection;
	}

	public void begin() throws SQLException {
			Statement stmt = connection.createStatement();
			stmt.execute("start transaction;");
	}

	public void commit() throws SQLException {
			logger.debug("Commiting");
			connection.commit();
	}

	public void rollback() throws SQLException {
			logger.debug("Rollback");
			connection.rollback();
	}

	public boolean isValid() {
		boolean valid = false;

		if (connection != null) {
			try {
				if (!connection.isClosed()) {
					Statement stmt = connection.createStatement();
					String query = "show tables";

					logger.debug("isValid (" + query + ")");
					ResultSet result = stmt.executeQuery(query);
					result.close();
					stmt.close();
					valid = true;
				} else {
					logger.error("connection invalid, already closed.");
				}
			} catch (SQLException sqle) {
				logger.error("cannot validate connection", sqle);
			}
		}
		return valid;
	}
}
