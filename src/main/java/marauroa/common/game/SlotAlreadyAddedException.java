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
 * Thrown in case the object already has a slot with this name
 */
public class SlotAlreadyAddedException extends RuntimeException {

	private static final long serialVersionUID = 5774414964919365640L;

	/**
	 * creates a new SlotAlreadyAddedException
	 *
	 * @param slot name of preexisting slot
	 */
	public SlotAlreadyAddedException(String slot) {
		super("Slot [" + slot + "] already added.");
	}
}
