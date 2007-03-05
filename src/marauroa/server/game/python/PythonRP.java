/* $Id: PythonRP.java,v 1.13 2007/03/05 18:18:24 arianne_rpg Exp $ */
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

import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.net.message.TransferContent;
import marauroa.server.game.AccountResult;
import marauroa.server.game.Result;
import marauroa.server.game.rp.RPServerManager;

@Deprecated
public class PythonRP {
	public PythonRP() {
	}

	private RPServerManager rpMan;

	void setRPManager(RPServerManager rpMan) {
		this.rpMan = rpMan;
	}

	public final void transferContent(RPObject.ID id,
			List<TransferContent> content) {
		rpMan.transferContent(id, content);
	}

	public boolean checkGameVersion(String game, String version) {
		return true;
	}

	public AccountResult createAccount(String username, String password, String email) {
		return new AccountResult(Result.FAILED_EXCEPTION, username);
	}

	public boolean onActionAdd(RPAction action, List<RPAction> actionList) {
		return true;
	}

	public boolean onIncompleteActionAdd(RPAction action,
			List<RPAction> actionList) {
		return true;
	}

	public int execute(RPObject.ID id, RPAction action) {
		return 0;
	}

	public void beginTurn() {
	}

	public void endTurn() {
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		return false;
	}

	public boolean onExit(RPObject.ID id) {
		return false;
	}

	public boolean onTimeout(RPObject.ID id) {
		return false;
	}
}
