/* $Id: CharacterAccessTest.java,v 1.3 2007/12/28 17:17:55 martinfuchs Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
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
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.RPObject;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the character related methods of database access.
 * 
 * @author miguel
 * 
 */
public class CharacterAccessTest {

	/**
	 * JDBCDatabase can only be instantiated by DatabaseFactory, so we extend
	 * instead JDBC Database and create a proper public constructor.
	 * 
	 * @author miguel
	 * 
	 */
	static class TestJDBC extends JDBCDatabase {

		public TestJDBC(Properties props) {
			super(props);
		}
	}

	private static TestJDBC database;

	/**
	 * Setup one time the database.
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void createDatabase() throws Exception {
		Log4J.init("marauroa/server/log4j.properties");

		Properties props = new Properties();

		props.put("jdbc_url", "jdbc:mysql://127.0.0.1/marauroatest");
		props.put("jdbc_class", "com.mysql.jdbc.Driver");
		props.put("jdbc_user", "junittest");
		props.put("jdbc_pwd", "passwd");

		database = new TestJDBC(props);
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

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.addCharacter(transaction, username, character, player);
			assertTrue(database.hasCharacter(transaction, username, character));
		} finally {
			transaction.rollback();
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

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();

			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.addCharacter(transaction, username, character, player);
			assertTrue(database.hasCharacter(transaction, username, character));
			database.addCharacter(transaction, username, character, player);

			fail("Character was added");
		} finally {
			transaction.rollback();
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

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.addCharacter(transaction, username, character, player);
			assertTrue(database.hasCharacter(transaction, username, character));
			database.removeCharacter(transaction, username, character);
			assertFalse(database.hasCharacter(transaction, username, character));
		} finally {
			transaction.rollback();
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

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.addCharacter(transaction, username, character, player);
			assertTrue(database.hasCharacter(transaction, username, character));
			database.removePlayer(transaction, username);
			assertFalse(database.hasCharacter(transaction, username, character));
		} finally {
			transaction.rollback();
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

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			for (String character : characters) {
				database.addCharacter(transaction, username, character, new RPObject());
			}

			List<String> result = database.getCharacters(transaction, username);
			assertArrayEquals(characters, result.toArray());
		} finally {
			transaction.rollback();
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

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			assertFalse(database.hasPlayer(transaction, username));
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			assertFalse(database.hasCharacter(transaction, username, character));
			database.addCharacter(transaction, username, character, player);

			RPObject loaded = database.loadCharacter(transaction, username, character);
			assertEquals(player, loaded);
		} finally {
			transaction.rollback();
		}
	}

}
