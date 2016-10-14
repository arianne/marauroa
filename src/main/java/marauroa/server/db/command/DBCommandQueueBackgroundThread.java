/***************************************************************************
 *                   (C) Copyright 2009-2013 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db.command;

import java.io.IOException;
import java.sql.SQLException;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.i18n.I18N;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.dbcommand.DBCommandWithCallback;

import org.apache.log4j.MDC;

/**
 * processes DBCommands in the background
 *
 * @author hendrik, madmetzger
 */
class DBCommandQueueBackgroundThread implements Runnable {
	private static Logger logger = Log4J.getLogger(DBCommandQueueBackgroundThread.class);

	/**
	 * the background thread
	 */
	public void run() {
		DBCommandQueue queue = DBCommandQueue.get();
		while (true) {
			DBCommandMetaData metaData = null;
			try {
				metaData = queue.getNextCommand();
			} catch (InterruptedException e) {
				logger.error(e, e);
			}

			if (metaData != null) {
				try {
					processCommand(metaData);
				} catch (RuntimeException e) {
					logger.error(e, e);
				}
			} else {
				// There are no more pending commands, check if the server is being shutdown.
				if (queue.isFinished()) {
					break;
				}
			}
		}
	}

	/**
	 * processes a command
	 *
	 * @param metaData meta data about the command to process
	 */
	private void processCommand(DBCommandMetaData metaData) {
		MDC.put("context", metaData + " ");
		if (TransactionPool.get() == null) {
			logger.warn("Database not initialized, skipping database operation");
			return;
		}

		for (int i = 0; i < 5; i++) {
			if (executeDBAction(metaData)) {
				break;
			}
			logger.warn("Retrying DBCommand " + metaData);
		}

		if (metaData.getCommand() instanceof DBCommandWithCallback) {
			DBCommandWithCallback commandWithCallback = (DBCommandWithCallback) metaData.getCommand();
			commandWithCallback.invokeCallback();
		}

		if (metaData.isResultAwaited()) {
			metaData.setProcessedTimestamp(System.currentTimeMillis());
			DBCommandQueue.get().addResult(metaData);
		}
		MDC.put("context", "");
	}

	/**
	 * executes the command
	 *
	 * @param metaData DBCommandMetaData
	 * @return true, if the command was executed (sucessfully or unsuccessfully); false if it should be tried again
	 */
	private boolean executeDBAction(DBCommandMetaData metaData) {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			I18N.setThreadLocale(metaData.getLocale());
			metaData.getCommand().execute(transaction);
			TransactionPool.get().commit(transaction);
		} catch (IOException e) {
			logger.error(e, e);
			I18N.resetThreadLocale();
			TransactionPool.get().rollback(transaction);
			metaData.getCommand().setException(e);
		} catch (SQLException e) {
			logger.error(e, e);
			I18N.resetThreadLocale();
			if (e.toString().contains("CommunicationsException") || e.toString().contains("Query execution was interrupted")) {
				TransactionPool.get().killTransaction(transaction);
				TransactionPool.get().refreshAvailableTransaction();
				return false;
			}
			TransactionPool.get().rollback(transaction);
			metaData.getCommand().setException(e);
		} catch (RuntimeException e) {
			logger.error(e, e);
			I18N.resetThreadLocale();
			TransactionPool.get().rollback(transaction);
			metaData.getCommand().setException(e);
		}
		I18N.resetThreadLocale();
		return true;
	}
}
