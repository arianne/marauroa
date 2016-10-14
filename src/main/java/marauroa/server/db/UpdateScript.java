/***************************************************************************
 *                   (C) Copyright 2007-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db;

import java.sql.SQLException;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * updates the structure of the database to the newest versoin
 *
 * @author hendrik
 */
public class UpdateScript {
	private static Logger logger = Log4J.getLogger(UpdateScript.class);

	/**
	 * updates the structure of the database
	 *
	 * @param transaction DBTransaction
	 * @throws SQLException in case of an unexpected database error
	 */
	public void update(DBTransaction transaction) throws SQLException {
		logger.info("Checking database structure and updating it if needed.");
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
			transaction.execute("UPDATE characters SET characters.timedate = (SELECT account.timedate FROM account WHERE characters.player_id=account.id);", null);
		}
		if (!transaction.doesColumnExist("rpobject", "protocol_version")) {
			transaction.execute("ALTER TABLE rpobject ADD COLUMN (protocol_version INTEGER);", null);
		}
		if (!transaction.doesColumnExist("rpzone", "protocol_version")) {
			transaction.execute("ALTER TABLE rpzone ADD COLUMN (protocol_version INTEGER);", null);
		}

		// 3.8.3
		if (!transaction.doesColumnExist("characters", "id")) {
			transaction.execute("ALTER TABLE characters DROP PRIMARY KEY;", null);
			transaction.execute("ALTER TABLE characters ADD COLUMN(id integer auto_increment not null, PRIMARY KEY(id));", null);
			transaction.execute("CREATE UNIQUE INDEX i_characters ON characters(charname);", null);
		}
		if (!transaction.doesColumnExist("characters", "status")) {
			transaction.execute("ALTER TABLE characters ADD COLUMN(status char(8) not null default 'active');", null);
			transaction.execute("UPDATE characters SET status='active' WHERE status IS NULL;", null);
		}

		// 3.8.4
		if (!transaction.doesColumnExist("accountLink", "secret")) {
			transaction.execute("ALTER TABLE accountLink ADD COLUMN (secret varchar(255));", null);
		}

		// 3.9.3
		if (transaction.doesColumnExist("account", "email")) {
			transaction.execute("INSERT INTO email (player_id, email, timedate) SELECT id, email, timedate FROM account WHERE email IS NOT NULL and email != ''", null);
			transaction.execute("ALTER TABLE account DROP COLUMN (email);", null);
		}

		logger.info("Completed database update.");
	}
}
