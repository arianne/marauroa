/* $Id: MarauroaRPRuleProcessor.java,v 1.12 2007/01/08 19:26:13 arianne_rpg Exp $ */
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
import marauroa.common.game.RPObject.ID;
import marauroa.server.createaccount.Result;

/** Default implementation for <code>IRPRuleProcessor</code> */
@SuppressWarnings("unused")
public class MarauroaRPRuleProcessor implements IRPRuleProcessor {
	
	private static MarauroaRPRuleProcessor instance;
	
	private RPServerManager rpman;

	protected MarauroaRPRuleProcessor() {
	}
	
	public static MarauroaRPRuleProcessor get() {
		if (instance == null) {
			instance = new MarauroaRPRuleProcessor();
		}
		return instance;
	}

	public void setContext(RPServerManager rpman) {
		this.rpman = rpman;
	}

	public boolean checkGameVersion(String game, String version) {
		return false;
	}

	public Result createAccount(String username, String password, String email) {
		return Result.FAILED_EXCEPTION;
	}

	public boolean onActionAdd(RPAction action, List<RPAction> actionList) {
		return true;
	}

	public boolean onIncompleteActionAdd(RPAction action,
			List<RPAction> actionList) {
		return true;
	}

	public RPAction.Status execute(RPObject.ID id, RPAction list) {
		return RPAction.Status.FAIL;
	}

	public void beginTurn() {
	}

	public void endTurn() {
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		return false;
	}

	public boolean onExit(RPObject.ID id) throws RPObjectNotFoundException {
		return false;
	}

	public boolean onTimeout(RPObject.ID id) throws RPObjectNotFoundException {
		return false;
	}

	public void onForcedExit(ID id) throws RPObjectNotFoundException {
	}
}
