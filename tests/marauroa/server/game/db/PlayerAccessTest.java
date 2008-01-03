/* $Id: PlayerAccessTest.java,v 1.5 2008/01/03 13:33:08 astridemma Exp $ */
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Properties;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.SecureLoginTest;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test database methods that are related with player like adding a player,
 * removing it, changing its status, etc...
 * 
 * @author miguel
 * 
 */
public class PlayerAccessTest {

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
		
		@Override
		protected void initializeRPObjectFactory() {
			factory=new marauroa.server.game.rp.RPObjectFactory();
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
	 * Test if create a player account works by adding it and making sure that
	 * the account is there using has method.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void addPlayer() throws SQLException {
		String username = "testUser";

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * We test the change password method by changing the password of a existing
	 * account There is right now no simple way of checking the value, as we
	 * would need the RSA key of the server to encript the password. ( and this
	 * is stored at marauroa.ini )
	 * 
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void changePassword() throws SQLException, IOException {
		String username = "testUser";

		SecureLoginTest.loadRSAKey();

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));

			PlayerEntry.SecuredLoginInfo login = SecureLoginTest.simulateSecureLogin(username,
			        "testPassword");
			assertTrue(database.verify(transaction, login));

			database.changePassword(transaction, username, "anewtestPassword");

			/*
			 * To test if password is correct we need to use the Secure login
			 * test unit
			 */
			login = SecureLoginTest.simulateSecureLogin(username, "anewtestPassword");
			assertTrue(database.verify(transaction, login));

		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Test if adding two times the same player throw a SQLException
	 * 
	 * @throws SQLException
	 */
	@Test(expected = SQLException.class)
	public void doubleAddedPlayer() throws SQLException {
		String username = "testUser";

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();

			if (database.hasPlayer(transaction, username)) {
				fail("Player was not expected");
			}
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			if (!database.hasPlayer(transaction, username)) {
				fail("Player was expected");
			}
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			fail("Player was added");
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Remove a player and check that it is not anymore at database with has
	 * method.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void removePlayer() throws SQLException {
		String username = "testUser";

		Transaction transaction = database.getTransaction();
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
	 * Check get status method. Every account is active by default.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void getStatus() throws SQLException {
		String username = "testUser";

		Transaction transaction = database.getTransaction();

		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active", database.getAccountStatus(transaction, username));
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Check the set status method, it uses getStatus to check the set value.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void setStatus() throws SQLException {
		String username = "testUser";

		Transaction transaction = database.getTransaction();

		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active", database.getAccountStatus(transaction, username));
			database.setAccountStatus(transaction, username, "banned");
			assertEquals("banned", database.getAccountStatus(transaction, username));
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Test if create a player account works by adding it and making sure that
	 * the account is there using has method.
	 * 
	 * @throws SQLException
	 * @throws UnknownHostException
	 */
	@Test
	public void blockAccountPlayer() throws SQLException, UnknownHostException {
		String username = "testUser";

		Transaction transaction = database.getTransaction();
		try {
			transaction.begin();
			database.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(database.hasPlayer(transaction, username));

			InetAddress address = InetAddress.getLocalHost();

			assertFalse(database.isAccountBlocked(transaction, username));

			for (int i = 0; i < TimeoutConf.FAILED_LOGIN_ATTEMPS + 1; i++) {
				database.addLoginEvent(transaction, username, address, false);
			}

			assertTrue(database.isAccountBlocked(transaction, username));
		} finally {
			transaction.rollback();
		}
	}
}
