/* $Id: PythonRPRuleProcessor.java,v 1.27 2008/08/16 16:56:46 nhnb Exp $ */
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

import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.server.game.rp.IRPRuleProcessor;
import marauroa.server.game.rp.RPServerManager;

/**
 * Python implementation of IRPRuleProcessor. You can't inherit directly
 * IRPRuleProcessor, so you need to inherit in your Python code the
 * PythonRPRuleProcessor class.
 * 
 * You should set ruleprocessor in server.ini to this class.
 * 
 * @see IRPRuleProcessor
 * 
 * @author miguel
 */
public class PythonRPRuleProcessor implements IRPRuleProcessor {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J
	        .getLogger(PythonRPRuleProcessor.class);

	/** The link with the python engine */
	private GameScript gameScript;

	/** Python implementation of IRPRuleProcessor */
	private PythonRP pythonRP;

	/**
	 * Constructor
	 */
	public PythonRPRuleProcessor() {
		// default constructor
	}

	/**
	 * Set the context where the actions are executed.
	 * 
	 * @param rpman
	 *            the RP Manager object
	 */
	public void setContext(RPServerManager rpman) {
		try {
			gameScript = GameScript.getGameScript();
			pythonRP = gameScript.getGameRules();

			pythonRP.setRPManager(rpman);
		} catch (Exception e) {
			logger.error("error while setting context", e);
		}
	}

	public void beginTurn() {
		pythonRP.beginTurn();
	}

	public boolean checkGameVersion(String game, String version) {
		return pythonRP.checkGameVersion(game, version);
	}

	public AccountResult createAccount(String username, String password, String email) {
		return pythonRP.createAccount(username, password, email);
	}

	public CharacterResult createCharacter(String username, String character, RPObject template) {
		return pythonRP.createCharacter(username, character, template);
	}

	public void endTurn() {
		pythonRP.endTurn();

	}

	public void execute(RPObject object, RPAction action) {
		pythonRP.execute(object, action);
	}

	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		return pythonRP.onActionAdd(object, action, actionList);
	}

	public boolean onExit(RPObject object) throws RPObjectNotFoundException {
		return pythonRP.onExit(object);
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		return pythonRP.onInit(object);
	}

	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		pythonRP.onTimeout(object);
	}
}
