/* $Id: IRPRuleProcessor.java,v 1.2 2007/01/19 08:08:54 arianne_rpg Exp $ */
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

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.server.createaccount.Result;

/**
 * Interface for the class that is in charge of executing actions. Implement it
 * to personalize the game
 * Important: you must implement the method
 *	 public static IRPRuleProcessor get().
 *
 * TODO: Find a way to enforce this. (The static modifier is not allowed in interfaces.)
 */
public interface IRPRuleProcessor {	
	/**
	 * Set the context where the actions are executed.
	 * 
	 * @param rpman the RPServerManager object that is running our actions
	 */
	public void setContext(RPServerManager rpman);

	/** 
	 * Returns true if the version of the game is compatible 
	 * @param game the game name
	 * @param version the game version
	 * @return true if game,version is compatible */
	public boolean checkGameVersion(String game, String version);

	/** 
	 * Creates an account for the game 
	 * @param username the username who is going to be added.
	 * @param password the password for our username. 
	 * @param email the email of the player for notifications or password reminders. 
	 * @param template the RPObject template we going to create.
	 * @return the Result of creating the account. */
	public Result createAccount(String username, String password, String email, RPObject template);

	/**
	 * This method is called *before* adding an action by RPScheduler so you can
	 * choose not to allow the action to be added by returning false
	 * 
	 * @param action the action that is going to be added.
	 * @param actionList the actions that this player already owns.
	 * @return true if we approve the action to be added.
	 */
	public boolean onActionAdd(RPAction action, List<RPAction> actionList);

	/**
	 * Execute an action in the name of a player.
	 * 
	 * @param id the id of the object owner of the actions.
	 * @param action the action to execute
	 */
	public void execute(RPObject.ID id, RPAction action);

	/** Notify it when a begin of actual turn happens. */
	public void beginTurn();

	/** Notify it when a end of actual turn happens. */
	public void endTurn();

	/**
	 * Callback method called when a new player enters in the game
	 * 
	 * @param object the new player that enters in the game.
	 */
	public boolean onInit(RPObject object) throws RPObjectInvalidException;

	/**
	 * Callback method called when a player exits the game
	 * 
	 * @param id the new player id that exits the game.
	 * @return true to allow player to exit
	 */
	public boolean onExit(RPObject.ID id) throws RPObjectNotFoundException;

	/**
	 * Callback method called when a new player time out.
	 * This method MUST logout the player
	 * 
	 * @param id the new player id that timeouts.
	 */
	public void onTimeout(RPObject.ID id) throws RPObjectNotFoundException;
}
