/* $Id: StressServer.java,v 1.3 2008/04/18 18:53:25 martinfuchs Exp $ */
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
package marauroa.functional.load;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.functional.IFunctionalTest;
import marauroa.functional.MarauroadLauncher;
import marauroa.functional.SimpleClient;

public class StressServer implements IFunctionalTest {

	private static final int PORT = 3219;

	private static int index;

	private int completed;

	private static final int NUM_CLIENTS = 10;

	private static final boolean DETACHED_SERVER = true;

	private static MarauroadLauncher server;

	public void setUp() throws Exception {
		if (!DETACHED_SERVER) {
			Log4J.init("log4j.properties");
			server = new MarauroadLauncher(PORT);
			Configuration.setConfigurationFile("src/marauroa/test/server.ini");

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

	/**
	 * Test the perception management in game.
	 */
	public void launch() throws Exception {
		for (int i = 0; i < NUM_CLIENTS; i++) {
			new Thread() {

				@Override
				public void run() {
					try {
						System.out.println("Initing client");
						int i = index++;
						SimpleClient client = new SimpleClient("client.properties");

						client.connect("localhost", PORT);

						AccountResult resAcc = client.createAccount("testUsername" + i, "password", "email");
						assertTrue("Account creation must not fail", !resAcc.failed());
						assertEquals("testUsername" + i, resAcc.getUsername());

						Thread.sleep(Math.abs(new Random().nextInt() % 20) * 1000);

						client.login("testUsername" + i, "password");

						RPObject template = new RPObject();
						template.put("client", "junit" + i);
						CharacterResult resChar = client.createCharacter("testCharacter", template);
						assertEquals("testCharacter", resChar.getCharacter());

						RPObject result = resChar.getTemplate();
						assertTrue(result.has("client"));
						assertEquals("junit" + i, result.get("client"));

						String[] characters = client.getCharacters();
						assertEquals(1, characters.length);
						assertEquals("testCharacter", characters[0]);

						boolean choosen = client.chooseCharacter("testCharacter");
						assertTrue(choosen);

						int amount = Math.abs(new Random().nextInt() % 30) + 10;
						while (client.getPerceptions() < amount) {
							client.loop(0);

							if (new Random().nextInt() % 10 == 0) {
								/*
								 * Send an action to server.
								 */
								RPAction action = new RPAction();
								action.put("type", "chat");
								action.put("text", "Hello world");
								client.send(action);
							}

							for (RPObject object : client.getObjects().values()) {
								if (object.has("hidden")) {
									fail("Not expected hidden object");
								}
							}

							Thread.sleep(1000);
						}

						System.out.println("Trying to leave server");
						assertTrue(client.logout());
						client.close();
						System.out.println("Leaved the server");
					} catch (Exception e) {
						e.printStackTrace();
						fail("Exception");
					} finally {
						System.out.println("Completed.");
						completed++;
					}
				}
			}.start();
		}

		/*
		 */
		while (completed != NUM_CLIENTS) {
			System.out.println("Completed: " + completed);
			Thread.sleep(1000);
		}
	}

}
