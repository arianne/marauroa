/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.python;

import java.util.List;

import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.TransferContent;
import marauroa.server.game.rp.RPServerManager;

/**
 * Barebone implementation of RPRuleProcessor that you can extends at Python.
 * You must extend this class.
 * 
 * @author miguel
 * 
 */
public class PythonRP {

	/**
	 * Constructor
	 * 
	 */
	public PythonRP() {
		// default constructor
	}

	/** instance of rp manager */
	private RPServerManager rpMan;

	/**
	 * This method is called by PythonRPRuleProcessor to make us know which is
	 * the RP Manager instance.
	 */
	void setRPManager(RPServerManager rpMan) {
		this.rpMan = rpMan;
	}

	/**
	 * Transfer content to client.
	 * 
	 * @param target
	 *            client to transfer content to.
	 * @param content
	 *            the contents to transfer.
	 */
	public void transferContent(RPObject target, List<TransferContent> content) {
		rpMan.transferContent(target, content);
	}

	/**
	 * Notify it when a begin of actual turn happens.
	 */
	public void beginTurn() {
		// implement in sub classes
	}

	/**
	 * Returns true if the version of the game is compatible
	 *
	 * @param game
	 *            the game name
	 * @param version
	 *            the game version
	 * @return true if game,version is compatible
	 */
	@SuppressWarnings("unused")
	public boolean checkGameVersion(String game, String version) {
		return true;
	}

	/**
	 * Creates an account for the game
	 *
	 * @param username
	 *            the username who is going to be added.
	 * @param password
	 *            the password for our username.
	 * @param email
	 *            the email of the player for notifications or password
	 *            reminders.
	 * @return the Result of creating the account.
	 */
	@SuppressWarnings("unused")
	public AccountResult createAccount(String username, String password, String email) {
		return null;
	}

	/**
	 * Creates an new character for an account already logged into the game
	 *
	 * @param username
	 *            the username who owns the account of the character to be
	 *            added.
	 * @param character
	 *            the character to create
	 * @param template
	 *            the desired values of the avatar representing the character.
	 * @return the Result of creating the character.
	 */
	@SuppressWarnings("unused")
	public CharacterResult createCharacter(String username, String character, RPObject template) {
		return null;
	}

	/**
	 * Notify it when a end of actual turn happens.
	 */
	public void endTurn() {
		// implement in sub classes
	}

	
	/**
	 * Execute an action in the name of a player.
	 *
	 * @param object
	 *            the object that executes
	 * @param action
	 *            the action to execute
	 */
	@SuppressWarnings("unused")
	public void execute(RPObject object, RPAction action) {
		// implement in sub classes
	}

	/**
	 * This method is called *before* adding an action by RPScheduler so you can
	 * choose not to allow the action to be added by returning false
	 *
	 * @param object
	 *            the object that casted the action
	 * @param action
	 *            the action that is going to be added.
	 * @param actionList
	 *            the actions that this player already owns.
	 * @return true if we approve the action to be added.
	 */
	@SuppressWarnings("unused")
	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		return true;
	}

	/**
	 * Callback method called when a new player time out. This method MUST
	 * logout the player
	 *
	 * @param object
	 *            the new player that timeouts.
	 */
	@SuppressWarnings("unused")
	public void onTimeout(RPObject object) {
		// implement in sub classes
	}


	/**
	 * Callback method called when a player exits the game
	 *
	 * @param object
	 *            the new player that exits the game.
	 * @return true to allow player to exit
	 */
	@SuppressWarnings("unused")
	public boolean onExit(RPObject object) {
		return true;
	}

	/**
	 * Callback method called when a new player enters in the game
	 *
	 * @param object
	 *            the new player that enters in the game.
	 * @return true if object has been added.
	 */
	@SuppressWarnings("unused")
	public boolean onInit(RPObject object) {
		return true;
	}

}
