/* $Id: NoDatabaseConfException.java,v 1.7 2009/07/11 11:52:44 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
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

/**
 * This exception is thrown when database is not configured correctly.
 *
 * @author miguel
 *
 */
public class NoDatabaseConfException extends IllegalStateException {

	private static final long serialVersionUID = -4145441757361358659L;

	/**
	 * Constructor
	 *
	 * @param cause
	 *            exception that generated this one.
	 */
	public NoDatabaseConfException(Throwable cause) {
		super("Database configuration file not found.", cause);
	}

	/** Constructor */
	public NoDatabaseConfException() {
		super("Database configuration file not found.");
	}
}
