/* $Id: SlotIsFullException.java,v 1.6 2008/02/22 10:28:32 arianne_rpg Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.common.game;

/**
 * thrown in case the slot is full
 */
public class SlotIsFullException extends RuntimeException {

	private static final long serialVersionUID = -944453052092790508L;

	/**
	 * creates a new SlotIsFullException
	 *
	 * @param slot name of slot
	 */
	public SlotIsFullException(String slot) {
		super("Slot [" + slot + "] is already full.");
	}
}
