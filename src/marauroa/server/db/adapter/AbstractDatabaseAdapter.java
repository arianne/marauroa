/* $Id: AbstractDatabaseAdapter.java,v 1.1 2009/07/14 21:45:30 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2007-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Properties;

import marauroa.server.db.DatabaseConnectionException;

/**
 * abstract database adapter
 *
 * @author hendrik
 */
public abstract class AbstractDatabaseAdapter implements DatabaseAdapter {
	protected Connection connection;

	protected LinkedList<Statement> statements = null;
	protected LinkedList<ResultSet> resultSets = null;

	/**
	 * creates a new AbstractDatabaseAdapter
	 *
	 * @param connInfo parmaters specifying the
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public AbstractDatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		this.connection = createConnection(connInfo);
		this.statements = new LinkedList<Statement>();
		this.resultSets = new LinkedList<ResultSet>();
	}

	/**
	 * This method creates the real connection to database.
	 *
	 * @return a cConnection to the database
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected Connection createConnection(Properties connInfo) throws DatabaseConnectionException {
		try {
			// instantiate the Driver class
			try {
				Class.forName((String) connInfo.get("jdbc_class")).newInstance();
			} catch (Exception e) {
				throw new DatabaseConnectionException("Cannot load driver class " + connInfo.get("jdbc_class"), e);
			}

			Properties connectionInfo = new Properties();
			connectionInfo.put("user", connInfo.get("jdbc_user"));
			connectionInfo.put("password", connInfo.get("jdbc_pwd"));
			connectionInfo.put("charSet", "UTF-8");

			Connection conn = DriverManager.getConnection((String) connInfo.get("jdbc_url"), connectionInfo);

			// enable transaction support
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			return conn;
		} catch (SQLException e) {
			throw new DatabaseConnectionException("Unable to create a connection to: " + connInfo.get("jdbc_url"), e);
		}
	}

	public void commit() throws SQLException {
		closeStatements();
		connection.commit();
	}

	public void rollback() throws SQLException {
		closeStatements();
		connection.rollback();
	}


	public int execute(String sql) throws SQLException {
		int res = -2;
		Statement statement = connection.createStatement();
		boolean resultType = statement.execute(sql);
		if (!resultType) {
			res = statement.getUpdateCount();
		}
		statement.close();
		return res;
	}

	public int execute(String sql, InputStream... inputStreams) throws SQLException, IOException {
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1; // yes, jdbc starts counting at 1.
		for (InputStream inputStream : inputStreams) {
			statement.setBinaryStream(i, inputStream, inputStream.available());
			i++;
		}
		int res = statement.executeUpdate();
		statement.close();
		return res;
	}

	public void executeBatch(String sql, InputStream... inputStreams) throws SQLException, IOException {
		PreparedStatement statement = connection.prepareStatement(sql);
		int i = 1; // yes, jdbc starts counting at 1.
		for (InputStream inputStream : inputStreams) {
			statement.setBinaryStream(i, inputStream, inputStream.available());
			statement.executeUpdate();
		}
		statement.close();
	}

	public ResultSet query(String sql) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		addToGarbageLists(stmt, resultSet);
		return resultSet;
	}

	public int querySingleCellInt(String sql) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		resultSet.next();
		int res = resultSet.getInt(1);
		resultSet.close();
		stmt.close();
		return res;
	}

	/**
	 * Stores open statements and resultSets in garbages lists, so that
	 * they can be closed with one single close()-method
	 *
	 * @param statement Statement
	 * @param resultSet ResultSet
	 */
	void addToGarbageLists(Statement statement, ResultSet resultSet) {
		statements.add(statement);
		resultSets.add(resultSet);
	}

	private void closeStatements() throws SQLException {
		// Note: Some JDBC drivers like Informix require resultSet.close() 
		// before statement.close() although the second one is supposed to
		// close open ResultSets by itself according to the API doc.
		for (ResultSet resultSet : resultSets) {
			resultSet.close();
		}
		for (Statement statement : statements) {
			statement.close();
		}
		resultSets.clear();
		statements.clear();
	}

	public int getLastInsertId(String table, String idcolumn) throws SQLException {
		String query = "select LAST_INSERT_ID() as inserted_id";
		return querySingleCellInt(query);
	}

	public void close() throws SQLException {
		closeStatements();
		connection.close();
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(sql);
		statements.add(statement);
		return statement;
	}
}
