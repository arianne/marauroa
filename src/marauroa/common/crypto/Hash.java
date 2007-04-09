/* $Id: Hash.java,v 1.6 2007/04/09 14:39:50 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * This class is used to create Hashes of byte arrays.
 *
 * @author quisar
 */
public class Hash {

	private static String hex = "0123456789ABCDEF";

	static private MessageDigest md;

	static private SecureRandom random;

	static {
		try {
			md = MessageDigest.getInstance("MD5");
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	synchronized public static final byte[] hash(final String value) {
		return hash(value.getBytes());
	}

	synchronized public static final int hashLength() {
		return md.getDigestLength();
	}

	synchronized public static final byte[] hash(final byte[] value) {
		md.reset();
		md.update(value);
		return md.digest();
	}

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

	synchronized public static final byte[] random(int nbBytes) {
		byte[] res = new byte[nbBytes];
		random.nextBytes(res);
		return res;
	}

	public static final String toHexString(final byte[] bs) {
		String res = "";
		for (byte b : bs) {
			res += hex.charAt(((b >>> 4) & 0xF));
			res += hex.charAt((b & 0xF));
		}
		return res;
	}

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
