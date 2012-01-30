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

/**
 * thrown in case an expected rpobject is not found
 */
public class RPObjectNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4649823545471552977L;

	/**
	 * creates a new RPObjectNotFoundException
	 *
	 * @param id id of expected rpobject
	 */
	public RPObjectNotFoundException(RPObject.ID id) {
		super("RP Object [" + id + "] not found");
	}
}
