/***************************************************************************
 *                   (C) Copyright 2009-2010 - Marauroa                    *
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
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;

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
				int i = 0;
				while (!processCommand(metaData) && (i < 3)) {
					logger.warn("Retrying last transaction because of errors.");
					i++;
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
	 * @return completed
	 */
	private boolean processCommand(DBCommandMetaData metaData) {
		boolean completed = true;
		MDC.put("context", metaData + " ");
		if (TransactionPool.get() == null) {
			logger.warn("Database not initialized, skipping database operation");
			return completed;
		}
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			metaData.getCommand().execute(transaction);
			TransactionPool.get().commit(transaction);
		} catch (IOException e) {
			handleError(metaData, transaction, e);
			completed = false;
		} catch (SQLException e) {
			handleError(metaData, transaction, e);
			completed = false;
		} catch (RuntimeException e) {
			handleError(metaData, transaction, e);
			completed = false;
		}

		if (metaData.isResultAwaited()) {
			metaData.setProcessedTimestamp(System.currentTimeMillis());
			DBCommandQueue.get().addResult(metaData);
		}
		MDC.put("context", "");
		return completed;
	}

	private void handleError(DBCommandMetaData metaData, DBTransaction transaction, Exception e) {
		logger.error(e, e);
		TransactionPool.get().rollback(transaction);
		TransactionPool.get().verifyAllAvailableConnections();
		metaData.getCommand().setException(e);
	}
}
