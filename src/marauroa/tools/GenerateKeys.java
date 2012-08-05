/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import marauroa.common.crypto.RSAKey;

/**
 * generates a key pair for server.ini
 *
 * @author hendrik
 */
public class GenerateKeys {
	private static BufferedReader in = null;


	/**
	 * reads a String from the input. When no String is chosen the defaultValue
	 * is used.
	 *
	 * @param defaultValue
	 *            if no value is written.
	 * @return the string read or default if none was read.
	 */
	public static String getStringWithDefault(final String defaultValue) {
		String ret = "";
		try {
			ret = in.readLine();
		} catch (final IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		if (ret != null) {
			if ((ret.length() == 0) && (defaultValue != null)) {
				ret = defaultValue;
			}
		}
		return ret;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		in = new BufferedReader(new InputStreamReader(System.in));

		/* The size of the RSA Key in bits, usually 512 */
		System.out.print("# How long should the key be (minimum 512)? [1024]: ");
		final String keySize = getStringWithDefault("1024");
		System.out.println();
		System.out.println("# Using key of " + keySize + " bits.");
		System.out.println("# Please wait while the key is generated (this may take a couple of minutes).");
		System.out.println("# Moving your mouse around to generate randomness may speed up the process.");
		RSAKey rsakey = RSAKey.generateKey(Integer.valueOf(keySize));
		System.out.println();
		System.out.println("# Encryption key");
		rsakey.print(System.out);
		in.close();
	}

}
