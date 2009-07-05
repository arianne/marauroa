package marauroa.server.db;

import java.sql.SQLException;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.db.adapter.DatabaseAdapter;

/**
 * a database transaction
 *
 * @author hendrik
 */
public class DBTransaction {
    private static Logger logger = Log4J.getLogger(TransactionPool.class);

	private DatabaseAdapter databaseAdapter = null;

	/**
	 * creates a new DBTransaction
	 *
	 * @param databaseAdapter database adapter for accessing the database
	 */
	protected DBTransaction(DatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
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
}
