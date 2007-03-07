package marauroa.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import marauroa.client.CreateAccountFailedException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
		client=null;
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
		client.createAccount("testUsername", "password", "email");

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

			client.createCharacter("testCharacter", template);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
