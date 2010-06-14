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

		// 3.7.1
		if (!transaction.doesColumnExist("loginEvent", "account_link_id")) {
			transaction.execute("ALTER TABLE loginEvent ADD COLUMN (account_link_id INTEGER);", null);
		}
		if (!transaction.doesColumnExist("characters", "timedate")) {
			transaction.execute("ALTER TABLE characters ADD COLUMN (timedate TIMESTAMP default CURRENT_TIMESTAMP);", null);
		}
		if (!transaction.doesColumnExist("rpobject", "protocol_version")) {
			transaction.execute("ALTER TABLE rpobject ADD COLUMN (protocol_version INTEGER);", null);
		}
		if (!transaction.doesColumnExist("rpzone", "protocol_version")) {
			transaction.execute("ALTER TABLE rpzone ADD COLUMN (protocol_version INTEGER);", null);
		}
	}
}
