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
import marauroa.common.game.Result;
import marauroa.common.net.NetConst;
import marauroa.server.marauroad;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the server and client framework y running lots of clients several times
 * against a server. The test is sucessfull if all client complete correctly.
 *
 * @author miguel
 *
 */
public class CrushServer {

	private static final int PORT = 3217;

	private static final int TIMES_TO_LOGIN = 10;

	private static int index;

	private int completed;

	private static final int NUM_CLIENTS = 150;

	private static final boolean DETACHED_SERVER = true;

	private static MockMarauroad server;

	static class MockMarauroad extends marauroad {

		public MockMarauroad() {
			super();
		}

	}

	@BeforeClass
	public static void createServer() throws InterruptedException {
		if (!DETACHED_SERVER) {
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
	 * Test the perception management in game.
	 */
	@Ignore
	@Test
	public void crushServer() throws Exception {
		for (int i = 0; i < NUM_CLIENTS; i++) {
			new Thread() {

				int i;

				public void run() {
					try {
						System.out.println("Initing client");
						i = index++;
						MockClient client = new MockClient("client.properties");

						Thread.sleep(Math.abs(new Random().nextInt() % 20) * 1000);

						client.connect("localhost", PORT);
						AccountResult resAcc = client.createAccount("testUsername" + i, "password", "email");
						assertEquals("testUsername" + i, resAcc.getUsername());
						assertEquals(Result.OK_CREATED, resAcc.getResult());

						Thread.sleep(Math.abs(new Random().nextInt() % 100) * 1000 + 5000);

						client.login("testUsername" + i, "password");

						RPObject template = new RPObject();
						template.put("client", "junit" + i);
						CharacterResult resChar = client.createCharacter("testCharacter", template);
						assertEquals("testCharacter", resChar.getCharacter());

						RPObject result = resChar.getTemplate();
						assertTrue(result.has("client"));
						assertEquals("junit" + i, result.get("client"));

						client.logout();

						for (int logins = 0; logins < TIMES_TO_LOGIN; logins++) {
							Thread.sleep(Math.abs(new Random().nextInt() % 30) * 1000 + 5000);
							client.login("testUsername" + i, "password");

							String[] characters = client.getCharacters();
							assertEquals(1, characters.length);
							assertEquals("testCharacter", characters[0]);

							boolean choosen = client.chooseCharacter("testCharacter");
							assertTrue(choosen);

							int amount = Math.abs(new Random().nextInt() % 90) + 10;
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

								if (new Random().nextInt() % 1000 == 0) {
									/*
									 * Randomly close the connection
									 */
									System.out.println("FORCED CLOSE CONNECTION: Testint random disconnects on server");
									client.close();
									return;
								}

								Thread.sleep(1000);
							}

							client.logout();
						}

						client.close();
						Thread.sleep(Math.abs(new Random().nextInt() % 60) * 1000);

					} catch (Exception e) {
						System.out.println("Problem for player: testUsername" + i);
						e.printStackTrace();
						System.exit(-1);
					} finally {
						completed++;
					}
				}
			}.start();
		}

		while (completed != NUM_CLIENTS) {
			Thread.sleep(10000);
			System.out.println("Still missing to complete: " + (NUM_CLIENTS - completed));
		}
	}
}
