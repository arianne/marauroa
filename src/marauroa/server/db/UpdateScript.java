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
		if (!transaction.doesColumnExist("loginEvent", "service")) {
			transaction.execute("ALTER TABLE loginEvent ADD COLUMN (service CHAR(10));", null);
		}
		if (!transaction.doesColumnExist("loginEvent", "seed")) {
			transaction.execute("ALTER TABLE loginEvent ADD COLUMN (seed VARCHAR(120));", null);
		}
		if (!transaction.doesColumnExist("passwordChange", "result")) {
			transaction.execute("ALTER TABLE passwordChange ADD COLUMN (result TINYINT);", null);
			transaction.execute("UPDATE passwordChange SET result=1 WHERE result IS NULL", null);
		}

		// 3.5.2
		if (!transaction.doesColumnExist("statistics", "ips_online")) {
			transaction.execute("ALTER TABLE statistics ADD COLUMN (ips_online INTEGER);", null);
		}

	}
}
