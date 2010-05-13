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
import java.sql.Timestamp;

import marauroa.server.db.DBTransaction;

/**
 * An abstract asynchronous database command.
 *
 * @author hendrik, madmetzger
 */
public abstract class AbstractDBCommand implements DBCommand {
	private Timestamp enqueueTime = null;
	private Exception exception = null;

	/**
	 * gets the exception in case one was thrown during processing.
	 *
	 * @return RuntimeException or <code>null</code> in case no exception was thrown.
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * gets the timestamp when this command was added to the queue
	 *
	 * @return Timestamp
	 */
	public Timestamp getEnqueueTime() {
		return enqueueTime;
	}

	/**
	 * remembers an exception
	 *
	 * @param exception RuntimeException
	 */
	public void setException(Exception exception) {
		this.exception = exception;
	}

	/**
	 * sets the timestamp when this command was added to the queue
	 *
	 * @param enqueueTime Timestamp
	 */
	public void setEnqueueTime(Timestamp enqueueTime) {
		this.enqueueTime = enqueueTime;
	}

	/**
	 * processes the database request.
	 *
	 * @param transaction DBTransaction
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public abstract void execute(DBTransaction transaction) throws SQLException, IOException;

}
