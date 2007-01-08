/* $Id: PythonRPRuleProcessor.java,v 1.12 2007/01/08 19:26:14 arianne_rpg Exp $ */
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
import marauroa.common.game.RPObject.ID;
import marauroa.server.createaccount.Result;
import marauroa.server.game.IRPRuleProcessor;
import marauroa.server.game.RPServerManager;
import marauroa.server.game.RPWorld;

import org.apache.log4j.Logger;

/**
 * FIXME: TODO: Update this class. It is now broken. Document how to use it.
 */
@SuppressWarnings("unused")
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
	 * @param zone
	 *            The zone where actions happens.
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

	public Result createAccount(String username, String password, String email) {
		return pythonRP.createAccount(username, password, email);
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
	 * @return the action status, that can be Success, Fail or incomplete,
	 *         please refer to Actions Explained for more info.
	 */
	public RPAction.Status execute(RPObject.ID id, RPAction action) {
		Log4J.startMethod(logger, "execute");

		RPAction.Status status = RPAction.Status.FAIL;

		try {
			if (pythonRP.execute(id, action) == 1) {
				status = RPAction.Status.SUCCESS;
			}
		} catch (Exception e) {
			logger.error("error in execute()", e);
		} finally {
			Log4J.finishMethod(logger, "execute");
		}

		return status;
	}

	/** Notify it when a new turn happens */
	synchronized public void endTurn() {
		Log4J.startMethod(logger, "endTurn");
		pythonRP.endTurn();
		Log4J.finishMethod(logger, "endTurn");
	}

	synchronized public void beginTurn() {
		Log4J.startMethod(logger, "beginTurn");
		pythonRP.beginTurn();
		Log4J.finishMethod(logger, "beginTurn");
	}

	synchronized public boolean onInit(RPObject object)
			throws RPObjectInvalidException {
		Log4J.startMethod(logger, "onInit");
		try {
			return pythonRP.onInit(object);
		} finally {
			Log4J.finishMethod(logger, "onInit");
		}
	}

	synchronized public boolean onExit(RPObject.ID id) {
		Log4J.startMethod(logger, "onExit");
		try {
			return pythonRP.onExit(id);
		} catch (Exception e) {
			logger.error("onExit() returned with an exeption", e);
			return true;
		} finally {
			Log4J.finishMethod(logger, "onExit");
		}
	}

	synchronized public boolean onTimeout(RPObject.ID id) {
		Log4J.startMethod(logger, "onTimeout");
		try {
			return pythonRP.onTimeout(id);
		} catch (Exception e) {
			logger.error("onTimeout() returned with an exeption", e);
			return true;
		} finally {
			Log4J.startMethod(logger, "onTimeout");
		}
	}

	public void onForcedExit(ID id) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub
		
	}
}
