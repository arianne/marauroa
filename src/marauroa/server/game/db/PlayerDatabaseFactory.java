/* $Id: PlayerDatabaseFactory.java,v 1.4 2007/02/04 12:57:00 arianne_rpg Exp $ */
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

import org.apache.log4j.Logger;

/** Utility class for choosing the right player databese. */
public class PlayerDatabaseFactory {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(PlayerDatabaseFactory.class);

	/**
	 * This method returns an instance of PlayerDatabase choosen using the
	 * Configuration file.
	 * 
	 * @return A shared instance of PlayerDatabase
	 */
	public static IDatabase getDatabase() throws NoDatabaseConfException {
		try {
			Configuration conf = Configuration.getConfiguration();
			String database_type = conf.get("marauroa_DATABASE");

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
	 * @param type
	 *            A String containing the type of database
	 * @return A shared instance of PlayerDatabase
	 */
	public static IDatabase getDatabase(String database_type)
			throws NoDatabaseConfException {
		Log4J.startMethod(logger, "getDatabase(" + database_type + ")");
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
