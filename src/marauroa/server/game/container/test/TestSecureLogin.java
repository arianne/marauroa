package marauroa.server.game.container.test;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;

import marauroa.common.Configuration;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAKey;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;

import org.junit.BeforeClass;
import org.junit.Test;

public class TestSecureLogin {
	private static PlayerEntryContainer cont;
	private static RSAKey key;	
	
	/**
	 * Initialize the container.
	 * @throws IOException 
	 *
	 */
	@BeforeClass
	public static void setUp() throws IOException {
		cont=PlayerEntryContainer.getContainer();

		key = new RSAKey(
				new BigInteger(Configuration.getConfiguration().get("n")), 
				new BigInteger(Configuration.getConfiguration().get("d")), 
				new BigInteger(Configuration.getConfiguration().get("e")));
	}

	@Test
	public void testLogin() throws SQLException {
		try {
		String password="password";
		byte[] serverNonce=Hash.random(Hash.hashLength());
		byte[] clientNonce=Hash.random(Hash.hashLength());
		
		byte[] clientNonceHash=Hash.hash(clientNonce);
		
		PlayerEntry.SecuredLoginInfo login=new PlayerEntry.SecuredLoginInfo(key, clientNonceHash, serverNonce);

		byte[] b1 = Hash.xor(clientNonce, Hash.hash(serverNonce));
		if (b1 == null) {
			fail("B1 is null");
		}

		byte[] b2 = Hash.xor(b1, Hash.hash(password));
		if (b2 == null) {
			fail("B2 is null");
		}

		byte[] cryptedPassword = key.encodeByteArray(b2);
		
		login.username="testUsername";
		login.clientNonce=clientNonce;
		login.password=cryptedPassword;
		
		boolean result=login.verify();
		
		assertTrue(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
