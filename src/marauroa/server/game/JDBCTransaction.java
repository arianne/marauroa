/* $Id: JDBCTransaction.java,v 1.7 2006/08/20 15:40:15 wikipedian Exp $ */
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

package marauroa.server.game;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import marauroa.common.Log4J;

import org.apache.log4j.Logger;

public class JDBCTransaction extends Transaction {
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

	public void begin() throws TransactionException {
		try {
			Statement stmt = connection.createStatement();
			stmt.execute("start transaction;");
		} catch (SQLException e) {
			throw new TransactionException(e.getMessage(), e);
		}
	}

	public void commit() throws TransactionException {
		try {
			logger.debug("Commiting");
			connection.commit();
		} catch (SQLException e) {
			throw new TransactionException(e.getMessage(), e);
		}
	}

	public void rollback() {
		try {
			logger.debug("Rollback");
			connection.rollback();
		} catch (SQLException e) {
			// throw new TransactionException(e.getMessage());
			logger.error("rollback failed", e);
		}
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
