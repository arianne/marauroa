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
	
}
