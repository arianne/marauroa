/* $Id: MySQLDatabaseAdapter.java,v 1.11 2010/01/31 20:36:13 nhnb Exp $ */
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

import java.util.Properties;

import marauroa.server.db.DatabaseConnectionException;

/**
 * abstracts from MySQL specifica
 *
 * @author hendrik
 */
public class MySQLDatabaseAdapter extends AbstractDatabaseAdapter {

	/**
	 * creates a new MySQLDatabaseAdapter
	 *
	 * @param connInfo parmaters specifying the
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public MySQLDatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		super(connInfo);
	}

	/**
	 * creates a new MySQLDatabaseAdapter for test purpose without conection to the DB
	 *
	 * @param connInfo parmaters specifying the
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected MySQLDatabaseAdapter() throws DatabaseConnectionException {
		super();
	}

	/**
	 * rewrites CREATE TABLE statements to add TYPE=InnoDB
	 *
	 * @param sql original SQL statement
	 * @return modified SQL statement
	 */
	@Override
	protected String rewriteSql(String sql) {
		String mySql = sql.trim();
		if (mySql.toLowerCase().startsWith("create table")) {
			mySql = sql.substring(0, sql.length() - 1) + " TYPE=InnoDB;";
		}
		return mySql;
	}

}
