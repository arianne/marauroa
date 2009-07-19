/* $Id: PythonRP.java,v 1.19 2009/07/19 09:44:51 nhnb Exp $ */
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

	public void beginTurn() {
		// implement in sub classes
	}

	@SuppressWarnings("unused")
	public boolean checkGameVersion(String game, String version) {
		return true;
	}

	@SuppressWarnings("unused")
	public AccountResult createAccount(String username, String password, String email) {
		return null;
	}

	@SuppressWarnings("unused")
	public CharacterResult createCharacter(String username, String character, RPObject template) {
		return null;
	}

	public void endTurn() {
		// implement in sub classes
	}

	@SuppressWarnings("unused")
	public void execute(RPObject object, RPAction action) {
		// implement in sub classes
	}

	@SuppressWarnings("unused")
	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		return true;
	}

	@SuppressWarnings("unused")
	public void onTimeout(RPObject object) {
		// implement in sub classes
	}

	@SuppressWarnings("unused")
	public boolean onExit(RPObject object) {
		return true;
	}

	@SuppressWarnings("unused")
	public boolean onInit(RPObject object) {
		return true;
	}

}
