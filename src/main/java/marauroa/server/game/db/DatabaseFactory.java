/***************************************************************************
 *                   (C) Copyright 2003-2013 - Marauroa                    *
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.DatabaseConnectionException;
import marauroa.server.db.JDBCSQLHelper;
import marauroa.server.db.TransactionPool;
import marauroa.server.db.UpdateScript;

/**
 * Utility class for choosing the right player database.
 */
public class DatabaseFactory {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(DatabaseFactory.class);

	/**
	 * initializes the database system for tests
	 */
	public void initializeTestDatabase() {
		Properties props = new Properties();
		props.put("jdbc_url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;AUTO_RECONNECT=TRUE;DB_CLOSE_ON_EXIT=TRUE");
		props.put("jdbc_class", "org.h2.Driver");
		props.put("database_adapter", "marauroa.server.db.adapter.H2DatabaseAdapter");

		internalInit(props);
	}

	/**
	 * initializes the database system
	 *
	 * @throws DatabaseConnectionException in case the database configuration is broken
	 */
	public void initializeDatabase() throws DatabaseConnectionException {
		try {
			internalInit(Configuration.getConfiguration().getAsProperties());
		} catch (IOException e) {
			logger.error("Failed to load configuration for database", e);
			throw new DatabaseConnectionException(e);
		}
	}

	private void internalInit(Properties configuration) {
		try {
			if (TransactionPool.get() == null) {
				TransactionPool pool = new TransactionPool(configuration);
				pool.registerGlobally();
				initializeDatabaseSchema();
				DAORegister.get();
				configureGameDatabaseAccess();
			}
		} catch (Exception e) {
			logger.error("Failed to initialize database", e);
			throw new DatabaseConnectionException(e);
		}
	}

	private void initializeDatabaseSchema() {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			new JDBCSQLHelper(transaction).runDBScript("marauroa/server/marauroa_init.sql");
			new UpdateScript().update(transaction);
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			logger.error(e, e);
			TransactionPool.get().rollback(transaction);
		}
	}

	private void configureGameDatabaseAccess() {
		try {
			Configuration conf = Configuration.getConfiguration();
			String database_type = conf.get("database_implementation");

			// compatibility: Ignore old JDBCDatabase entry and invoke initialize()
			// using reflecting because old database factories are not required
			// to extend this class.
			if ((database_type != null) && (!database_type.equals("marauroa.server.game.db.JDBCDatabase"))) {
				Class<?> databaseClass = Class.forName(database_type);
				java.lang.reflect.Method method = databaseClass.getDeclaredMethod("initialize");
				method.invoke(databaseClass.newInstance());
			}
		} catch (Exception e) {
			logger.error("error initializing game database", e);
		}
	}
}
