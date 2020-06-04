/***************************************************************************
 *                     (C) Copyright 2020 - Marauroa                       *
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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import marauroa.common.Configuration;

/**
 * logging of database commands with timestamps
 */
public class DBCommandQueueLogger {
	private static Logger logger = Logger.getLogger(DBCommandQueueLogger.class);
	private BufferedWriter writer = null;
	private static DBCommandQueueLogger instance;

	DBCommandQueueLogger() {
		instance = this;
	}

	/**
	 * gets the command queue
	 *
	 * @return DBCommandQueueLogger
	 */
	public static DBCommandQueueLogger get() {
		return instance;
	}

	void log(DBCommandMetaData metaData, long startTime, long dbDoneTime, long callbackDoneTime) {
		// do a quick check on whether the writer is defined before entering the synchronized block
		if (writer != null) {
			logInernal(metaData, startTime, dbDoneTime, callbackDoneTime);
		}
	}

	/**
	 * logs a database command
	 *
	 * @param metaData command
	 * @param startTime start time
	 * @param dbDoneTime db actions execution completed
	 * @param callbackDoneTime callback execution completed
	 */
	synchronized void logInernal(DBCommandMetaData metaData, long startTime, long dbDoneTime, long callbackDoneTime) {
		if (writer != null) {
			try {
				writer.append(startTime + "\t" + (dbDoneTime - startTime) + "\t" + (callbackDoneTime - dbDoneTime) + "\t" + metaData + "\n");
			} catch (IOException e) {
				logger.error(e, e);
			}
		}
	}


	/**
	 * stop logging
	 */
	public synchronized void stopLogging() {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				logger.error(e, e);
			}
			writer = null;
		}
	}

	/**
	 * start logging
	 *
	 * @param filename filename to log to
	 */
	public synchronized void startLogging(String filename) {
		stopLogging();
		try {
			writer = new BufferedWriter(new FileWriter(filename));
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	private String generateLoggingFilename(String prefix) {
		String loggingFolder;
		try {
			loggingFolder = Configuration.getConfiguration().get("logging_folder");
		} catch (IOException e) {
			logger.error(e, e);
			return null;
		}
		if (loggingFolder == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH);
        String filename = loggingFolder + "/" + prefix + "-" + format.format(new Date()) + ".txt";
        return filename;
	}


	/**
	 * logs the complete queue
	 *
	 * @param queue queue
	 */
	public void logQueueSize(DBCommandQueue queue) {
		String filename = generateLoggingFilename("large-db-queue");
		if (filename == null) {
			return;
		}

		DBCommandMetaData[] content = queue.dumpQueue();
		BufferedWriter sizeWriter;
		try {
			sizeWriter = new BufferedWriter(new FileWriter(filename));
			for (DBCommandMetaData commandMetaData : content) {
				sizeWriter.append(commandMetaData + "\n");
			}
			sizeWriter.close();
		} catch (IOException e) {
			logger.error(e, e);
		}
	}
}