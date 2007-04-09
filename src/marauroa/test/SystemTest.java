package marauroa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import marauroa.client.BannedAddressException;
import marauroa.client.CreateAccountFailedException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.NetConst;
import marauroa.server.net.validator.ConnectionValidator;
import marauroa.test.CrushServer.MockMarauroad;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
	public void createAccount() throws IOException, TimeoutException, InvalidVersionException,
			CreateAccountFailedException, BannedAddressException {
		client.connect("localhost", PORT);
		AccountResult res = client.createAccount("testUsername", "password", "email");

		assertEquals("testUsername", res.getUsername());

		/*
		 * Doing a second time should fail
		 */
		try {
			client.createAccount("testUsername", "password", "email");
			fail("Created two accounts with the same name");
		} catch (CreateAccountFailedException e) {
			assertTrue("Account should not be created as it already exists.", true);
		}
	}

	/**
	 * Test the login process.
	 * 
	 * @throws Exception
	 */
	@Test
	public void login() throws Exception {
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
	 * Test the login process bug when using invalid case in username. It should
	 * not logint
	 * 
	 * @throws Exception
	 */
	@Test
	public void loginBadCase() throws Exception {
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
	public void loginDouble() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(0, characters.length);
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
	public void createCharacter() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			RPObject template = new RPObject();
			template.put("client", "junit");

			CharacterResult res = client.createCharacter("testCharacter", template);
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
	public void chooseCharacter() throws Exception {
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
	 * Test the perception management in game.
	 */
	@Test
	public void receivePerceptions() throws Exception {
		try {
			client.connect("localhost", PORT);
			client.login("testUsername", "password");

			String[] characters = client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			while (client.getPerceptions() < 5) {
				client.loop(0);
			}

			client.logout();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testBannedIP() throws IOException, InvalidVersionException, TimeoutException, LoginFailedException {
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
