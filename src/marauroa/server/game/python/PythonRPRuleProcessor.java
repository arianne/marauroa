/* $Id: PythonRPRuleProcessor.java,v 1.20 2007/02/10 23:17:55 arianne_rpg Exp $ */
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
package marauroa.server.game.python;

import java.io.FileNotFoundException;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.server.game.AccountResult;
import marauroa.server.game.rp.IRPRuleProcessor;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.game.rp.RPWorld;

import org.apache.log4j.Logger;

/**
 * FIXME: TODO: Update this class. It is now broken. Document how to use it.
 */
@SuppressWarnings("unused")
@Deprecated
public class PythonRPRuleProcessor implements IRPRuleProcessor {
	/** the logger instance. */
	private static final Logger logger = Log4J
			.getLogger(PythonRPRuleProcessor.class);

	private GameScript gameScript;

	private PythonRP pythonRP;

	private RPServerManager rpman;

	public PythonRPRuleProcessor() throws FileNotFoundException {
	}

	/**
	 * Set the context where the actions are executed.
	 *
	 * @param rpman the RP Manager object
	 */
	public void setContext(RPServerManager rpman) {
		try {
			this.rpman = rpman;

			gameScript = GameScript.getGameScript();
			gameScript.setRPWorld(RPWorld.get());
			pythonRP = gameScript.getGameRules();
		} catch (Exception e) {
			logger.error("error while setting context", e);
		}
	}

	public boolean checkGameVersion(String game, String version) {
		return pythonRP.checkGameVersion(game, version);
	}

	public AccountResult createAccount(String username, String password, String email, RPObject template) {
		return pythonRP.createAccount(username, password, email, template);
	}

	public boolean onActionAdd(RPAction action, List<RPAction> actionList) {
		return pythonRP.onActionAdd(action, actionList);
	}

	public boolean onIncompleteActionAdd(RPAction action,
			List<RPAction> actionList) {
		return pythonRP.onIncompleteActionAdd(action, actionList);
	}

	/**
	 * Execute an action in the name of a player.
	 *
	 * @param id
	 *            the id of the object owner of the actions.
	 * @param action
	 *            the action to execute
	 */
	public void execute(RPObject.ID id, RPAction action) {
		try {
			pythonRP.execute(id, action);
		} catch (Exception e) {
			logger.error("error in execute()", e);
		}
	}

	/** Notify it when a new turn happens */
	synchronized public void endTurn() {
		pythonRP.endTurn();
	}

	synchronized public void beginTurn() {
		pythonRP.beginTurn();
	}

	synchronized public boolean onInit(RPObject object)
			throws RPObjectInvalidException {
			return pythonRP.onInit(object);
	}

	synchronized public boolean onExit(RPObject.ID id) {
		try {
			return pythonRP.onExit(id);
		} catch (Exception e) {
			logger.error("onExit() returned with an exeption", e);
			return true;
		}
	}

	synchronized public void onTimeout(RPObject.ID id) {
		try {
			pythonRP.onTimeout(id);
		} catch (Exception e) {
			logger.error("onTimeout() returned with an exeption", e);
		}
	}

	public boolean onExit(RPObject object) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub

	}

	public void execute(RPObject object, RPAction action) {
		// TODO Auto-generated method stub

	}

	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		// TODO Auto-generated method stub
		return false;
	}
}
