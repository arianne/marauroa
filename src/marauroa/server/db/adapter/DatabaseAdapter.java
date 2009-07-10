package marauroa.server.db.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * adapter for a database system
 *
 * @author hendrik
 */
public interface DatabaseAdapter {

	void commit() throws SQLException;

	void rollback() throws SQLException;

	public int execute(String sql) throws SQLException;

	public void execute(String sql, InputStream... inputStreams) throws SQLException, IOException;

	public void executeBatch(String sql, InputStream... inputStreams) throws SQLException, IOException;

	public ResultSet query(String sql) throws SQLException;

	public int querySingleCellInt(String sql) throws SQLException;

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
	public int getLastInsertId(String table, String idcolumn) throws SQLException;

	/**
	 * closes the database connection
	 *
	 * @throws SQLException in case of an database error
	 */
	public void close() throws SQLException;
	
}
