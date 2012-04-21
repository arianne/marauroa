/***************************************************************************
 *                   (C) Copyright 2010-2012 - Marauroa                    *
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
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.SecureLoginTest;

import org.junit.AfterClass;
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
	private static TransactionPool transactionPool;
	private static AccountDAO accountDAO;
	private static LoginEventDAO loginEventDAO;

	/**
	 * Setup one time the accountDAO.
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
		props.put("database_adapter", "marauroa.server.db.adapter.MySQLDatabaseAdapter");
		transactionPool = new TransactionPool(props);
		accountDAO = DAORegister.get().get(AccountDAO.class);
		loginEventDAO = DAORegister.get().get(LoginEventDAO.class);
	}

	/**
	 * Setup one time the accountDAO.
	 *
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void closeDatabase() throws Exception {
		transactionPool.close();
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

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));
		} finally {
			transactionPool.rollback(transaction);
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

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));

			PlayerEntry.SecuredLoginInfo login = SecureLoginTest.simulateSecureLogin(username,
			        "testPassword");
			assertTrue(accountDAO.verify(transaction, login));

			accountDAO.changePassword(transaction, username, "anewtestPassword");

			/*
			 * To test if password is correct we need to use the Secure login
			 * test unit
			 */
			login = SecureLoginTest.simulateSecureLogin(username, "anewtestPassword");
			assertTrue(accountDAO.verify(transaction, login));

		} finally {
			transactionPool.rollback(transaction);
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

		DBTransaction transaction = transactionPool.beginWork();
		try {
			if (accountDAO.hasPlayer(transaction, username)) {
				fail("Player was not expected");
			}
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			if (!accountDAO.hasPlayer(transaction, username)) {
				fail("Player was expected");
			}
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");

			fail("Player was added");
		} finally {
			transactionPool.rollback(transaction);
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

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));
			accountDAO.removePlayer(transaction, username);
			assertFalse(accountDAO.hasPlayer(transaction, username));
		} finally {
			transactionPool.rollback(transaction);
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

		DBTransaction transaction = transactionPool.beginWork();

		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active", accountDAO.getAccountStatus(transaction, username));
		} finally {
			transactionPool.rollback(transaction);
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

		DBTransaction transaction = transactionPool.beginWork();

		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertEquals("active", accountDAO.getAccountStatus(transaction, username));
			accountDAO.setAccountStatus(transaction, username, "banned");
			assertEquals("banned", accountDAO.getAccountStatus(transaction, username));
		} finally {
			transactionPool.rollback(transaction);
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

		DBTransaction transaction = transactionPool.beginWork();
		try {
			accountDAO.addPlayer(transaction, username, Hash.hash("testPassword"), "email@email.com");
			assertTrue(accountDAO.hasPlayer(transaction, username));

			InetAddress address = InetAddress.getLocalHost();

			assertFalse(loginEventDAO.isAccountBlocked(transaction, username));

			for (int i = 0; i < TimeoutConf.FAILED_LOGIN_ATTEMPTS_ACCOUNT + 1; i++) {
				DAORegister.get().get(LoginEventDAO.class).addLoginEvent(transaction, username, address, null, null, 0);
			}

			assertTrue(loginEventDAO.isAccountBlocked(transaction, username));
		} finally {
			transactionPool.rollback(transaction);
		}
	}
}
