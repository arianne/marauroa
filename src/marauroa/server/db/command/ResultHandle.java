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
 * identifies the results belonging to the code doing the request
 *
 * @author hendrik
 */
public final class ResultHandle {

	private String random;

	/**
	 * creates a new result handle
	 */
	public ResultHandle() {
		random = Thread.currentThread().getName() + "-" + System.currentTimeMillis() + "-" + Math.random();
	}

	@Override
	public String toString() {
		return "ResultHandle [" + random + "]";
	}

}
