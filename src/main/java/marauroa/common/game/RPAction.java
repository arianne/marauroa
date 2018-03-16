/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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
 * This class represent an Action.
 * <p>
 * To express the willingness of a client to do something it must send the
 * server a MessageC2SAction message.<br>
 * An action is composed of several attributes. (an attribute is similar to a
 * variable in that it has a name and contains a value).<br>
 * There are optional and mandatory attributes. If a mandatory attribute is not
 * found, the message is skipped by the RPServerManager.
 * <p>
 * Mandatory Action Attributes are action_id and type.<br>
 * The action_id is used to identify the action when a resulting response comes
 * in a perception
 */
public class RPAction extends Attributes {

	/**
	 * Constructor
	 */
	public RPAction() {
		super(RPClass.getBaseRPObjectDefault());
	}

	/**
	 * Constructor
	 *
	 * @param rpclass rpclass
	 */
	public RPAction(String rpclass) {
		super(RPClass.getRPClassOrDefault(rpclass));
	}


	/**
	 * Constructor
	 *
	 * @param rpclass rpclass
	 */
	public RPAction(RPClass rpclass) {
		super(rpclass);
	}
}
