package marauroa.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import marauroa.client.CreateAccountFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.net.InvalidVersionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SystemTest {
	private MockClient client;

	@Before
	public void createClient() {
		client=new MockClient("log4j.properties");
	}

	@After
	public void disconnectClient() {
		client=new MockClient("log4j.properties");
	}

	@Test
	public void createaccount() throws IOException, TimeoutException, InvalidVersionException, CreateAccountFailedException {
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

}
