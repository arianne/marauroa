/* $Id: SystemTest.java,v 1.6 2009/12/24 13:16:55 nhnb Exp $ */
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import marauroa.client.BannedAddressException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;
import marauroa.common.net.InvalidVersionException;
import marauroa.functional.IFunctionalTest;
import marauroa.functional.MarauroadLauncher;
import marauroa.functional.SimpleClient;
import marauroa.server.net.validator.ConnectionValidator;

/**
 * Test the whole system. Mostly from the client perspective.
 * 
 * @author miguel
 * 
 */
public class SystemTest implements IFunctionalTest {

	private static final int PORT = 3218;

	private SimpleClient client;

	private static final boolean DETACHED_SERVER = false;

	private static MarauroadLauncher server;

	public void setUp() throws Exception {
		if (!DETACHED_SERVER) {
			Log4J.init("log4j.properties");
			server = new MarauroadLauncher(PORT);
			Configuration.setConfigurationFile("src/marauroa/test/server.ini");

			File ini = new File("server.ini");
			assertTrue(
					"There must be a file named server.ini in src/marauroa/test.",
					ini.exists());

			try {
				Configuration.getConfiguration();
			} catch (Exception e) {
				fail("Unable to find configuration file");
			}

			server.start();
			Thread.sleep(2000);
		}
	}

	public void tearDown() throws Exception {
		if (!DETACHED_SERVER) {
			server.finish();
			Thread.sleep(2000);
		}
	}

	public void launch() throws Exception {
		t0_createAccount();
		t1_login();
		t1_1_loginTimeout();
		t1_1_login();
		t3_loginDouble();
		t4_createCharacter();
		t4_1_createCharacterFailure();
		t5_chooseCharacter();
		t5_1_chooseWrongCharacter();
		t6_receivePerceptions();
		t7_testKeepAlive();
		t7_1_testKeepAliveWorks();
		t8_testBannedIP();
	}

	/**
	 * Test create account process by creating an account and it should work and
	 * create the account again and failing.
	 * 
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws InvalidVersionException
	 * @throws BannedAddressException
	 */

	public void t0_createAccount() throws IOException, TimeoutException,
			InvalidVersionException, BannedAddressException {
		client = new SimpleClient("log4j.properties");
		client.connect("localhost", PORT);
		AccountResult res = client.createAccount("testUsername", "password",
				"email");
		assertTrue("Account creation must not fail", !res.failed());

		assertEquals("testUsername", res.getUsername());

		/*
		 * Doing a second time should fail
		 */
		AccountResult result = client.createAccount("testUsername", "password",
				"email");
		assertTrue("Second account creation must fail", result.failed());
		client.close();
	}

	/**
	 * Test the login process.
	 * 
	 * @throws Exception
	 */

	public void t1_login() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(0, characters.length);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test the timeout event.
	 * 
	 * @throws Exception
	 */
	public void t1_1_loginTimeout() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.logout();

			client.close();
		} catch (TimeoutException e) {
			/*
			 * Good. Expected.
			 */
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test the login process.
	 * 
	 * @throws Exception
	 */

	public void t1_1_login() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testAnotherUsername", "NoPassword");
			fail("It must not login");
		} catch (Exception e) {
			e.printStackTrace();
		}

		client.close();
	}

	/**
	 * Test the login process by login twice on the server. It should logout the
	 * previous login
	 * 
	 * @throws Exception
	 */

	public void t3_loginDouble() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			SimpleClient altClient = new SimpleClient("log4j.properties");
			altClient.connect("localhost", PORT);
			altClient.login("testUsername", "password");

			String[] characters = altClient.getCharacters();
			assertEquals(0, characters.length);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		try {
			client.logout();
			fail("Connection should have been closed");
		} catch (TimeoutException e) {
			// ignore timeout during logout as we are going
			// to close the channel anyway
		}
		client.close();
	}

	/**
	 * Test the create character process.
	 * 
	 * @throws Exception
	 */

	public void t4_createCharacter() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			RPObject template = new RPObject();
			template.put("client", "junit");

			CharacterResult res = client.createCharacter("testCharacter",
					template);
			assertEquals(res.getResult(), Result.OK_CREATED);
			assertEquals("testCharacter", res.getCharacter());

			RPObject result = res.getTemplate();
			assertTrue(result.has("client"));
			assertEquals("junit", result.get("client"));
			assertTrue(result.has("name"));
			assertEquals("testCharacter", result.get("name"));

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test the create character process.
	 * 
	 * @throws Exception
	 */

	public void t4_1_createCharacterFailure() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			RPObject template = new RPObject();
			template.put("client", "junit");

			CharacterResult res = client.createCharacter("ter", template);
			assertTrue(res.getResult().failed());
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test the create character process.
	 * 
	 * @throws Exception
	 */

	public void t5_chooseCharacter() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			client.logout();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test the choose character process for an incorrect character name.
	 * 
	 * @throws Exception
	 */

	public void t5_1_chooseWrongCharacter() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testAnotherCharacter");
			assertFalse(choosen);

			client.logout();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test the perception management in game.
	 */

	public void t6_receivePerceptions() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			RPAction action = new RPAction();
			action.put("text", 1);
			client.send(action);

			while (client.getPerceptions() < 5) {
				assertTrue(client.getConnectionState());
				client.loop(0);
			}

			client.logout();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/*
	 * TODO: Write test case for Logout NACK. TODO: Remove, modify and add
	 * objects on perceptions.
	 */

	@SuppressWarnings("deprecation")
	public void t7_testKeepAlive() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client = new SimpleClient("log4j.properties") {
				@Override
				public synchronized boolean loop(int delta) {
					try {
						netMan.getMessage(30);
					} catch (InvalidVersionException e) {
					}

					return false;
				}
			};

			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			RPAction action = new RPAction();
			action.put("text", 1);
			client.send(action);

			while (client.getConnectionState()) {
				client.loop(30);
				/*
				 * We don't run loop method that would send keep alive message.
				 * Instead we just sleep a bit.
				 */
				Thread.sleep(3000);

				client.resync();
			}

			assertFalse(
					"Connection must be broken as connection was closed by server",
					client.getConnectionState());
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void t7_1_testKeepAliveWorks() throws Exception {
		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			long init = System.currentTimeMillis();
			/*
			 * Timeout for players is 30 seconds.
			 */
			while (System.currentTimeMillis() - init < 40000) {
				client.loop(0);
				Thread.sleep(50);
			}

			assertTrue("Connection still be there", client.getConnectionState());

			client.logout();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void t8_testBannedIP() throws IOException, InvalidVersionException,
			TimeoutException, LoginFailedException {
		BareRPRuleProcessor rp = (BareRPRuleProcessor) BareRPRuleProcessor
				.get();
		ConnectionValidator conn = rp.getValidator();

		conn.addBan("127.0.0.1", "0.0.0.0", 20);

		try {
			client = new SimpleClient("log4j.properties");
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			fail();
		} catch (BannedAddressException e) {
			/*
			 * Good.
			 */
		}
		client.close();
	}

}
