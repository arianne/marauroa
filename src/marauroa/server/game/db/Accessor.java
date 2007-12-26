package marauroa.server.game.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;



public interface Accessor {

	/**
	 * Executes an SQL statement
	 *
	 * @param sql sql
	 * @return -2 in case no updated count was returned by that statement, the update count otherwise.
	 * @throws SQLException in case of an SQL error
	 */
	public abstract int execute(String sql) throws SQLException;

	/**
	 * Executes an SQL statement
	 *
	 * @param sql sql
	 * @param inputStreams data
	 * @throws SQLException in case of an SQL error
	 * @throws IOException in case of an IO error
	 */
	public abstract void execute(String sql, InputStream... inputStreams) throws SQLException, IOException;

	/**
	 * Executes an prepared SQL statement multiple times with different data
	 *
	 * @param sql sql
	 * @param inputStreams data
	 * @throws SQLException in case of an SQL error
	 * @throws IOException in case of an IO error
	 */
	public abstract void executeBatch(String sql, InputStream... inputStreams) throws SQLException, IOException;

	/**
	 * Executes a query. Note: You need to call the close() method
	 *
	 * @param sql query
	 * @return ResultSet
	 * @throws SQLException in case of an SQL error
	 */
	public abstract ResultSet query(String sql) throws SQLException;

	/**
	 * Returns the value of the first row first column which is
	 * exspected to be an int. Use this for count(*) and last_insert_id 
	 *
	 * @param sql query
	 * @return integer of first column in first row
	 * @throws SQLException in case of an SQL error
	 */
	public abstract int querySingleCellInt(String sql) throws SQLException;

	/**
	 * Clean up
	 *
	 * @throws SQLException in case of an unexpected JDBC error
	 */
	public abstract void close() throws SQLException;

}