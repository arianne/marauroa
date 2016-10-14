/***************************************************************************
 *                   (C) Copyright 2003-2008 - Marauroa                    *
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

/** This exception is thrown when an attribute is not found */
public class SyntaxException extends RuntimeException {

	private static final long serialVersionUID = 3724525088825394097L;

	/**
	 * creates a new SyntaxException
	 *
	 * @param offendingAttribute name of missing attribute
	 */
	public SyntaxException(String offendingAttribute) {
		super("attribute " + offendingAttribute + " isn't defined.");
	}

	/**
	 * creates a new SyntaxException
	 *
	 * @param offendingAttribute id of missing attribute
	 */
	public SyntaxException(short offendingAttribute) {
		super("attribute code " + offendingAttribute + " isn't defined.");
	}
}
