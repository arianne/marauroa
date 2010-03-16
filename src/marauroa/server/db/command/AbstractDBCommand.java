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
 * An abstract asynchronous database command.
 *
 * @author hendrik, madmetzger
 */
public abstract class AbstractDBCommand implements DBCommand {
	private static Logger logger = Log4J.getLogger(AbstractDBCommand.class);

	private RuntimeException exception = null;

	/**
	 * gets the exception in case one was thrown during processing.
	 *
	 * @return RuntimeException or <code>null</code> in case no exception was thrown.
	 */
	public RuntimeException getException() {
		return exception;
	}

	/**
	 * processes the database request.
	 */
	public void process() {
		try {
			execute();
		} catch (RuntimeException e) {
			logger.error(e, e);
			exception = e;
		}
	}

	
	/**
	 * processes the database request.
	 */
	protected abstract void execute();
}
