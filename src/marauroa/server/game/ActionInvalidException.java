/* $Id: ActionInvalidException.java,v 1.11 2008/02/22 10:28:33 arianne_rpg Exp $ */
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
package marauroa.server.game;

/**
 * This exception is thrown when an action lacks of an important attribute like:
 * <ul>
 * <li>sourceid
 * <li>zoneid
 * </ul>
 *
 * @author miguel
 *
 */
public class ActionInvalidException extends Exception {

	private static final long serialVersionUID = -2287105367089095987L;

	/**
	 * Constructor
	 *
	 * @param attribute missing attribute
	 */
	public ActionInvalidException(String attribute) {
		super("Action is invalid: It lacks of mandatory attribute [" + attribute + "]");
	}
}
