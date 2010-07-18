/* $Id: BareRPRuleProcessor.java,v 1.6 2010/07/18 13:46:26 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.functional.basic;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.game.Result;
import marauroa.common.net.message.TransferContent;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.JDBCSQLHelper;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.server.game.rp.IRPRuleProcessor;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.game.rp.RPWorld;
import marauroa.server.net.validator.ConnectionValidator;

public class BareRPRuleProcessor implements IRPRuleProcessor {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(BareRPRuleProcessor.class);

	private TransactionPool transactionPool;
	
	private RPWorld world;

	private List<RPObject> players;

	private RPServerManager rpman;

	public BareRPRuleProcessor() {
		new DatabaseFactory().initializeDatabase();
		transactionPool = TransactionPool.get();
		world = BareRPWorld.get();

		DBTransaction transaction = transactionPool.beginWork();
		try {
			JDBCSQLHelper sql = new JDBCSQLHelper(transaction);
			sql.runDBScript("marauroa/test/clear.sql");
			sql.runDBScript("marauroa/server/marauroa_init.sql");
			transactionPool.commit(transaction);
		} catch (SQLException e) {
			transactionPool.rollback(transaction);
			e.printStackTrace();
			TestHelper.fail();
		}

		players = new LinkedList<RPObject>();
	}

	private static BareRPRuleProcessor rules;

	/**
	 * This method MUST be implemented in other for marauroa to be able to load
	 * this World implementation. There is no way of enforcing static methods on
	 * a Interface, so just keep this in mind when writting your own game.
	 * 
	 * @return an unique instance of world.
	 */
	public static IRPRuleProcessor get() {
		if (rules == null) {
			rules = new BareRPRuleProcessor();
		}

		return rules;
	}

	public void beginTurn() {
		for (RPObject player : players) {
			int x = 0;
			if (player.has("x")) {
				x = player.getInt("x");
			}

			player.put("x", x + 1);
			world.modify(player);
		}
	}

	/**
	 * Checks if game is correct. We expect TestFramework at 0.00 version.
	 */
	public boolean checkGameVersion(String game, String version) {
		TestHelper.assertEquals("TestFramework", game);
		TestHelper.assertEquals("0.00", version);

		logger.info("Client uses:" + game + ":" + version);

		return game.equals("TestFramework") && version.equals("0.00");
	}

	/**
	 * Create an account for a player.
	 */
	public AccountResult createAccount(String username, String password, String email) {
		DBTransaction transaction = transactionPool.beginWork();
		try {
			if (DAORegister.get().get(AccountDAO.class).hasPlayer(transaction, username)) {
				logger.warn("Account already exist: " + username);
				transactionPool.commit(transaction);
				return new AccountResult(Result.FAILED_PLAYER_EXISTS, username);
			}

			DAORegister.get().get(AccountDAO.class).addPlayer(transaction, username, Hash.hash(password), email);

			transactionPool.commit(transaction);
			return new AccountResult(Result.OK_CREATED, username);
		} catch (SQLException e) {
			transactionPool.rollback(transaction);
			TestHelper.fail();
			return new AccountResult(Result.FAILED_EXCEPTION, username);
		}
	}

	/**
	 * Create a character for a player
	 */
	public CharacterResult createCharacter(String username, String character, RPObject template) {
		DBTransaction transaction = transactionPool.beginWork();
		try {
			/*
			 * We filter too short character names.
			 */
			if(character.length()<4) {
				return new CharacterResult(Result.FAILED_STRING_SIZE, character, template);
			}
			
			RPObject player = new RPObject(template);

			player.put("name", character);
			player.put("version", "0.00");
			player.put("ATK", 50);

			if (DAORegister.get().get(CharacterDAO.class).hasCharacter(transaction, character)) {
				logger.warn("Character already exist: " + character);
				return new CharacterResult(Result.FAILED_PLAYER_EXISTS, character, player);
			}

			DAORegister.get().get(CharacterDAO.class).addCharacter(transaction, username, character, player);
			transactionPool.commit(transaction);
			return new CharacterResult(Result.OK_CREATED, character, player);
		} catch (Exception e) {
			transactionPool.rollback(transaction);
			TestHelper.fail();
			return new CharacterResult(Result.FAILED_EXCEPTION, character, template);
		}
	}

	public void endTurn() {
		// empty method required by interface
	}

	public void execute(RPObject object, RPAction action) {
		logger.info(object.get("name") + " running " + action);

		RPEvent chat = new RPEvent("chat");
		chat.put("text", action.get("text"));
		object.addEvent(chat);

		int eventCounter = 0;
		if (object.has("eventcounter")) {
			eventCounter = object.getInt("eventcounter");
		}

		object.put("eventcounter", eventCounter + 1);

		world.modify(object);
	}

	public boolean onActionAdd(RPObject object, RPAction action, List<RPAction> actionList) {
		return true;
	}

	public boolean onExit(RPObject object) throws RPObjectNotFoundException {
		RPObject result = world.remove(object.getID());
		TestHelper.assertNotNull(result);

		players.remove(object);
		return true;
	}

	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		System.out.println("PreW: "+object);
		RPObject old=object;

		object.put("zoneid", "test");
		world.add(object);
		
		System.out.println("PostW("+(old==object?1:0)+"): "+object);

		TestHelper.assertNotNull(object);

		players.add(object);
		
		TransferContent content = new TransferContent("test_content", 1, new byte[] {1,2,3,4,5,6,7,8,9,10});
		
		System.out.println("PreT("+(old==object?1:0)+"): "+object);
		rpman.transferContent(object, content);
		System.out.println("PostT("+(old==object?1:0)+"): "+object);

		return true;
	}

	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		logger.warn("Client " + object.get("name") + " timeout");
		onExit(object);
	}

	public void setContext(RPServerManager rpman) {
		this.rpman = rpman;
	}

	public ConnectionValidator getValidator() {
		return rpman.getValidator();
	}
}
