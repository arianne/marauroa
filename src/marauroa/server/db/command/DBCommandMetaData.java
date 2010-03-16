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

import java.sql.Timestamp;
import java.util.Date;

/**
 * stores meta information about a command
 *
 * @author hendrik, madmetzger
 */
class DBCommandMetaData {

	private DBCommand command;
	private Thread requestingThread;
	private boolean awaitResult;
	private long processedTimestamp = -1;

	/**
	 * creates a new DBCommandMetaData object
	 *
	 * @param command DBCommand
	 * @param requestingThread the thread requesting the execution of the DBCommand
	 * @param awaitResult does the thread want a result back?
	 */
	public DBCommandMetaData(DBCommand command, Thread requestingThread, boolean awaitResult) {
		this.command = command;
		this.requestingThread = requestingThread;
		this.awaitResult = awaitResult;
		command.setEnqueueTime(new Timestamp(new Date().getTime()));
	}

	/**
	 * gets the command
	 *
	 * @return DBCommand
	 */
	public DBCommand getCommand() {
		return command;
	}

	/**
	 * gets the requesting Thread
	 *
	 * @return requestingThread
	 */
	public Thread getRequestingThread() {
		return requestingThread;
	}

	/**
	 * is the result awaited?
	 *
	 * @return true, if the result is awaited; false if it should be thrown away
	 */
	public boolean isResultAwaited() {
		return awaitResult;
	}

	/**
	 * gets the timestamp when the command was processed.
	 *
	 * @return timestamp, -1 indicated that the command was not processed, yet.
	 */
	public long getProcessedTimestamp() {
		return processedTimestamp;
	}

	/**
	 * sets the timestamp when the command was processed.
	 *
	 * @param timestamp timestamp
	 */
	public void setProcessedTimestamp(long processedTimestamp) {
		this.processedTimestamp = processedTimestamp;
	}

}
