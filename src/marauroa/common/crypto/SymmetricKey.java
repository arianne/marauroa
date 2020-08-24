package marauroa.common.crypto;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Symmetric Key crypto
 *
 * @author hendrik
 */
public class SymmetricKey {
	private byte[] key;

	/**
	 * creates a new SymmetricKey
	 *
	 * @param key key (at least 128 random bits)
	 */
	public SymmetricKey(byte[] key) {
		this.key = Arrays.copyOf(key, key.length);
	}

	/**
	 * encrypts data
	 *
	 * @param initialisationVector  random data of the same length as the key
	 * @param data data to encrypt
	 * @return encrypted data
	 * @throws GeneralSecurityException in case of a security error
	 */
    public byte[] encrypt(byte[] initialisationVector, byte[] data) throws GeneralSecurityException {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(initialisationVector));
        byte[] encrypted = cipher.doFinal(data);
        return encrypted;
    }

	/**
	 * deencrypts data
	 *
	 * @param initialisationVector  random data of the same length as the key
	 * @param encrypted encrytped data to decrypt
	 * @return encrypted data
	 * @throws GeneralSecurityException in case of a security error
	 */
    public byte[] decrypt(byte[] initialisationVector, byte[] encrypted) throws GeneralSecurityException {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(initialisationVector));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

}
