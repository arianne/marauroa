/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa;

import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.server.db.TransactionPool;

import org.junit.Test;

public class DatabaseExistsTest {

	@Test
	public void checkDatabaseExists() throws Exception {
		try {
			Log4J.init("marauroa/server/log4j.properties");

			Properties props = new Properties();

			props.put("jdbc_url", "jdbc:h2:~/marauroa/database/test;AUTO_RECONNECT=TRUE;DB_CLOSE_ON_EXIT=TRUE");
			props.put("jdbc_class", "org.h2.Driver");
			props.put("database_adapter", "marauroa.server.db.adapter.H2DatabaseAdapter");

			TransactionPool pool = new TransactionPool(props);
			pool.rollback(pool.beginWork());
			pool.close();
		} catch (Exception e) {
			throw new Exception("Database is not accessible. Please check \"marauroatest\" is created and that user \"junittest\" with password \"passwd\" can access it.", e);
		}
	}
}
