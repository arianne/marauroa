/***************************************************************************
 *                   (C) Copyright 2007-2007 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.rp;

/**
 * This exception is thrown when the RPObject factory can't be created, usually
 * because is not configured correctly at server.ini
 *
 * @author miguel
 *
 */
public class NoFactoryConfException extends IllegalStateException {

	private static final long serialVersionUID = -4145441757361358659L;

	/**
	 * Constructor
	 *
	 * @param cause
	 *            exception that generated this one.
	 */
	public NoFactoryConfException(Throwable cause) {
		super("RPObject Factory configuration file not found.", cause);
	}

	/** Constructor */
	public NoFactoryConfException() {
		super("RPObject Factory configuration file not found.");
	}
}