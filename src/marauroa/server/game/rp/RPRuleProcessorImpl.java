/***************************************************************************
 *                   (C) Copyright 2011-2012 - Marauroa                    *
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

import java.sql.SQLException;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.crypto.Hash;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.game.Result;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * a default implementation of RPRuleProcessorImpl
 */
public class RPRuleProcessorImpl implements IRPRuleProcessor {
	private static RPRuleProcessorImpl instance;

	/** RPServerManager */
	protected RPServerManager manager;

	private static Logger logger = Log4J.getLogger(RPRuleProcessorImpl.class);

	/**
	 * gets the Rule singleton object
	 *
	 * @return Rule
	 */
	public static IRPRuleProcessor get() {
		if (instance == null) {
			instance = new RPRuleProcessorImpl();
		}
		return instance;
	}


	/**
	 * Set the context where the actions are executed.
	 *
	 * @param rpman the RPServerManager object that is running our actions
	 */
	public void setContext(RPServerManager rpman) {
		manager = rpman;
	}


	/**
	 * Returns true if the version of the game is compatible
	 *
	 * @param game the game name
	 * @param version the game version
	 * @return true, if the game and version is compatible
	 */
	public boolean checkGameVersion(String game, String version) {
		return true;
	}

	/**
	 * Callback method called when a new player time out. This method MUST
	 * logout the player
	 *
	 * @param object the new player that timeouts.
	 * @throws RPObjectNotFoundException if the object was not found
	 */
	public synchronized void onTimeout(RPObject object) {
		onExit(object);
	}


	/**
	 * Callback method called when a player exits the game
	 *
	 * @param object the new player that exits the game.
	 * @return true to allow player to exit
	 * @throws RPObjectNotFoundException if the object was not found
	 */
	public synchronized boolean onExit(RPObject object) {
		RPWorld.get().remove(object.getID());
		return true;
	}


	/**
	 * Callback method called when a new player enters in the game
	 *
	 * @param object the new player that enters in the game.
	 * @return true if object has been added.
	 * @throws RPObjectInvalidException if the object was not accepted
	 */
	public synchronized boolean onInit(RPObject object) {
		IRPZone zone = RPWorld.get().getDefaultZone();
		zone.add(object);
		return true;
	}


	/**
	 * Notify it when a begin of actual turn happens.
	 */
	public synchronized void beginTurn() {
		// do nothing
	}


	/**
	 * This method is called *before* adding an action by RPScheduler so you can
	 * choose not to allow the action to be added by returning false
	 *
	 * @param caster the object that casted the action
	 * @param action the action that is going to be added.
	 * @param actionList the actions that this player already owns.
	 * @return true if we approve the action to be added.
	 */
	public boolean onActionAdd(RPObject caster, RPAction action, List<RPAction> actionList) {
		// accept all actions
		return true;
	}


	/**
	 * Notify it when a end of actual turn happens.
	 */
	public synchronized void endTurn() {
		// do nothing
	}


	/**
	 * Execute an action in the name of a player.
	 *
	 * @param caster the object that executes
	 * @param action the action to execute
	 */
	public void execute(RPObject caster, RPAction action) {
		logger.info(caster + " executed action " + action);
	}


	/**
	 * Creates an account for the game
	 *
	 * @param username the username who is going to be added.
	 * @param password the password for our username.
	 * @param email the email of the player for notifications or password reminders.
	 * @return the Result of creating the account.
	 */
	public AccountResult createAccount(String username, String password, String email) {
		TransactionPool transactionPool = TransactionPool.get();
		DBTransaction trans = transactionPool.beginWork();
		AccountDAO accountDAO = DAORegister.get().get(AccountDAO.class);
		try {
			if (accountDAO.hasPlayer(trans, username)) {
				return new AccountResult(Result.FAILED_CHARACTER_EXISTS, username);
			}
			accountDAO.addPlayer(trans, username, Hash.hash(password), email);
			transactionPool.commit(trans);
			return new AccountResult(Result.OK_CREATED, username);

		} catch (SQLException e) {
			logger.error(e, e);
			transactionPool.rollback(trans);
			return new AccountResult(Result.FAILED_EXCEPTION, username);
		}
	}


	/**
	 * Creates an new character for an account already logged into the game
	 *
	 * @param username the username who owns the account of the character to be added.
	 * @param character the character to create
	 * @param template the desired values of the avatar representing the character.
	 * @return the Result of creating the character.
	 */
	public CharacterResult createCharacter(String username, String character, RPObject template) {
		TransactionPool transactionPool = TransactionPool.get();
		DBTransaction trans = transactionPool.beginWork();
		CharacterDAO characterDAO = DAORegister.get().get(CharacterDAO.class);
		try {
			if (characterDAO.hasCharacter(trans, username, character)) {
				return new CharacterResult(Result.FAILED_PLAYER_EXISTS, character, template);
			}
			RPObject object = createCharacterObject(username, character, template);
			IRPZone zone = RPWorld.get().getDefaultZone();
			zone.assignRPObjectID(object);
			characterDAO.addCharacter(trans, username, character, object);
			transactionPool.commit(trans);
			return new CharacterResult(Result.OK_CREATED, character, object);

		} catch (Exception e) {
			logger.error(e, e);
			transactionPool.rollback(trans);
			return new CharacterResult(Result.FAILED_EXCEPTION, character, template);
		}
	}

	/**
	 * Creates an new character object that will used by createCharacter
	 *
	 * @param username the username who owns the account of the character to be added.
	 * @param character the character to create
	 * @param template the desired values of the avatar representing the character.
	 * @return RPObject
	 */
	@SuppressWarnings("unused")
	protected RPObject createCharacterObject(String username, String character, RPObject template) {
		RPObject object = new RPObject(template);
		object.put("name", character);
		return object;
	}
}
