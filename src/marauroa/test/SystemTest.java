package marauroa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Random;

import marauroa.client.CreateAccountFailedException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;
import marauroa.server.marauroad;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
	private static final int NUM_CLIENTS = 60;
	
	private MockClient client;
  	private static marauroad server;
  
  	@BeforeClass
  	public static void createServer() {
  		Log4J.init("marauroa/server/log4j.properties");
  		server = marauroad.getMarauroa();
  		Configuration.setConfigurationFile("src/marauroa/test/server.ini");
  
  		try {
  			Configuration.getConfiguration();
  		} catch (Exception e) {
  			fail("Unable to find configuration file");
  		}
  		
  		server.start();
  	}
  
  	@AfterClass
  	public static void takeDownServer() {
  		server.finish();
  	}
	
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

	private static int index;
	private int completed;

	/**
	 * Test the perception management in game.
	 */
	@Test
	public void stressServer() throws Exception {
		for(int i=0;i<NUM_CLIENTS;i++) {
			new Thread() {
				public void run() {
					try {
						int i=index++;
						MockClient client=new MockClient("log4j.properties");

						Thread.sleep(Math.abs(new Random().nextInt()%20000));
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

						boolean choosen=client.chooseCharacter("testCharacter");
						assertTrue(choosen);

						int amount=new Random().nextInt()%30;
						while(client.getPerceptions()<amount) {
							client.loop(0);
						}

						client.logout();
						client.close();
					} catch(Exception e) {
						e.printStackTrace();
						fail("Exception");
					} finally {
						completed++;
					}
				}
			}.start();
		}
		
		/*
		 *   20000 ms of random sleep
		 * +  5000 ms of thread execution time 
		 * + 15000 ms of safety.
		 */
		while(completed!=NUM_CLIENTS) {
			Thread.sleep(1000); 
		}
	}

	/**
	 * Test the perception management in game.
	 */
	@Ignore
	@Test
	public void crushServer() throws Exception {
		for(int i=0;i<200;i++) {
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
					}					
				}
			}.start();
		}

		Thread.sleep(3600000); 
	}
}
