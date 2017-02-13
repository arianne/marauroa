/***************************************************************************
 *                   (C) Copyright 2007-2012 - Marauroa                    *
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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
	private boolean gotAConnection = false;

	/**
	 * creates a new AdapterFactory
	 *
	 * @param connInfo
	 */
	public AdapterFactory(Properties connInfo) {
		this.connInfo = connInfo;
	}

	/**
	 * creates a database adapter
	 *
	 * @return database adapter
	 * @throws IOException             in case of an unexpected input/output error
	 * @throws ClassNotFoundException  if the adapter class is missing
	 * @throws SecurityException       if a security manager prevented the operation
	 * @throws NoSuchMethodException   if the constructor with a Properties object as parameter is missing
	 * @throws IllegalArgumentException on an internal error
	 * @throws InstantiationException   if the adapter class may not be instantiated
	 * @throws IllegalAccessException   on an internal error
	 * @throws InvocationTargetException if the constructor threw an exception
	 */
	private DatabaseAdapter internalCreate() throws IOException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		String adapter = connInfo.getProperty("database_adapter");
		if (adapter == null) {
			return new MySQLDatabaseAdapter(connInfo);
		}
		Class<? extends DatabaseAdapter> clazz = Class.forName(adapter).asSubclass(DatabaseAdapter.class);
		Constructor<? extends DatabaseAdapter> ctor = clazz.getConstructor(Properties.class);
		return ctor.newInstance(connInfo);
	}

	/**
	 * creates a DatabaseAdapter
	 *
	 * @return DatabaseAdapter for the specified database
	 */
    public DatabaseAdapter create() {
		int maxRetries = Integer.parseInt(connInfo.getProperty("database_connection_retries", "2000000000"));
		int wait = Integer.parseInt(connInfo.getProperty("database_connection_waittime", "1000"));

		int i = 0;
		while (true) {
			try {
				DatabaseAdapter adapter = internalCreate();
				gotAConnection = true;
				return adapter;
			} catch (Exception e) {
				if (gotAConnection && (i < maxRetries)) {
					logger.warn("Reconnect failed, will try again soon: " + e);
					i++;
					try {
						Thread.sleep(wait);
					} catch (InterruptedException e1) {
						logger.error(e1, e1);
					}
				} else {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
