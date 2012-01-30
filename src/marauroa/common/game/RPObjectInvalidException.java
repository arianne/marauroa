/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.game;

/**
 * invalid rpobject (required attribute is missing).
 */
public class RPObjectInvalidException extends RuntimeException {

	private static final long serialVersionUID = 2413566633754598291L;

	/**
	 * creates a new RPObjectInvalidException
	 *
	 * @param attribute name of missing attribute
	 */
	public RPObjectInvalidException(String attribute) {
		super("Object is invalid: It lacks of mandatory attribute [" + attribute + "]");
	}

	/**
	 * creates a new RPObjectInvalidException
	 *
	 * @param cause the root exception
	 */
	public RPObjectInvalidException(Throwable cause) {
		super("Object is invalid", cause);
	}
}
