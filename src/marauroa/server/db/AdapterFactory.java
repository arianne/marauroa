package marauroa.server.db;

import java.util.Properties;

import marauroa.server.db.adapter.DatabaseAdapter;
import marauroa.server.db.adapter.MySQLDatabaseAdapter;

/**
 * creates DatabaseAdapters for the configured database system
 *
 * @author hendrik
 */
class AdapterFactory {

	private Properties connInfo;

	/**
	 * creates a new AdapterFactory
	 *
	 * @param connInfo
	 */
	public AdapterFactory(Properties connInfo) {
		this.connInfo = connInfo;
	}

	/**
	 * creates a DatabaseAdapter
	 *
	 * @return DatabaseAdapter for the specified database
	 */
	public DatabaseAdapter create() {
		// TODO: make this configureable
		return new MySQLDatabaseAdapter(connInfo);
	}
}
