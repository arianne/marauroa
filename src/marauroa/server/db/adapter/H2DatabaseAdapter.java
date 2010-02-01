/* $Id: H2DatabaseAdapter.java,v 1.2 2010/02/01 07:17:26 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2007-2010 - Marauroa                    *
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import marauroa.server.db.DatabaseConnectionException;

/**
 * abstracts from H2 specifica
 *
 * @author hendrik
 */
public class H2DatabaseAdapter extends AbstractDatabaseAdapter {

	/**
	 * creates a new H2Adapter
	 *
	 * @param connInfo parmaters specifying the
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public H2DatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		super(connInfo);
	}

	/**
	 * creates a new H2Adapter for test purpose without conection to the DB
	 *
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected H2DatabaseAdapter() throws DatabaseConnectionException {
		super();
	}

	/**
	 * rewrites ALTER TABLE statements to remove the "COLUMS (" part
	 *
	 * @param sql original SQL statement
	 * @return modified SQL statement
	 */
	@Override
	protected String rewriteSql(String sql) {
		String mySql = sql.trim();
		String mySqlLower = mySql.toLowerCase();
		if (mySqlLower.startsWith("alter table")) {
			int posColumn = mySqlLower.indexOf(" column");
			int posBracket = mySql.indexOf("(", posColumn);
			int posClose = mySql.lastIndexOf(")");
			mySql = mySql.substring(0, posColumn + 1) + mySql.substring(posBracket + 1, posClose) + ";";
		}
		return mySql;
	}

	@Override
	public boolean doesTableExist(String table) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getTables(null, null, table.toUpperCase(), null);
		boolean res = result.next();
		result.close();
		return res;
	}

	@Override
	public boolean doesColumnExist(String table, String column) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns("", "", table.toUpperCase(), column.toUpperCase());
		boolean res = result.next();
		result.close();
		return res;
	}

	
}
