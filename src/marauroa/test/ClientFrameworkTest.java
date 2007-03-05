package marauroa.test;

import static org.junit.Assert.fail;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.net.InvalidVersionException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientFrameworkTest {
	MockClientFramework client ;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Before
	public void setup() throws Exception {
		client = new MockClientFramework("");
	}

	@Test
	public final void testClientFramework() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testConnect() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testLogin() {
		try {
			client.login("JohnDo", "empty");
		} catch (InvalidVersionException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (TimeoutException e) {
			e.printStackTrace();
			fail(e.toString());
		} catch (LoginFailedException e) {

			e.printStackTrace();
			fail(e.toString());
		}

	}

	@Test
	public final void testChooseCharacter() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testCreateAccount() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testSend() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testLogout() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testLoop() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetConnectionState() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testOnPerception() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testOnTransferREQ() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testOnTransfer() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testOnAvailableCharacters() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testOnServerInfo() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetGameName() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testGetVersionNumber() {
		fail("Not yet implemented"); // TODO
	}

}
