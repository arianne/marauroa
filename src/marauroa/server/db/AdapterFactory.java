/***************************************************************************
 *                   (C) Copyright 2007-2010 - Marauroa                    *
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

import java.lang.reflect.Constructor;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.db.adapter.DatabaseAdapter;
import marauroa.server.db.adapter.MySQLDatabaseAdapter;

/**
 * creates DatabaseAdapters for the configured database system
 *
 * @author hendrik
 */
class AdapterFactory {
	private static Logger logger = Log4J.getLogger(AdapterFactory.class);

	private final Properties connInfo;

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
	@SuppressWarnings("unchecked")
    public DatabaseAdapter create() {
		try {
			String adapter = connInfo.getProperty("database_adapter");
			if (adapter == null) {
				return new MySQLDatabaseAdapter(connInfo);
			}
			Class<DatabaseAdapter> clazz= (Class<DatabaseAdapter>) Class.forName(adapter);
			Constructor<DatabaseAdapter> ctor = clazz.getConstructor(Properties.class);
			return ctor.newInstance(connInfo);
		} catch (Exception e) {
			logger.error(e, e);
			return null;
		}
	}
}
