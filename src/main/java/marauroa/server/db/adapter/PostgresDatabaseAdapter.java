/***************************************************************************
 *                   (C) Copyright 2007-2016 - Marauroa                    *
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
 * abstracts from PostgreSQL specifications
 *
 * @author hendrik
 */
public class PostgresDatabaseAdapter extends AbstractDatabaseAdapter {
	private static Logger logger = Log4J.getLogger(PostgresDatabaseAdapter.class);

	/**
	 * creates a new PostgresqlDatabaseAdapter
	 *
	 * @param connInfo parameters specifying the connection
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public PostgresDatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		super(connInfo);
	}

	/**
	 * creates a new PostgresqlDatabaseAdapter for test purposes, without connection to the DB
	 *
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	protected PostgresDatabaseAdapter() throws DatabaseConnectionException {
		super();
	}

	@Override
	protected Connection createConnection(Properties connInfo) throws SQLException, DatabaseConnectionException {
		Connection con = super.createConnection(connInfo);
		DatabaseMetaData meta;
		meta = con.getMetaData();
		String name = meta.getDatabaseProductName();
		if (name.toLowerCase(Locale.ENGLISH).indexOf("postgres") < 0) {
			logger.warn("Using PostgresqlDatabaseAdapter to connect to " + name);
		}
		return con;
	}

	@Override
	public boolean doesColumnExist(String table, String column) throws SQLException {
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet result = meta.getColumns(connection.getCatalog(), "%", table.toLowerCase(Locale.ENGLISH), column.toLowerCase(Locale.ENGLISH));
		boolean res = result.next();
		result.close();
		return res;
	}
	/**
	 * checks whether the specified index exists
	 * 
	 * @param index name of index
	 * @return true, if the index exists; false otherwise
	 * @throws SQLException in case of a database error
	 */
	private boolean doesIndexExist(String index) throws SQLException {
		boolean res = false;
		String sql = "SELECT to_regclass('" + StringChecker.escapeSQLString(index) + "')";
		ResultSet rs = query(sql);
		try {
			rs.next();
			res = (rs.getString(1) != null);
		} finally {
			rs.close();
		}
		return res;
	}

	/**
	 * gets the id of the last insert. Note: The table and idcolumn parameters
	 * <b>must</b> match the last insert statement. This is because on some
	 * database systems a SELECT IDENTITY is performed and on other database
	 * systems a SELECT curval(table_idcolumn_seq).
	 *
	 * @param table  name of table on which the last insert was done
	 * @param idcolumn name autoincrement serial column of that table
	 * @return generated id
	 * @throws SQLException in case of an database error
	 */
	@Override
	public int getLastInsertId(String table, String idcolumn) throws SQLException {
		return querySingleCellInt("SELECT currval('" + StringChecker.escapeSQLString(table + "_" + idcolumn + "_seq") +  "')");
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
		if (lowerCase.startsWith("alter table")) {
			mySql = rewriteSqlAlterTable(mySql);
		} else if (lowerCase.startsWith("create table")) {
			mySql = rewriteSqlCreateTable(mySql);
		} else if (lowerCase.startsWith("create index") || lowerCase.startsWith("create unique index")) {
			mySql = rewriteSqlCreateIndex(mySql);
		}
		return mySql;
	}
	
	private String rewriteSqlAlterTable(String sql) {
		String mySql = sql;
		String mySqlLower = sql.toLowerCase(Locale.ENGLISH);
		if (mySqlLower.startsWith("alter table")) {
			int posColumn = mySqlLower.indexOf(" column");
			if (posColumn > -1) {
				int posBracket = mySql.indexOf("(", posColumn);
				int posClose = mySql.lastIndexOf(")");
				mySql = mySql.substring(0, posColumn + 1)
						+ mySql.substring(posBracket + 1, posClose) + ";";
			}
		}
		return mySql;
	}

	private String rewriteSqlCreateIndex(String sql) throws SQLException {
		CreateIndexStatementParser parser = new CreateIndexStatementParser(sql);
		boolean exists = doesIndexExist(parser.getName());
		if (exists) {
			return "";
		}
		return parser.toSqlWithoutIf();
	}

	private String rewriteSqlCreateTable(String sql) {
		String pattern = "(?i) int(?:eger)?[ ]+auto_increment";
		return sql.replaceAll(pattern, " SERIAL ")
				.replaceAll("(\\W)blob(\\W)", "$1bytea$2")
				.replaceAll("(\\W)tinyint(\\W)", "$1integer$2");
	}

}
