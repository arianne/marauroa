/***************************************************************************
 *                   (C) Copyright 2007-2015 - Marauroa                    *
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.db.DatabaseConnectionException;
import marauroa.server.db.StringChecker;

/**
 * abstracts from MySQL specifications
 *
 * @author hendrik
 */
public class MySQLDatabaseAdapter extends AbstractDatabaseAdapter {
	private static Logger logger = Log4J.getLogger(MySQLDatabaseAdapter.class);

	// major version of the database
	private int majorVersion;

	/**
	 * creates a new MySQLDatabaseAdapter
	 *
	 * @param connInfo parameters specifying the connection
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public MySQLDatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		super(connInfo);
	}

	/**
	 * creates a new MySQLDatabaseAdapter for test purposes, without connection to the DB
	 *
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected MySQLDatabaseAdapter() throws DatabaseConnectionException {
		super();
	}

	@Override
	protected Connection createConnection(Properties connInfo) throws SQLException, DatabaseConnectionException {
		try {
			Connection con = super.createConnection(connInfo);
			DatabaseMetaData meta;
			meta = con.getMetaData();
			String name = meta.getDatabaseProductName();
			if (name.toLowerCase(Locale.ENGLISH).indexOf("mysql") < 0) {
				logger.warn("Using MySQLDatabaseAdapter to connect to " + name);
			}
			this.majorVersion = con.getMetaData().getDatabaseMajorVersion();
			return con;
		} catch (SQLException e) {

			// Shorten extremely long MySql error message, which contains the cause-Exception in getMessage() instead of getCause()
			String msg = e.toString();
			if (msg.contains("CommunicationsException")) {
				int pos = msg.indexOf("BEGIN NESTED EXCEPTION");
				if (pos > -1) {
					throw new DatabaseConnectionException(msg.substring(0, pos - 3).trim());
				}
			}
			throw e;
		}
	}

	/**
	 * checks whether the specified table is a real table and not a view
	 * 
	 * @param table name of table
	 * @return true, if the table is a base table; false otherwise
	 * @throws SQLException in case of a database error
	 */
	private boolean isBaseTable(String table) throws SQLException {
		boolean res = false;
		String sql = "show full tables LIKE '" + StringChecker.escapeSQLString(table) + "'";

		ResultSet result = query(sql);
		try {
			res = result.next();
			if (res) {
				res = "BASE TABLE".equals(result.getString(2));
			}
		} finally {
			result.close();
		}
		return res;
	}

	/**
	 * checks whether the specified index exists
	 * 
	 * @param table name of table
	 * @param index name of index
	 * @return true, if the index exists; false otherwise
	 * @throws SQLException in case of a database error
	 */
	private boolean doesIndexExist(String table, String index) throws SQLException {
		String sql = "SELECT count(1) FROM information_schema.statistics"
				+ " WHERE table_schema = database()"
				+ " AND table_name = '" + StringChecker.escapeSQLString(table) + "'"
				+ " AND index_name = '" + StringChecker.escapeSQLString(index) + "'";
		
		return querySingleCellInt(sql) > 0;
	}

	/**
	 * rewrites CREATE TABLE statements to add TYPE=InnoDB
	 *
	 * @param sql original SQL statement
	 * @return modified SQL statement
	 * @throws SQLException in case of a database error
	 */
	@Override
	protected String rewriteSql(String sql) throws SQLException {
		String mySql = sql.trim();
		String lowerCase = mySql.toLowerCase(Locale.ENGLISH);
		if (lowerCase.startsWith("create table")) {
			mySql = rewriteSqlCreateTable(mySql);
		} else if (lowerCase.startsWith("create index") || lowerCase.startsWith("create unique index")) {
			mySql = rewriteSqlCreateIndex(mySql);
		}
		return mySql;
	}

	private String rewriteSqlCreateIndex(String sql) throws SQLException {
		CreateIndexStatementParser parser = new CreateIndexStatementParser(sql);
		boolean skip = !isBaseTable(parser.getTable()) || doesIndexExist(parser.getTable(), parser.getName());
		if (skip) {
			return "";
		}
		return parser.toSqlWithoutIf();
	}

	private String rewriteSqlCreateTable(String sql) {
		String mySql;
		if (this.majorVersion >= 5) {
			mySql = sql.substring(0, sql.length() - 1) + " ENGINE=InnoDB;";
		} else {
			mySql = sql.substring(0, sql.length() - 1) + " TYPE=InnoDB;";
		}
		return mySql;
	}

}
