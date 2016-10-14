/***************************************************************************
 *                (C) Copyright 2011-2013 - Faiumoni e. V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.rp;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.Message;

/**
 * A interface which assists in debugging a server.
 *
 * @author hendrik
 */
public class DebugInterface {
	private static DebugInterface instance = new DebugInterface();

	/**
	 * gets the instance of the DebugInterface
	 *
	 * @return DebugInterface
	 */
	public static DebugInterface get() {
		return instance;
	}

	/**
	 * sets the instance of the DebugInterface
	 *
	 * @param debugInterface DebugInterface
	 */
	public static void set(DebugInterface debugInterface) {
		instance = debugInterface;
	}

	/**
	 * an action is about to be executed
	 *
	 * @param object object belonging to the client requesting the action
	 * @param action action as sent from the client
	 * @return true if the action should be executed; false if it should be skiped.
	 */
	public boolean executeAction(@SuppressWarnings("unused") RPObject object, @SuppressWarnings("unused") RPAction action) {
		return true;
	}

	/**
	 * This method is called when a player is added to the game world
	 *
	 * @param object player object
	 * @return true, to continue, false to cause an error
	 */
	public boolean onInit(@SuppressWarnings("unused") RPObject object) {
		return true;
	}

	/**
	 * This method is called when a player leaves the game
	 *
	 * @param object player object
	 * @return true, to continue, false to prevent logout
	 */
	public boolean onExit(@SuppressWarnings("unused") RPObject object) {
		return true;
	}

	/**
	 * This method is called when connection to client is closed
	 *
	 * @param object player object
	 */
	public void onTimeout(@SuppressWarnings("unused") RPObject object) {
		return;
	}

	/**
	 * reports crashes
	 *
	 * @param object object
	 */
	public void onCrash(@SuppressWarnings("unused") RPObject object) {
		return;
	}

	/**
	 * This method is called, when a message is received
	 *
	 * @param msg message
	 */
	public void onMessage(@SuppressWarnings("unused") Message msg) {
		return;
	}
}
