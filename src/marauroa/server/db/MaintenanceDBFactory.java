/***************************************************************************
 *                   (C) Copyright 2021 - Faiumoni e. V.                   *
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

import java.util.Properties;

import marauroa.server.db.adapter.DatabaseAdapter;

/**
 * direct access to DBTransactions on arbitrary databases.
 * 
 * @author hendrik
 */
public class MaintenanceDBFactory {

	/**
	 * creates a DBTransaction for the specified database connection
	 *
	 * @param conf configuration for database connection
	 * @return DBTransaction
	 */
	public DBTransaction create(Properties conf) {
		DatabaseAdapter adapter = new AdapterFactory(conf).create();
		DBTransaction dbtransaction = new DBTransaction(adapter);
		dbtransaction.setThread(Thread.currentThread());
		return dbtransaction;
	}
}
