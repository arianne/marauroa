package marauroa.server.db;

import java.sql.SQLException;

/**
 * updates the structure of the database to the newest versoin
 *
 * @author hendrik
 */
public class UpdateScript {

	/**
	 * updates the structure of the database
	 *
	 * @param transaction DBTransaction
	 * @throws SQLException in case of an unexpected database error
	 */
	public void update(DBTransaction transaction) throws SQLException {
		if (!transaction.doesColumnExist("loginEvent", "seed")) {
			transaction.execute("ALTER TABLE loginEvent ADD COLUMN (seed VARCHAR(120));", null);
		}
	}
}
