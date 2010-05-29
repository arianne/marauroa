/* $Id: MySQLDatabaseAdapter.java,v 1.14 2010/05/29 21:30:11 nhnb Exp $ */
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.db.DatabaseConnectionException;

/**
 * abstracts from MySQL specifica
 *
 * @author hendrik
 */
public class MySQLDatabaseAdapter extends AbstractDatabaseAdapter {
    private static Logger logger = Log4J.getLogger(MySQLDatabaseAdapter.class);

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
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected MySQLDatabaseAdapter() throws DatabaseConnectionException {
		super();
	}

	@Override
    protected Connection createConnection(Properties connInfo) throws DatabaseConnectionException {
	    Connection con = super.createConnection(connInfo);
		DatabaseMetaData meta;
        try {
	        meta = con.getMetaData();
			String name = meta.getDatabaseProductName();
		    if (name.toLowerCase(Locale.ENGLISH).indexOf("mysql") < 0) {
		    	logger.warn("Using MySQLDatabaseAdapter to connect to " + name);
		    }
        } catch (SQLException e) {
	        logger.error(e, e);
        }
	    return con;
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
		if (mySql.toLowerCase(Locale.ENGLISH).startsWith("create table")) {
			mySql = sql.substring(0, sql.length() - 1) + " TYPE=InnoDB;";
		}
		return mySql;
	}

}
