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
 * a database command that can be processed asynchronously.
 *
 * @author hendrik, madmetzger
 */
public interface DBCommand {

	/**
	 * gets the exception in case one was thrown during processing.
	 *
	 * @return RuntimeException or <code>null</code> in case no exception was thrown.
	 */
	public Exception getException();

	/**
	 * processes the database request.
	 *
	 * @param transaction DBTransaction
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public void execute(DBTransaction transaction) throws SQLException, IOException;

	/**
	 * gets the timestamp when this command was added to the queue
	 *
	 * @return Timestamp
	 */
	public Timestamp getEnqueueTime();

	/**
	 * remembers an exception
	 *
	 * @param exception RuntimeException
	 */
	public void setException(Exception exception);

	/**
	 * sets the timestamp when this command was added to the queue
	 *
	 * @param enqueueTime Timestamp
	 */
	public void setEnqueueTime(Timestamp enqueueTime);

}