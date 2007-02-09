/* $Id: MarauroaRPRuleProcessor.java,v 1.5 2007/02/09 15:51:47 arianne_rpg Exp $ */
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
import marauroa.server.game.AccountResult;

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
		// TODO Auto-generated method stub
		this.rpman = rpman;
	}

	public boolean checkGameVersion(String game, String version) {
		// TODO Auto-generated method stub
		return false;
	}

	public AccountResult createAccount(String username, String password, String email, RPObject template) {
		// TODO Auto-generated method stub
		return AccountResult.FAILED_EXCEPTION;
	}

	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		// TODO Auto-generated method stub
		return true;
	}

	public void execute(RPObject object, RPAction action) {
		// TODO Auto-generated method stub
	}

	public void beginTurn() {
		// TODO Auto-generated method stub
	}

	public void endTurn() {
		// TODO Auto-generated method stub
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onExit(RPObject object) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub
		return false;
	}

	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		// TODO Auto-generated method stub
		
	}
}
