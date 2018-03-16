/***************************************************************************
 *                   (C) Copyright 2007-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.crypto;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * This class is used to create Hashes of byte arrays.
 * It is thread safe.
 *
 * This class require from JVM that it provides MD5 digest
 * and SHA1PRNG random number generator.
 *
 * @author quisar
 */
public class Hash {
	private static Logger logger = Log4J.getLogger(Hash.class);

	private static String hex = "0123456789ABCDEF";

	static private MessageDigest md;

	static private SecureRandom random;

	static {
		try {
			md = MessageDigest.getInstance("MD5");
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return a hash of the given argument.
	 * It is thread safe.
	 *
	 * @param value a string
	 * @return the hash of the string.
	 */
	synchronized public static final byte[] hash(final String value) {
		try {
			return hash(value.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e, e);
			return null;
		}
	}

	/**
	 * Return the length of the hash digest in bytes.
	 * @return  the length of the hash digest in bytes.
	 */
	synchronized public static final int hashLength() {
		return md.getDigestLength();
	}

	/**
	 * Return the hash of an array of bytes.
	 * This method is thread safe.
	 *
	 * @param value an array of bytes.
	 * @return the hash of an array of bytes.
	 */
	synchronized public static final byte[] hash(final byte[] value) {
		md.reset();
		md.update(value);
		return md.digest();
	}

	/**
	 * Returns the XOR of two arrays of bytes of the same size otherwise it returns null.
	 *
	 * @param b1 an array of bytes.
	 * @param b2 an array of bytes.
	 * @return an array of bytes containing the xor of b1 and b2
	 */
	public static final byte[] xor(final byte[] b1, final byte[] b2) {
		if (b1.length != b2.length) {
			return null;
		}
		byte[] res = new byte[b1.length];
		for (int i = 0; i < b1.length; i++) {
			res[i] = (byte) (b1[i] ^ b2[i]);
		}
		return res;
	}

	/**
	 * Compare two arrays of bytes so that it returns a negative integer, zero,
	 * or a positive integer as the first argument is less than, equal to, or
	 * greater than the second.
	 *
	 * @param b1 an array of bytes.
	 * @param b2 an array of bytes.
	 *
	 * @return a negative integer, zero,
	 * or a positive integer as the first argument is less than, equal to, or
	 * greater than the second.
	 */
	public static final int compare(final byte[] b1, final byte[] b2) {
		if (b1.length != b2.length) {
			return (b1.length - b2.length);
		}
		for (int i = 0; i < b1.length; i++) {
			if (b1[i] != b2[i]) {
				return b1[i] - b2[i];
			}
		}
		return 0;
	}

	/**
	 * Generate an array of bytes of nbBytes size.
	 *
	 * @param nbBytes size of the array.
	 * @return an array of bytes of nbBytes size.
	 */
	synchronized public static final byte[] random(int nbBytes) {
		byte[] res = new byte[nbBytes];
		random.nextBytes(res);
		return res;
	}

	/**
	 * Convert and array of bytes to a Hex string.
	 * @param bs array of bytes
	 * @return a string representing a hexadecimal number.
	 */
	public static final String toHexString(final byte[] bs) {
		StringBuilder res = new StringBuilder();
		for (byte b : bs) {
			res.append(hex.charAt(((b >>> 4) & 0xF)));
			res.append(hex.charAt((b & 0xF)));
		}
		return res.toString();
	}

	/**
	 * converts a BigInteger to a byte array
	 *
	 * @param b BigInteger
	 * @return byte array
	 */
	public static final byte[] bigIntToBytes(BigInteger b) {
		byte[] preRes = b.toByteArray();
		if (preRes[0] != 1) {
			return preRes;
		}
		byte[] res = new byte[preRes.length - 1];
		for (int i = 0; i < res.length; i++) {
			res[i] = preRes[i + 1];
		}
		return res;
	}

	/**
	 * converts a byte array into a BigInteger object
	 *
	 * @param b byte array
	 * @return BigInteger
	 */
	public static final BigInteger bytesToBigInt(byte[] b) {
		if (b[0] > 1) {
			return new BigInteger(b);
		}
		byte[] res = new byte[b.length + 1];
		res[0] = 1;
		for (int i = 0; i < b.length; i++) {
			res[i + 1] = b[i];
		}
		return new BigInteger(res);
	}
}
