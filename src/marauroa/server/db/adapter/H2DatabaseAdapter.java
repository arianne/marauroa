/***************************************************************************
 *                   (C) Copyright 2007-2011 - Marauroa                    *
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

/**
 * abstracts from H2 specifications
 *
 * @author hendrik
 */
public class H2DatabaseAdapter extends AbstractDatabaseAdapter {
	private static Logger logger = Log4J.getLogger(H2DatabaseAdapter.class);

	/**
	 * creates a new H2Adapter
	 *
	 * @param connInfo parameters specifying the connection
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public H2DatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		super(connInfo);
	}

	/**
	 * creates a new H2Adapter for test purposes, without connection to the DB
	 *
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected H2DatabaseAdapter() throws DatabaseConnectionException {
		super();
	}

	@Override
	protected Connection createConnection(Properties connInfo) throws DatabaseConnectionException {
		Connection con = super.createConnection(connInfo);
		DatabaseMetaData meta;
		try {
			meta = con.getMetaData();
			String name = meta.getDatabaseProductName();
			if (name.toLowerCase(Locale.ENGLISH).indexOf("h2") < 0) {
				logger.warn("Using H2DatabaseAdapter to connect to " + name, new Throwable());
			}
			if (connInfo.getProperty("jdbc_url", "").toLowerCase(Locale.ENGLISH).indexOf(";mode=") > -1) {
				logger.warn("The configuration parameter jdbc_url configures H2 for compatibility mode. This is likely to cause trouble.");
			}
		} catch (SQLException e) {
			logger.error(e, e);
		}
		return con;
	}

	/**
	 * rewrites ALTER TABLE statements to remove the "COLUMNS (" part
	 *
	 * @param sql original SQL statement
	 * @return modified SQL statement
	 */
	@Override
	protected String rewriteSql(String sql) {
		String mySql = sql.trim();
		String mySqlLower = mySql.toLowerCase(Locale.ENGLISH);
		if (mySqlLower.startsWith("alter table")) {
			int posColumn = mySqlLower.indexOf(" column");
			if (posColumn > -1) {
				int posBracket = mySql.indexOf("(", posColumn);
				int posClose = mySql.lastIndexOf(")");
				mySql = mySql.substring(0, posColumn + 1)
						+ mySql.substring(posBracket + 1, posClose) + ";";
				mySql = mySql.replace(", PRIMARY KEY(id)", "");
			}
		}
		return mySql;
	}

	@Override
	public boolean doesTableExist(String table) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getTables(null, null, table.toUpperCase(Locale.ENGLISH), null);
		boolean res = result.next();
		result.close();
		return res;
	}

	@Override
	public boolean doesColumnExist(String table, String column) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(null, null, table.toUpperCase(),
				column.toUpperCase(Locale.ENGLISH));
		boolean res = result.next();
		result.close();
		return res;
	}

	@Override
	public int getColumnLength(String table, String column) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(null, null, table.toUpperCase(Locale.ENGLISH),
				column.toUpperCase(Locale.ENGLISH));
		if (result.next()) {
			return result.getInt("COLUMN_SIZE");
		}
		return -1;
	}

}
