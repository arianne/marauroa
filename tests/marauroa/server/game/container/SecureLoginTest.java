package marauroa.server.game.container;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;

import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAKey;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the secure login procedure in the same way.
 * 
 * @author miguel
 * 
 */
public class SecureLoginTest {

	private static RSAKey key;

	/**
	 * Initialize the container.
	 * 
	 * @throws IOException
	 * 
	 */
	@BeforeClass
	public static void setUp() throws IOException {
		/*
		 * Make sure database is initialized-
		 */
		PlayerEntryContainer.getContainer();
		loadRSAKey();
	}

	public static void loadRSAKey() throws IOException {
		key = new RSAKey(new BigInteger("2408376769632966826891253753617412746862686794740723175774423430043927850498085639220684795629747326949838501777926669337171495421818563824539329224927899179237"), 
				new BigInteger("2247818318324102371765170170042918563738507675091341630722795201374332660464879838332237004076252849654527963214772652641735279016325354691167883850414929419335"), 
		        new BigInteger("15"));
	}

	/**
	 * This method supposes that you have an account already created with
	 * username testUsername and password password. It test if verify works
	 * correctly with a correct account.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testLogin() throws SQLException {
		String password = "password";
		PlayerEntry.SecuredLoginInfo login = simulateSecureLogin("testUsername", password);
		assertTrue(login.verify());
	}

	/**
	 * This method suppose that you have an account already created with
	 * username testUsername and password password. It test if verify works
	 * correctly with a bad password.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testLoginFailure() throws SQLException {
		String password = "badpassword";

		PlayerEntry.SecuredLoginInfo login = simulateSecureLogin("testUsername", password);
		assertFalse(login.verify());
	}

	public static PlayerEntry.SecuredLoginInfo simulateSecureLogin(String username, String password)
	        throws SQLException {
		byte[] serverNonce = Hash.random(Hash.hashLength());
		byte[] clientNonce = Hash.random(Hash.hashLength());

		byte[] clientNonceHash = Hash.hash(clientNonce);

		PlayerEntry.SecuredLoginInfo login = new PlayerEntry.SecuredLoginInfo(key, clientNonceHash,
		        serverNonce);

		byte[] b1 = Hash.xor(clientNonce, serverNonce);
		if (b1 == null) {
			fail("B1 is null");
		}

		byte[] b2 = Hash.xor(b1, Hash.hash(password));
		if (b2 == null) {
			fail("B2 is null");
		}

		byte[] cryptedPassword = key.encodeByteArray(b2);

		login.username = username;
		login.clientNonce = clientNonce;
		login.password = cryptedPassword;

		return login;
	}
}
