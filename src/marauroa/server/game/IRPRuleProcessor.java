/* $Id: IRPRuleProcessor.java,v 1.10 2006/08/23 02:11:45 wikipedian Exp $ */
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
package marauroa.server.game;

import java.util.List;

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.server.createaccount.Result;

/**
 * Interface for the class that is in charge of executing actions. Implement it
 * to personalize the game
 */
public interface IRPRuleProcessor {
	
	// Important: you must implement the method
	// public static IRPRuleProcessor get().
	// TODO: Find a way to enforce this. (The static modifier is not
	// allowed in interfaces.)
	
	
	/**
	 * Set the context where the actions are executed.
	 * 
	 * @param rpman
	 *            ...
	 */
	public void setContext(RPServerManager rpman);

	/** Returns true if the version of the game is compatible */
	public boolean checkGameVersion(String game, String version);

	/** Creates an account for the game */
	public Result createAccount(String username, String password, String email);

	/**
	 * This method is called *before* adding an action by RPScheduler so you can
	 * choose not to allow the action to be added by returning false
	 */
	public boolean onActionAdd(RPAction action, List<RPAction> actionList);

	/**
	 * This method is called *before* adding an incomplete action, an action
	 * that has been added before but has not been completed, by RPScheduler so
	 * you can choose not to allow the action to be added by returning false
	 */
	public boolean onIncompleteActionAdd(RPAction action,
			List<RPAction> actionList);

	/**
	 * Execute an action in the name of a player.
	 * 
	 * @param id
	 *            the id of the object owner of the actions.
	 * @param action
	 *            the action to execute
	 * @return the action status, that can be Success, Fail or incomplete,
	 *         please refer to Actions Explained for more info.
	 */
	public RPAction.Status execute(RPObject.ID id, RPAction action);

	/** Notify it when a begin of actual turn happens. */
	public void beginTurn();

	/** Notify it when a end of actual turn happens. */
	public void endTurn();

	/**
	 * Callback method called when a new player enters in the game
	 * 
	 * @param object
	 *            the new player that enters in the game.
	 */
	public boolean onInit(RPObject object) throws RPObjectInvalidException;

	/**
	 * Callback method called when a new player exits the game
	 * 
	 * @param id
	 *            the new player id that exits the game.
	 * @return true to update the player on database.
	 */
	public boolean onExit(RPObject.ID id) throws RPObjectNotFoundException;

	/**
	 * Callback method called when a new player time out
	 * 
	 * @param id
	 *            the new player id that timeouts.
	 */
	public boolean onTimeout(RPObject.ID id) throws RPObjectNotFoundException;
}
