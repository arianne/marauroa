package marauroa.server.db.adapter;

import java.sql.SQLException;

/**
 * adapter for a database system
 *
 * @author hendrik
 */
public interface DatabaseAdapter {

	void commit() throws SQLException;

	void rollback() throws SQLException;
	
}
