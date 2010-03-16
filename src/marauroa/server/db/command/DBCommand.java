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
	public RuntimeException getException();

	/**
	 * processes the database request.
	 */
	public void process();

}