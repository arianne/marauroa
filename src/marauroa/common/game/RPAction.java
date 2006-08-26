/* $Id: RPAction.java,v 1.5 2006/08/26 20:00:28 nhnb Exp $ */
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

/** This class represent an Action. Please refer to "Actions Explained" document */
public class RPAction extends Attributes {
	/** This class represent the status of the action */
	public enum Status {
		SUCCESS(0), FAIL(1), INCOMPLETE(2);

		private final int val;

		Status(int val) {
			this.val = val;
		}

		public int get() {
			return val;
		}
	};

	/** Constructor */
	public RPAction() {
		super(RPClass.getBaseRPActionDefault());
	}

	@Override
	public Object clone() {
		RPAction action = new RPAction();

		action.fill(this);
		return action;
	}
}
