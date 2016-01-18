package marauroa.clientconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import marauroa.client.ClientFramework;
import marauroa.client.LoginFailedException;
import marauroa.common.game.AccountResult;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;
import marauroa.helper.ResetMarauroaSingleton;
import marauroa.server.marauroad;

/**
 * tests for the marauroa network stack
 */
public class ClientConnectTest {

	/**
	 * starts a marauroa server
	 * 
	 * @throws Exception in case of an unexpected error
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String filename = "./functional_tests/marauroa/clientconnect/testserver.ini";
		assertTrue(new File(filename).exists());
		String[] args = new String[] { "-c", filename };
		marauroad.main(args);
		Thread.sleep(1000);
	}

	/**
	 * cleanup marauroa
	 *
	 * @throws SecurityException in case of a security error
	 * @throws NoSuchFieldException in cases of an unexpected refactoring
	 * @throws IllegalArgumentException in case of an illegal argument
	 * @throws IllegalAccessException in case of a security error
	 */
	@AfterClass
	public static void afterclass() throws IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException {
		ResetMarauroaSingleton.sysoutthreads();
	}

	/**
	 * tests connecting to a marauroa server and executing commands
	 *
	 * @throws Exception in case of an exception
	 */
	@Test
	public void clientconnectTest() throws Exception {
		ClientFramework cl = new MinimalClient();

		cl.connect("localhost", 12300);
		assertEquals(new AccountResult(Result.OK_CREATED,"hugo").toString(),cl.createAccount("hugo", "pw2", "emil").toString());
		assertEquals(new AccountResult(Result.FAILED_PLAYER_EXISTS,"hugo").toString(),cl.createAccount("hugo", "pw2", "emil").toString());

		cl.login("hugo", "pw2");
		cl.logout();
	}

	/**
	 * tests rejection on wrong password
	 *
	 * @throws Exception in cae of an unexpected error
	 */
	@Test (expected=LoginFailedException.class)
	public void wrongPwTest() throws Exception {
		ClientFramework cl = new MinimalClient();

		cl.connect("localhost", 12300);
		assertEquals(new AccountResult(Result.OK_CREATED,"haxor").toString(),cl.createAccount("haxor", "goodpw", "emil").toString());
		cl.login("haxor", "badpw");
		cl.logout();
	}

	/**
	 * tests creation of characters
	 *
	 * @throws Exception in case of an unexpected error
	 */
	@Test
	public void createCharacterTest() throws Exception {
		ClientFramework cl = new MinimalClient();

		cl.connect("localhost", 12300);
		assertEquals(new AccountResult(Result.OK_CREATED,"character").toString(),cl.createAccount("character", "pw2", "emil").toString());

		cl.login("character", "pw2");
		assertEquals(Result.OK_CREATED,cl.createCharacter("jack", new RPObject()).getResult());
		assertEquals(Result.FAILED_CHARACTER_EXISTS, cl.createCharacter("jack", new RPObject()).getResult());

		cl.logout();
	}

	final class MinimalClient extends ClientFramework {
		@Override
		protected List<TransferContent> onTransferREQ(List<TransferContent> items) {
			return null;
		}

		@Override
		protected void onTransfer(List<TransferContent> items) {
			// ignored
		}

		@Override
		protected void onServerInfo(String[] info) {
			// ignored
		}

		@Override
		protected void onPreviousLogins(List<String> previousLogins) {
			// ignored
		}

		@Override
		protected void onPerception(MessageS2CPerception message) {
			// ignored
		}

		@Override
		protected void onAvailableCharacters(String[] characters) {
			// ignored
		}

		@Override
		protected String getVersionNumber() {
			return "versionnumber";
		}

		@Override
		protected String getGameName() {
			return "clientconnecttest";
		}
	}

}
