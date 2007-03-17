package marauroa.test;

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
import marauroa.common.net.NetConst;
import marauroa.test.CrushServer.MockMarauroad;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class StressServer {
	private static final int PORT= 3219;
	private static int index;
	private int completed;
	private static final int NUM_CLIENTS = 10;

	private static final boolean DETACHED_SERVER=false;
	private static MockMarauroad server;


	@BeforeClass
	public static void createServer() throws InterruptedException {
		if(!DETACHED_SERVER) {
			Log4J.init("log4j.properties");
			server = new MockMarauroad();
			Configuration.setConfigurationFile("src/marauroa/test/server.ini");

			try {
				Configuration.getConfiguration();
			} catch (Exception e) {
				fail("Unable to find configuration file");
			}

			/*
			 * Ugly hack, but junit does runs test cases in parallel 
			 */
			NetConst.tcpPort=PORT;

			server.start();
			Thread.sleep(2000);
		}
	}

	@AfterClass
	public static void takeDownServer() throws InterruptedException {
		if(!DETACHED_SERVER) {
			server.finish();
			Thread.sleep(2000);
		}
	}

	/**
	 * Test the perception management in game.
	 */
	@Test
	public void stressServer() throws Exception {
		for(int i=0;i<NUM_CLIENTS;i++) {
			new Thread() {
				public void run() {
					try {
						System.out.println("Initing client");
						int i=index++;
						MockClient client=new MockClient("client.properties");

						client.connect("localhost",PORT);
						AccountResult resAcc=client.createAccount("testUsername"+i, "password", "email");
						assertEquals("testUsername"+i,resAcc.getUsername());

						Thread.sleep(Math.abs(new Random().nextInt()%20)*1000);

						client.login("testUsername"+i, "password");

						RPObject template=new RPObject();
						template.put("client", "junit"+i);
						CharacterResult resChar=client.createCharacter("testCharacter", template);
						assertEquals("testCharacter",resChar.getCharacter());

						RPObject result=resChar.getTemplate();
						assertTrue(result.has("client"));
						assertEquals("junit"+i, result.get("client"));

						String[] characters=client.getCharacters();
						assertEquals(1, characters.length);
						assertEquals("testCharacter", characters[0]);

						boolean choosen=client.chooseCharacter("testCharacter");
						assertTrue(choosen);

						int amount=Math.abs(new Random().nextInt()%30)+10;
						while(client.getPerceptions()<amount) {
							client.loop(0);

							if(new Random().nextInt()%10==0) {
								/*
								 * Send an action to server.
								 */
								RPAction action=new RPAction();
								action.put("type","chat");
								action.put("text","Hello world");
								client.send(action);
							}

							for(RPObject object: client.getObjects().values()) {
								if(object.has("hidden")) {
									fail("Not expected hidden object");
								}
							}


							Thread.sleep(1000);
							}

						System.out.println("Trying to leave server");
						assertTrue(client.logout());
						client.close();
						System.out.println("Leaved the server");
					} catch(Exception e) {
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
		while(completed!=NUM_CLIENTS) {
			System.out.println("Completed: "+completed);
			Thread.sleep(1000);
		}
	}

}
