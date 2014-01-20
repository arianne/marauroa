package marauroa.clientconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import marauroa.client.ClientFramework;
import marauroa.client.LoginFailedException;
import marauroa.common.game.AccountResult;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;
import marauroa.helper.ResetMarauroaSingleton;
import marauroa.server.marauroad;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClientConnectTest {


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		File db = new File("./functional_tests/marauroa/clientconnect/clientconnect.h2.db");
		if (db.exists()) {
			db.delete();
		}


		String filename = "./functional_tests/marauroa/clientconnect/clientconnect.ini";
		assertTrue(new File(filename).exists());
		String[] args = new String[] { "-c", filename };
		marauroad.main(args);
		Thread.sleep(1000);

	}

	@AfterClass
	public static void afterclass() throws IllegalArgumentException, SecurityException,
			IllegalAccessException, NoSuchFieldException {
		ResetMarauroaSingleton.sysoutthreads();

	}

	@Test
	public void clientconnectTest() throws Exception {
		ClientFramework cl = new MinimalClient();

		cl.connect("localhost", 12300);
		assertEquals(new AccountResult(Result.OK_CREATED,"hugo").toString(),cl.createAccount("hugo", "pw2", "emil").toString());
		assertEquals(new AccountResult(Result.FAILED_PLAYER_EXISTS,"hugo").toString(),cl.createAccount("hugo", "pw2", "emil").toString());

		cl.login("hugo", "pw2");
		cl.logout();
	}


	@Test (expected=LoginFailedException.class)
	public void wrongPwTest() throws Exception {
		ClientFramework cl = new MinimalClient();

		cl.connect("localhost", 12300);
		assertEquals(new AccountResult(Result.OK_CREATED,"haxor").toString(),cl.createAccount("haxor", "goodpw", "emil").toString());
		cl.login("haxor", "badpw");
		cl.logout();
	}

	@Test
	public void createCharacterTest() throws Exception {
		ClientFramework cl = new MinimalClient();

		cl.connect("localhost", 12300);
		assertEquals(new AccountResult(Result.OK_CREATED,"character").toString(),cl.createAccount("character", "pw2", "emil").toString());

		cl.login("character", "pw2");
		assertEquals(Result.OK_CREATED,cl.createCharacter("jack", new RPObject()).getResult());
		//XXX shouldnt this be Result.FAILED_CHARACTER_EXISTS?
		assertEquals(Result.FAILED_PLAYER_EXISTS,cl.createCharacter("jack", new RPObject()).getResult());

		cl.logout();
	}

	private final class MinimalClient extends ClientFramework {
		@Override
		protected List<TransferContent> onTransferREQ(List<TransferContent> items) {
			return null;
		}

		@Override
		protected void onTransfer(List<TransferContent> items) {

		}

		@Override
		protected void onServerInfo(String[] info) {

		}

		@Override
		protected void onPreviousLogins(List<String> previousLogins) {
		}

		@Override
		protected void onPerception(MessageS2CPerception message) {

		}

		@Override
		protected void onAvailableCharacters(String[] characters) {

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
