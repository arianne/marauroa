package marauroa.common;

import java.security.GeneralSecurityException;

import org.junit.Assert;
import org.junit.Test;

import marauroa.common.crypto.Hash;
import marauroa.common.crypto.SymmetricKey;

/***
 * Tests for symmetric key crypto
 */
public class SymmetricKeyTest {

	/**
	 * Tests symmetric key crypto
	 *
	 * @throws GeneralSecurityException in case of a security error
	 */
	@Test
	public void testSymmetricKey() throws GeneralSecurityException {
		byte[] key = Hash.random(16);
		byte[] init = Hash.random(16);

		SymmetricKey symmetricKey = new SymmetricKey(key);
		byte[] data = new byte[] {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4};

		byte[] encrypted = symmetricKey.encrypt(init, data);
		byte[] res = symmetricKey.decrypt(init, encrypted);
		Assert.assertArrayEquals(data, res);
	}
}
