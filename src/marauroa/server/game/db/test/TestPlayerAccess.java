/* $Id: TestPlayerAccess.java,v 1.5 2007/02/28 20:37:50 arianne_rpg Exp $ */
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
package marauroa.server.game.db.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.server.game.db.JDBCDatabase;
import marauroa.server.game.db.Transaction;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test database methods that are related with player like adding a player, removing
 * it, changing its status, etc...
 * @author miguel
 *
 */
public class TestPlayerAccess {

	/**
	 * JDBCDatabase can only be instantiated by DatabaseFactory, so we extend instead
	 * JDBC Database and create a proper public constructor.
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

		database=new TestJDBC(props);
	}

	/**
	 * Test if create a player account works by adding it and making sure that the account is
	 * there using has method.
	 * @throws SQLException
	 */
	@Test
	public void addPlayer() throws SQLException {
		String username="testUser";

		Transaction transaction=database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * We test the change password method by changing the password of a existing account
	 * There is right now no simple way of checking the value, as we would need the
	 * RSA key of the server to encript the password. ( and this is stored at marauroa.ini )
	 *
	 * @throws SQLException
	 */
	@Test
	public void changePassword() throws SQLException {
		String username="testUser";

		Transaction transaction=database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));

			database.changePassword(transaction, username, "anewtestPassword");

			/* TODO: There is no way of testing if password is correct or not. */
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Test if adding two times the same player throw a SQLException
	 * @throws SQLException
	 */
	@Test(expected=SQLException.class)
	public void doubleAddedPlayer() throws SQLException {
		String username="testUser";

		Transaction transaction=database.getTransaction();
		try{
			transaction.begin();

			if(database.hasPlayer(transaction, username)) {
				fail("Player was not expected");
			}
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			if(!database.hasPlayer(transaction, username)) {
				fail("Player was expected");
			}
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			fail("Player was added");
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Remove a player and check that it is not anymore at database with has method.
	 * @throws SQLException
	 */
	@Test
	public void removePlayer() throws SQLException {
		String username="testUser";

		Transaction transaction=database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
			database.removePlayer(transaction, username);
			assertFalse(database.hasPlayer(transaction, username));
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Check get status method.
	 * Every account is active by default.
	 * @throws SQLException
	 */
	@Test
	public void getStatus() throws SQLException {
		String username="testUser";

		Transaction transaction=database.getTransaction();

		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active",database.getAccountStatus(transaction, username));
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Check the set status method, it uses getStatus to check the set value.
	 * @throws SQLException
	 */
	@Test
	public void setStatus() throws SQLException {
		String username="testUser";

		Transaction transaction=database.getTransaction();

		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active",database.getAccountStatus(transaction, username));
			database.setAccountStatus(transaction, username, "banned");
			assertEquals("banned",database.getAccountStatus(transaction, username));
		} finally {
			transaction.rollback();
		}
	}
}
