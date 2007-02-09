/* $Id: RPAction.java,v 1.8 2007/02/09 15:51:46 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
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


/** This class represent an Action.
 *  Please refer to "Actions Explained" document */
public class RPAction extends Attributes {
	/** Constructor */
	public RPAction() {
		// TODO: Default RP Class
		super(null);
	}

	@Override
	public Object clone() {
		RPAction action = new RPAction();

		action.fill(this);
		return action;
	}
}
