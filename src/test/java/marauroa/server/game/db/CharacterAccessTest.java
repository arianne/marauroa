/***************************************************************************
 *                   (C) Copyright 2007-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.db;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the character related methods of database access.
 *
 * @author miguel
 *
 */
public class CharacterAccessTest {

	private static TransactionPool transactionPool;
	private static AccountDAO accountDAO;
	private static CharacterDAO characterDAO;

	/**
	 * Setup one time the database.
	 *
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void createDatabase() throws Exception {
		Log4J.init("marauroa/server/log4j.properties");

		new DatabaseFactory().initializeTestDatabase();

		transactionPool = TransactionPool.get();

		accountDAO = DAORegister.get().get(AccountDAO.class);
		characterDAO = DAORegister.get().get(CharacterDAO.class);
	}

	/**
	 * Add a character to a player account and test it existence with
	 * hasCharacter method.
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void addCharacter() throws SQLException, IOException {
		String username = "testUserCA";
		String character = "testCharacterCA";
		RPObject player = new RPObject();

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));
			characterDAO.addCharacter(transaction, username, character, player);
			assertTrue(characterDAO.hasCharacter(transaction, username, character));
		} finally {
			transactionPool.rollback(transaction);
		}
	}

	/**
	 * Test that adding two times the same character throws a SQLException
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test(expected = SQLException.class)
	public void doubleAddedCharacter() throws SQLException, IOException {
		String username = "testUserCA";
		String character = "testCharacterCA";
		RPObject player = new RPObject();

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));
			characterDAO.addCharacter(transaction, username, character, player);
			assertTrue(characterDAO.hasCharacter(transaction, username, character));
			characterDAO.addCharacter(transaction, username, character, player);

			fail("Character was added");
		} finally {
			transactionPool.rollback(transaction);
		}
	}

	/**
	 * Test that remove character removed it and assert with hasCharacter.
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void removeCharacter() throws SQLException, IOException {
		String username = "testUserCA";
		String character = "testCharacterCA";
		RPObject player = new RPObject();

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));
			characterDAO.addCharacter(transaction, username, character, player);
			assertTrue(characterDAO.hasCharacter(transaction, username, character));
			characterDAO.removeCharacter(transaction, username, character);
			assertFalse(characterDAO.hasCharacter(transaction, username, character));
		} finally {
			transactionPool.rollback(transaction);
		}
	}

	/**
	 * Check that removing the player does in fact also removes the character
	 * that belonged to that player.
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void removePlayerCharacter() throws SQLException, IOException {
		String username = "testUserCA";
		String character = "testCharacterCA";
		RPObject player = new RPObject();

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));
			characterDAO.addCharacter(transaction, username, character, player);
			assertTrue(characterDAO.hasCharacter(transaction, username, character));
			accountDAO.removePlayer(transaction, username);
			assertFalse(characterDAO.hasCharacter(transaction, username, character));
		} finally {
			transactionPool.rollback(transaction);
		}
	}

	/**
	 * Check that getCharacters return a list with all the characters that
	 * belong to a player.
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void getCharacters() throws SQLException, IOException {
		String username = "testUserCA";
		String[] characters = { "testCharacterCA1", "testCharacterCA2", "testCharacterCA3" };

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			for (String character : characters) {
				characterDAO.addCharacter(transaction, username, character, new RPObject());
			}

			List<String> result = characterDAO.getCharacters(transaction, username);
			assertArrayEquals(characters, result.toArray());
		} finally {
			transactionPool.rollback(transaction);
		}
	}

	/**
	 * Check that storing and loading an avatar associated to a character of a
	 * player works as expected. This code depends on RPObject and
	 * Serialization.
	 *
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void storeAndLoadCharacter() throws SQLException, IOException {
		String username = "testUserCA";
		String character = "testCharacterCA";
		RPObject player = new RPObject();
		player.put("one", "number one");
		player.put("two", 2);
		player.put("three", 3.0);

		DBTransaction transaction = transactionPool.beginWork();
		try {
			assertFalse(accountDAO.hasPlayer(transaction, username));
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			assertFalse(characterDAO.hasCharacter(transaction, username, character));
			characterDAO.addCharacter(transaction, username, character, player);

			RPObject loaded = characterDAO.loadCharacter(transaction, username, character);
			assertEquals(player, loaded);
		} finally {
			transactionPool.rollback(transaction);
		}
	}

	/**
	 * Changes status of a character.
	 *
	 * @throws java.sql.SQLException if the status change fails.
	 * @throws java.io.IOException
	 */
	@Test
	public void changeCharacterStatus() throws SQLException, IOException {
		//First create a character
		String username = "testUserCA2";
		String character = "testCharacterCA2";
		RPObject player = new RPObject();

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));
			characterDAO.addCharacter(transaction, username, character, player);
			assertTrue(characterDAO.hasCharacter(transaction, username, character));
			assertTrue(characterDAO.hasActiveCharacter(transaction, username, character));
			//Now change the status
			characterDAO.setCharacterStatus(transaction, username, character, "inactive");
			//Check again
			assertFalse(characterDAO.hasActiveCharacter(transaction, username, character));
		} finally {
			transactionPool.rollback(transaction);
		}
	}

}
