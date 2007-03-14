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
import marauroa.server.marauroad;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class CrushServer {
	private static int index;
	private int completed;
	private static final int NUM_CLIENTS = 200;

	private static final boolean DETACHED_SERVER=false;
	private static marauroad server;

	@BeforeClass
	public static void createServer() throws InterruptedException {
		if(!DETACHED_SERVER) {
			Log4J.init("log4j.properties");
			server = marauroad.getMarauroa();
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

	@AfterClass
	public static void takeDownServer() {
		if(!DETACHED_SERVER) {
			server.finish();
		}
	}


	/**
	 * Test the perception management in game.
	 */
	@Ignore
	@Test
	public void crushServer() throws Exception {
		for(int i=0;i<NUM_CLIENTS;i++) {
			new Thread() {
				public void run() {
					try {
						int i=index++;
						MockClient client=new MockClient("log4j.properties");

						Thread.sleep(Math.abs(new Random().nextInt()%200)*60000);

						client.connect("localhost",3217);
						AccountResult resAcc=client.createAccount("testUsername"+i, "password", "email");
						assertEquals("testUsername"+i,resAcc.getUsername());

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

						client.logout();

						for(int j=0;j<10;j++) {
							client.login("testUsername"+i, "password");

							boolean choosen=client.chooseCharacter("testCharacter");
							assertTrue(choosen);

							int amount=new Random().nextInt()%4000;
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

								if(new Random().nextInt()%1000==0) {
									/*
									 * Randomly close the connection
									 */
									System.out.println("FORCED CLOSE CONNECTION: Testint random disconnects on server");
									client.close();
									return;
								}
							}

							client.logout();
							client.close();
							Thread.sleep(Math.abs(new Random().nextInt()%60)*1000);
						}
					} catch(Exception e) {
						e.printStackTrace();
						fail("Exception");
					} finally {
						completed++;
					}
				}
			}.start();
		}

		while(completed!=NUM_CLIENTS) {
			Thread.sleep(1000);
		}
	}
}
