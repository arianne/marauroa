package marauroa.server.db.adapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import marauroa.server.db.DatabaseConnectionException;

/**
 * abstracts from MySQL specifica
 *
 * @author hendrik
 */
public class MySQLDatabaseAdapter implements DatabaseAdapter {
	private Connection connection;

	/**
	 * creates a new MySQLDatabaseAdapter
	 *
	 * @param connInfo parmaters specifying the
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public MySQLDatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		this.connection = createConnection(connInfo);
	}

	/**
	 * This method creates the real connection to database.
	 *
	 * @return a cConnection to the database
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	private Connection createConnection(Properties connInfo) throws DatabaseConnectionException {
		try {
			// We instantiate now the Driver class
			try {
				Class.forName((String) connInfo.get("jdbc_class")).newInstance();
			} catch (Exception e) {
				throw new DatabaseConnectionException("Cannot load driver class " + connInfo.get("jdbc_class"), e);
			}

			Properties connectionInfo = new Properties();
			connectionInfo.put("user", connInfo.get("jdbc_user"));
			connectionInfo.put("password", connInfo.get("jdbc_pwd"));
			connectionInfo.put("charSet", "UTF-8");

			Connection conn = DriverManager.getConnection((String) connInfo.get("jdbc_url"), connectionInfo);

			// enable transaction support
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			return conn;
		} catch (SQLException e) {
			throw new DatabaseConnectionException("Unable to create a connection to: " + connInfo.get("jdbc_url"), e);
		}
	}
}
