/* $Id: SystemTest.java,v 1.44 2008/02/22 10:28:34 arianne_rpg Exp $ */
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
package marauroa.test;

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
import marauroa.common.net.NetConst;
import marauroa.server.net.validator.ConnectionValidator;
import marauroa.test.CrushServer.MockMarauroad;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the whole system. Mostly from the client perspective.
 * 
 * @author miguel
 * 
 */
public class SystemTest {

	private static final int PORT = 3218;

	private MockClient client;

	private static final boolean DETACHED_SERVER = false;

	private static MockMarauroad server;

	@BeforeClass
	public static void createServer() throws InterruptedException {
		if (!DETACHED_SERVER) {
			Log4J.init("log4j.properties");
			server = new MockMarauroad();
			Configuration.setConfigurationFile("src/marauroa/test/server.ini");

			File ini = new File("server.ini");
			assertTrue("There must be a file named server.ini in src/marauroa/test.",ini.exists());

			try {
				Configuration.getConfiguration();
			} catch (Exception e) {
				fail("Unable to find configuration file");
			}

			/*
			 * Ugly hack, but junit does runs test cases in parallel
			 */
			NetConst.tcpPort = PORT;

			server.start();
			Thread.sleep(2000);
		}
	}

	@AfterClass
	public static void takeDownServer() throws InterruptedException {
		if (!DETACHED_SERVER) {
			server.finish();
			Thread.sleep(2000);
		}
	}

	/**
	 * Create a new client each time
	 * 
	 */
	@Before
	public void createClient() {
		client = new MockClient("log4j.properties");
	}

	/**
	 * And disconnect it when done.
	 * 
	 */
	@After
	public void disconnectClient() {
		client.close();
	}
	
	/**
	 * Test create account process by creating an account and it should work and
	 * create the account again and failing.
	 * 
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws InvalidVersionException
	 * @throws CreateAccountFailedException
	 * @throws BannedAddressException
	 */
	@Test
	public void t0_createAccount() throws IOException, TimeoutException, InvalidVersionException, BannedAddressException {
		client.connect("localhost", PORT);
		AccountResult res = client.createAccount("testUsername", "password", "email");
		assertTrue("Account creation must not fail", !res.failed());

		assertEquals("testUsername", res.getUsername());

		/*
		 * Doing a second time should fail
		 */
		AccountResult result=client.createAccount("testUsername", "password", "email");
		assertTrue("Second account creation must fail", result.failed());
	}

	/**
	 * Test the login process.
	 * 
	 * @throws Exception
	 */
	@Test
	public void t1_login() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(0, characters.length);
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
	@Test(expected=TimeoutException.class)
	public void t1_1_loginTimeout() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.logout();

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
	@Test
	public void t1_1_login() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testAnotherUsername", "NoPassword");
			fail("It must not login");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test the login process bug when using invalid case in username. It should
	 * not login
	 * 
	 * TODO: Stendhal 0.6x allowed different cases so we are not going to 
	 *   consider this bug for now.
	 * 
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void t2_loginBadCase() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testusername", "password");
			fail("It must not login");
		} catch (LoginFailedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test the login process by login twice on the server. It should logout the
	 * previous login
	 * 
	 * @throws Exception
	 */
	@Test
	public void t3_loginDouble() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");
			
			MockClient altClient = new MockClient("log4j.properties");
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
		} catch(TimeoutException e) {		
		}
	}

	/**
	 * Test the create character process.
	 * 
	 * @throws Exception
	 */
	@Test
	public void t4_createCharacter() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			RPObject template = new RPObject();
			template.put("client", "junit");

			CharacterResult res = client.createCharacter("testCharacter", template);
			assertEquals(res.getResult(),Result.OK_CREATED);
			assertEquals("testCharacter", res.getCharacter());

			RPObject result = res.getTemplate();
			assertTrue(result.has("client"));
			assertEquals("junit", result.get("client"));
			assertTrue(result.has("name"));
			assertEquals("testCharacter", result.get("name"));

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);
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
	@Test
	public void t4_1_createCharacterFailure() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			RPObject template = new RPObject();
			template.put("client", "junit");

			CharacterResult res = client.createCharacter("ter", template);
			assertTrue(res.getResult().failed());
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
	@Test
	public void t5_chooseCharacter() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			client.logout();
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
	@Test
	public void t5_1_chooseWrongCharacter() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testAnotherCharacter");
			assertFalse(choosen);

			client.logout();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}	
	/**
	 * Test the perception management in game.
	 */
	@Test
	public void t6_receivePerceptions() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);
			
			RPAction action=new RPAction();
			action.put("text", 1);
			client.send(action);

			while (client.getPerceptions() < 5) {
				assertTrue(client.getConnectionState());
				client.loop(0);
			}

			client.logout();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	/*
	 * TODO: Write test case for Logout NACK.
	 * TODO: Remove, modify and add objects on perceptions.
	 */

	@Test
	public void t7_testKeepAlive() throws Exception {
		try {
			client=new MockClient("log4j.properties") {
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
			
			RPAction action=new RPAction();
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
			
			assertFalse("Connection must be broken as connection was closed by server", client.getConnectionState());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void t7_1_testKeepAliveWorks() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			long init=System.currentTimeMillis();
			/*
			 * Timeout for players is 30 seconds.
			 */			
			while (System.currentTimeMillis()-init<40000) {
				client.loop(0);
				Thread.sleep(50);
			}
			
			assertTrue("Connection still be there", client.getConnectionState());

			client.logout();			
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void t8_testBannedIP() throws IOException, InvalidVersionException, TimeoutException,
	        LoginFailedException {
		MockRPRuleProcessor rp = (MockRPRuleProcessor) MockRPRuleProcessor.get();
		ConnectionValidator conn = rp.getValidator();

		conn.addBan("127.0.0.1", "0.0.0.0", 20);

		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			fail();
		} catch (BannedAddressException e) {
			/*
			 * Good.
			 */
		}
	}

}
