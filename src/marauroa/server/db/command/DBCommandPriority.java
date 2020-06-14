/***************************************************************************
 *                      (C) Copyright 2020 - Marauroa                      *
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
 * Priority of this command
 */
public enum DBCommandPriority {

	/**
	 * this command has to be executed before the next player may login
	 */
	CRITICAL(10000),

	/**
	 * this command deals with data not relevant to logins
	 */
	LOW(90000);

	private int priorityValue;

	DBCommandPriority(int priority) {
		this.priorityValue = priority;
	}

	/**
	 * gets the priority value to be used in comparisons
	 *
	 * @return priority value
	 */
	public int getPriorityValue() {
		return priorityValue;
	}

}
