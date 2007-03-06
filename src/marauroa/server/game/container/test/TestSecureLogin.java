package marauroa.server.game.container.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

/**
 * Test the secure login procedure in the same way. 
 * 
 * @author miguel
 *
 */
public class TestSecureLogin {
	private static RSAKey key;	
	
	/**
	 * Initialize the container.
	 * @throws IOException 
	 *
	 */
	@BeforeClass
	public static void setUp() throws IOException {
		/*
		 * Make sure database is initialized-
		 */
		PlayerEntryContainer.getContainer();

		key = new RSAKey(
				new BigInteger(Configuration.getConfiguration().get("n")), 
				new BigInteger(Configuration.getConfiguration().get("d")), 
				new BigInteger(Configuration.getConfiguration().get("e")));
	}

	/**
	 * This method suppose that you have an account already created with username testUsername and password password.
	 * It test if verify works correctly with a correct account.
	 * @throws SQLException
	 */
	@Test
	public void testLogin() throws SQLException {
		String password="password";
		
		assertTrue(simulateSecureLogin(password));
	}

	/**
	 * This method suppose that you have an account already created with username testUsername and password password.
	 * It test if verify works correctly with a bad password.
	 * @throws SQLException
	 */
	@Test
	public void testLoginFailure() throws SQLException {
		String password="badpassword";

		assertFalse(simulateSecureLogin(password));
	}
	private boolean simulateSecureLogin(String password) throws SQLException {
		byte[] serverNonce=Hash.random(Hash.hashLength());
		byte[] clientNonce=Hash.random(Hash.hashLength());
		
		byte[] clientNonceHash=Hash.hash(clientNonce);
		byte[] serverNonceHash=Hash.hash(serverNonce);
		
		PlayerEntry.SecuredLoginInfo login=new PlayerEntry.SecuredLoginInfo(key, clientNonceHash, serverNonce);

		byte[] b1 = Hash.xor(clientNonceHash, serverNonce);
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
		
		return login.verify();
	}
}
