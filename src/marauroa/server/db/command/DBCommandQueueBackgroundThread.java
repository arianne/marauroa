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

import marauroa.common.Log4J;
import marauroa.common.Logger;

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
				processCommand(metaData);

				if (metaData.isResultAwaited()) {
					metaData.setProcessedTimestamp(System.currentTimeMillis());
					queue.addResult(metaData);
				}
			}
		}
		// TODO: exit thread on exit of the jvm
	}

	/**
	 * processes a command
	 *
	 * @param metaData meta data about the command to process
	 */
	private void processCommand(DBCommandMetaData metaData) {
		try {
			metaData.getCommand().execute();
		} catch (RuntimeException e) {
			logger.error(e, e);
			metaData.getCommand().setException(e);
		}
	}

}
