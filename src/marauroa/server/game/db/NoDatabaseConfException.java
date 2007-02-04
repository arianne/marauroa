/* $Id: NoDatabaseConfException.java,v 1.3 2007/02/04 12:57:00 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
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
 * @author miguel
 *
 */
public class NoDatabaseConfException extends IllegalStateException {
	private static final long serialVersionUID = -4145441757361358659L;

	public NoDatabaseConfException(Throwable cause) {
		super("Database configuration file not found.", cause);
	}

	public NoDatabaseConfException() {
		super("Database configuration file not found.");
	}
}
