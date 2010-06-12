/* $Id: IRPRuleProcessor.java,v 1.15 2010/06/12 15:08:42 nhnb Exp $ */
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
package marauroa.server.game.rp;

import java.util.List;

import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;

/**
 * Interface for the class that is in charge of executing actions. Implement it
 * to personalize the game
 * <p>
 * <b> Important: you must implement the method<br>
 * <i>public static IRPRuleProcessor get().</i> </b>
 * <p>
 * This interface is the key to extend Marauroa to match your game needs. First
 * we have setContext that allows us complete access to RP Server Manager, so we
 * can control some things like disconnect players or send them stuff. It is not
 * possible to access GameServerManager or NetworkServerManager directly to
 * avoid incorrect manipulation of data structures that could place the server
 * in a bad state.
 * <p>
 * onActionAdd, execute, beginTurn and endTurn allow you to code behaviour for
 * each of these events. You can control whenever to allow player to add an
 * action to system, what system should do when it receive an action and what to
 * do at the begin and end of each turn.<br>
 * Perceptions are delivered just after endTurn is called.
 * <p>
 * Also your game can handle what to do at player entering, player leaving and
 * decide what to do when player timeouts because connection has been dropped
 * for example.
 *
 */
public interface IRPRuleProcessor {

	/**
	 * Set the context where the actions are executed.
	 *
	 * @param rpman
	 *            the RPServerManager object that is running our actions
	 */
	public void setContext(RPServerManager rpman);

	/**
	 * Returns true if the version of the game is compatible
	 *
	 * @param game
	 *            the game name
	 * @param version
	 *            the game version
	 * @return true if game,version is compatible
	 */
	public boolean checkGameVersion(String game, String version);

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
	public AccountResult createAccount(String username, String password, String email);

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
	public CharacterResult createCharacter(String username, String character, RPObject template);

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
	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList);

	/**
	 * Execute an action in the name of a player.
	 *
	 * @param object
	 *            the object that executes
	 * @param action
	 *            the action to execute
	 */
	public void execute(RPObject object, RPAction action);

	/**
	 * Notify it when a begin of actual turn happens.
	 */
	public void beginTurn();

	/**
	 * Notify it when a end of actual turn happens.
	 */
	public void endTurn();

	/**
	 * Callback method called when a new player enters in the game
	 *
	 * @param object
	 *            the new player that enters in the game.
	 * @return true if object has been added.
	 * @throws RPObjectInvalidException if the object was not accepted
	 */
	public boolean onInit(RPObject object) throws RPObjectInvalidException;

	/**
	 * Callback method called when a player exits the game
	 *
	 * @param object
	 *            the new player that exits the game.
	 * @return true to allow player to exit
	 * @throws RPObjectNotFoundException if the object was not found
	 */
	public boolean onExit(RPObject object) throws RPObjectNotFoundException;

	/**
	 * Callback method called when a new player time out. This method MUST
	 * logout the player
	 *
	 * @param object
	 *            the new player that timeouts.
	 * @throws RPObjectNotFoundException if the object was not found
	 */
	public void onTimeout(RPObject object) throws RPObjectNotFoundException;
}
