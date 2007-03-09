package marauroa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import marauroa.client.CreateAccountFailedException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
/**
 * Test the whole system.
 * Mostly from the client perspective.
 *
 * @author miguel
 *
 */
public class SystemTest {
	private MockClient client;

	/**
	 * Create a new client each time
	 *
	 */
	@Before
	public void createClient() {
		client=new MockClient("log4j.properties");
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
	 * Test create account process by creating an account and it should work
	 * and create the account again and failing.
	 *
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws InvalidVersionException
	 * @throws CreateAccountFailedException
	 */
	@Test
	public void createAccount() throws IOException, TimeoutException, InvalidVersionException, CreateAccountFailedException {
		client.connect("localhost", 3217);
		AccountResult res=client.createAccount("testUsername", "password", "email");

		assertEquals("testUsername",res.getUsername());

		/*
		 * Doing a second time should fail
		 */
		try {
			client.createAccount("testUsername", "password", "email");
			fail("Created two accounts with the same name");
		} catch(CreateAccountFailedException e) {
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
			client.connect("localhost",3217);
			client.login("testUsername", "password");

			String[] characters=client.getCharacters();
			assertEquals(0, characters.length);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Test the login process bug when using invalid case in username.
	 * It should not logint
	 *
	 * @throws Exception
	 */
	@Test
	public void loginBadCase() throws Exception {
		try {
			client.connect("localhost",3217);
			client.login("testusername", "password");
			fail("It must not login");
		} catch(LoginFailedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Test the login process by login twice on the server.
	 * It should logout the previous login
	 *
	 * @throws Exception
	 */
	@Test
	public void loginDouble() throws Exception {
		try {
			client.connect("localhost",3217);
			client.login("testUsername", "password");
			client.login("testUsername", "password");

			String[] characters=client.getCharacters();
			assertEquals(0, characters.length);
		} catch(Exception e) {
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
			client.connect("localhost",3217);
			client.login("testUsername", "password");

			RPObject template=new RPObject();
			template.put("client", "junit");

			CharacterResult res=client.createCharacter("testCharacter", template);
			assertEquals("testCharacter",res.getCharacter());

			RPObject result=res.getTemplate();
			assertTrue(result.has("client"));
			assertEquals("junit", result.get("client"));
			assertTrue(result.has("name"));
			assertEquals("testCharacter", result.get("name"));

			String[] characters=client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);
		} catch(Exception e) {
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
			client.connect("localhost",3217);
			client.login("testUsername", "password");

			String[] characters=client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen=client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			client.logout();
		} catch(Exception e) {
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
			client.connect("localhost",3217);
			client.login("testUsername", "password");

			String[] characters=client.getCharacters();
			assertEquals(1, characters.length);
			assertEquals("testCharacter", characters[0]);

			boolean choosen=client.chooseCharacter("testCharacter");
			assertTrue(choosen);

			while(client.getPerceptions()<5) {
				client.loop(0);
			}

			client.logout();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
