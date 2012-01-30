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

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * adapter for a database system
 *
 * @author hendrik
 */
public interface DatabaseAdapter {

	/**
	 * commits the current transaction
	 *
	 * @throws SQLException in case of an database error
	 */
	public void commit() throws SQLException;

	/**
	 * rolls the current transaction back, undoing all the changes.
	 *
	 * @throws SQLException in case of an database error
	 */
	public void rollback() throws SQLException;

	/**
	 * executes an SQL statement
	 *
	 * @param sql sql-statement to execute
	 * @return number of affected rows
	 * @throws SQLException in case of an database error
	 */
	public int execute(String sql) throws SQLException;

	/**
	 * executes an SQL statement with streamed parameters
	 *
	 * @param sql sql-statement to execute
	 * @param inputStreams  parameters
	 * @return number of affected rows
	 * @throws SQLException in case of an database error
	 * @throws IOException  in case the stream cannot be read to the end
	 */
	public int execute(String sql, InputStream... inputStreams) throws SQLException, IOException;

	/**
	 * executes a batch of sql-statements
	 *
	 * @param sql           sql-statement to execute
	 * @param inputStreams  a list of input stream. For each of them the statement is executed
	 * @throws SQLException in case of an database error
	 * @throws IOException  in case one of the streams cannot be read to the end
	 */
	public void executeBatch(String sql, InputStream... inputStreams) throws SQLException, IOException;

	/**
	 * queries the database for information
	 *
	 * @param sql sql-statement to execute
	 * @return ResultSet
	 * @throws SQLException in case of an database error
	 */
	public ResultSet query(String sql) throws SQLException;

	/**
	 * queries for a single row, single column integer response like a count-select
	 *
	 * @param sql sql-statement to execute
	 * @return integer of the first column in the first row
	 * @throws SQLException in case of an database error
	 */
	public int querySingleCellInt(String sql) throws SQLException;

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
	public int getLastInsertId(String table, String idcolumn) throws SQLException;

	/**
	 * closes the database connection
	 *
	 * @throws SQLException in case of an database error
	 */
	public void close() throws SQLException;

	/**
	 * Prepares a statement for a batch operation.
	 *
	 * @param sql   SQL statement
	 * @return PreparedStatement
	 * @throws SQLException in case of an database error
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException;

	/**
	 * checks whether the specified table exists
	 *
	 * @param table name of table
	 * @return true, if the table exists, false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean doesTableExist(String table) throws SQLException;


	/**
	 * checks whether the specified column exists
	 *
	 * @param table name of table
	 * @param column name of column
	 * @return true, if the column exists, false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean doesColumnExist(String table, String column) throws SQLException;

	/**
	 * Gets the length of the specified column
	 * @param table name of table
	 * @param column name of column
	 * @return the length of the column or -1 if no column with the given name in the given table exists
	 * @throws SQLException in case of an database error
	 */
	public int getColumnLength(String table, String column) throws SQLException;
}
