/* $Id: DatabaseFactory.java,v 1.11 2009/07/11 11:52:44 nhnb Exp $ */
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
package marauroa.server.game.db;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.server.db.DatabaseConnectionException;
import marauroa.server.db.TransactionPool;

/**
 * Utility class for choosing the right player database.
 */
public class DatabaseFactory {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(DatabaseFactory.class);


	/**
	 * This method returns an instance of PlayerDatabase chosen using the
	 * param.
	 *
	 * @return A shared instance of PlayerDatabase
	 * @throws DatabaseConnectionException in case the database configuration is broken
	 */
	public void initializeDatabase() throws DatabaseConnectionException {
		try {
			if (TransactionPool.get() == null) {
				TransactionPool pool = new TransactionPool(Configuration.getConfiguration().getAsProperties());
				pool.registerGlobally();
			}
			DAORegister.get();
		} catch (Exception e) {
			logger.error("cannot get player database", e);
			throw new DatabaseConnectionException(e);
		}
	}
}
