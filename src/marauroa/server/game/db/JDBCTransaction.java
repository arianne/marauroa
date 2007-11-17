/* $Id: JDBCTransaction.java,v 1.14 2007/11/17 13:01:50 martinfuchs Exp $ */
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

/**
 * Implementation of a JDBC transaction for MySQL
 *
 * @author miguel
 */
public class JDBCTransaction implements Transaction {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(JDBCTransaction.class);

	private Connection connection;
	private JDBCAccess accessor;

	/**
	 * Constructor
	 *
	 * @param connection
	 *            associated database connection.
	 */
	public JDBCTransaction(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Sets Connection
	 *
	 * @param connection
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

	/**
	 * Starts a transaction
	 *
	 * @throws SQLException
	 */
	public void begin() throws SQLException {
		Statement stmt = connection.createStatement();
		stmt.execute("start transaction;");
	}

	/**
	 * commits the changes made to backstore.
	 *
	 * @throws SQLException
	 *             if the underlaying backstore throws an Exception
	 */
	public void commit() throws SQLException {
		logger.debug("Commiting");
		connection.commit();
		closeAccessor();
	}

	/**
	 * Makes previous changes to backstore invalid
	 */
	public void rollback() throws SQLException {
		logger.debug("Rollback");
		connection.rollback();
		closeAccessor();
	}

	/**
	 * Returns true if the transaction is still valid. A transaction could stop
	 * to be valid because the associated connection has been dropped.
	 *
	 * @return true if the transaction is valid.
	 */
	boolean isValid() {
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
					logger.warn("connection invalid, already closed.");
				}
			} catch (SQLException sqle) {
				logger.warn("cannot validate connection", sqle);
				logger.warn("Now server will try to reconnecto to MySQL");
			}
		}
		return valid;
	}

	/**
	 * Returns a helper object to access the database
	 *
	 * @return Accessor
	 */
	public Accessor getAccessor() {
		if (accessor == null) {
			accessor = new JDBCAccess(this);
		}
		return accessor;
	}

	/**
	 * closes the JDBCAccessor
	 *
	 * @throws SQLException in case of an SQL error
	 */
	private void closeAccessor() throws SQLException {
		if (accessor != null) {
			accessor.close();
		}
	}
}
