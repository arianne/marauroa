/* $Id: DatabaseFactory.java,v 1.2 2007/03/02 23:26:14 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.db;

import marauroa.common.Configuration;
import marauroa.common.Log4J;

/**
 * Utility class for choosing the right player databese.
 */
public class DatabaseFactory {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(DatabaseFactory.class);

	/**
	 * This method returns an instance of PlayerDatabase choosen using the
	 * Configuration file.
	 *
	 * @return A shared instance of PlayerDatabase
	 */
	public static IDatabase getDatabase() throws NoDatabaseConfException {
		try {
			Configuration conf = Configuration.getConfiguration();
			String database_type = conf.get("database_implementation");

			return getDatabase(database_type);
		} catch (Exception e) {
			logger.debug("cannot get player databese", e);
			throw new NoDatabaseConfException(e);
		}
	}

	/**
	 * This method returns an instance of PlayerDatabase choosen using the
	 * param.
	 *
	 * @param database_type
	 *            A String containing the type of database
	 * @return A shared instance of PlayerDatabase
	 */
	public static IDatabase getDatabase(String database_type)
			throws NoDatabaseConfException {
		try {
			Class databaseClass = Class.forName(database_type);
			java.lang.reflect.Method singleton = databaseClass.getDeclaredMethod("getDatabase");
			return (IDatabase) singleton.invoke(null);
		} catch (Exception e) {
			logger.error("cannot get player database", e);
			throw new NoDatabaseConfException(e);
		}
	}
}
