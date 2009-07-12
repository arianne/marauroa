/* $Id: DBTransaction.java,v 1.16 2009/07/12 17:25:25 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringTokenizer;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.db.adapter.DatabaseAdapter;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * a database transaction
 *
 * @author hendrik
 */
public class DBTransaction {
    private static Logger logger = Log4J.getLogger(DBTransaction.class);

	private DatabaseAdapter databaseAdapter = null;
    private RE reInt;
    private RE reIntList;

	/**
	 * Creates a new DBTransaction.
	 *
	 * @param databaseAdapter database adapter for accessing the database
	 */
	protected DBTransaction(DatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
        try {
            reInt = new RE("^[0-9 ]*$");
            reIntList = new RE("^[0-9, ]*$");
        } catch (RESyntaxException e) {
            logger.error(e, e);
        }
	}

	/**
	 * trys to commits this transaction, in case the commit fails, a rollback is executed.
	 *
	 * @throws SQLException in case of an database error
	 */
	protected void commit() throws SQLException {
		try {
			databaseAdapter.commit();
		} catch (SQLException e) {
			databaseAdapter.rollback();
			throw e;
		}
	}

	/**
	 * rollsback this transaction
	 */
	protected void rollback() {
		try {
			databaseAdapter.rollback();
		} catch (SQLException e) {
			logger.error(e, e);
		}
	}

	/**
	 * closes the database connection
	 */
	protected void close() {
		try {
			databaseAdapter.close();
		} catch (SQLException e) {
			logger.error(e, e);
		}
	}

    /**
     * Replaces variables SQL-Statements and prevents SQL injection attacks
     *
     * @param sql SQL-String
     * @param params replacement parameters
     * @return SQL-String with substitued parameters
     * @throws SQLException in case of an sql injection attack
     */
    public String subst(String sql, Map<String, ?> params) throws SQLException {
    	if (params == null) {
    		return sql;
    	}
        StringBuffer res = new StringBuffer();
        StringTokenizer st = new StringTokenizer(sql, "([]'", true);
        String lastToken = "";
        String secondLastToken = "";
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (lastToken.equals("[")) {

                // Variablen ersetzen
                Object temp = params.get(token);
                if (temp != null) {
                    token = temp.toString();
                } else {
                    token = "";
                }

                // SQL-Injection abfangen
                if (secondLastToken.equals("(")) {
                    if (!reIntList.match(token)) {
                        throw new SQLException("Illegal argument: \"" + token + "\" is not an integer list"); 
                    }
                } else if (secondLastToken.equals("'")) {
                    if (token.length() > 0) {
                        token = StringChecker.escapeSQLString(token);
                    }
                } else {
                    if (!reInt.match(token)) {
                        throw new SQLException("Illegal argument: \"" + token + "\" is not an integer."); 
                    }
                }
            }
            secondLastToken = lastToken;
            lastToken = token.trim();
            if (token.equals("[") || token.equals("]")) {
                token = "";
            }
            res.append(token);
        }
        return res.toString();
    }

    /**
     * executes an SQL statement with parameter substituion
     *
     * @param query   SQL statement
     * @param params  parameter values
     * @return number of affected rows
     * @throws SQLException in case of an database error 
     */
	public int execute(String query, Map<String, Object> params) throws SQLException {
		String sql = subst(query, params);
		return databaseAdapter.execute(sql);
	}	

    /**
     * executes an SQL statement with parameter substituion
     *
     * @param query   SQL statement
     * @param params  parameter values
     * @param inStream input streams to stream into "?" columns
     * @return number of affected rows
     * @throws SQLException in case of an database error 
     * @throws IOException in case of an input/output error
     */
	public int execute(String query, Map<String, Object> params, InputStream... inStream) throws SQLException, IOException {
		String sql = subst(query, params);
		return databaseAdapter.execute(sql, inStream);
	}

    /**
     * queries the database
     *
     * @param query   SQL statement
     * @param params  parameter values
     * @throws SQLException in case of an database error 
     */
	public ResultSet query(String query, Map<String, Object> params) throws SQLException {
		String sql = subst(query, params);
		return databaseAdapter.query(sql);
	}

    /**
     * queries the database and returns the first column in the first row as integer (for example for a count(*)).
     *
     * @param query   SQL statement
     * @param params  parameter values
     * @throws SQLException in case of an database error 
     */
	public int querySingleCellInt(String query, Map<String, Object> params) throws SQLException {
		String sql = subst(query, params);
		return databaseAdapter.querySingleCellInt(sql);
	}

	/**
	 * gets the id of the last insert. Note: The table and idcolumn parameters
	 * <b>must</b> match the last insert statement. This is because on some
	 * database systems a SELECT IDENTITY is performaned and on other database
	 * systems a SELECT curval(table_idcolumn_seq). 
	 *  
	 * @param table  name of table on which the last insert was done
	 * @param idcolumn name autoincrement serial column of that table
	 * @return generated id
	 * @throws SQLException in case of an database error
	 */
	public int getLastInsertId(String table, String idcolumn) throws SQLException {
		return databaseAdapter.getLastInsertId(table, idcolumn);
	}

	/**
	 * Prepares a statement for a batch operation.
	 *
     * @param query   SQL statement
     * @param params  parameter values
     * @return PreparedStatement
     * @throws SQLException in case of an database error 
	 */
	public PreparedStatement prepareStatement(String query, Map<String, Object> params) throws SQLException {
		String sql = subst(query, params);
		return databaseAdapter.prepareStatement(sql);
	}
}
