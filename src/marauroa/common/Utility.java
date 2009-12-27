/* $Id: Utility.java,v 1.13 2009/12/27 19:30:09 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
/*
 * Utility.java
 *
 * Created on 11. September 2005, 10:16
 *
 */

package marauroa.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Some generic utility methods.
 * 
 * @author Matthias Totz
 */
public class Utility {

	/** no instance allowed */
	private Utility() {
		// static class
	}

	/**
	 * adds some leading '0' to the sting until the length <i>maxDigits</i> is
	 * reached
	 * 
	 * @param number
	 *            the number to convert
	 * @param maxDigits
	 *            the amount of digits expected
	 * @return the expected number
	 */
	public static String addLeadingZeros(String number, int maxDigits) {
		StringBuilder result = new StringBuilder(number);

		while (result.length() < maxDigits) {
			result.insert(0, "0");
		}

		return result.toString();
	}


	/**
	 * creates a nice hex-dump of the byte array
	 * 
	 * @param byteArray
	 *            the byte array to convert.
	 * @return a hex-dump of the array.
	 */
	public static String dumpByteArray(byte[] byteArray) {
		return dumpInputStream(new ByteArrayInputStream(byteArray));
	}

	/**
	 * creates a nice hex-dump of the byte array
	 * 
	 * @param byteStream
	 *            the byte array to convert.
	 * @return a hex-dump of the array.
	 */
	public static String dumpInputStream(InputStream byteStream) {
		StringBuilder result = new StringBuilder();
		try {
			int index = 0;
			StringBuilder chars = new StringBuilder();

			int theByte = byteStream.read();
			result.append(addLeadingZeros(Integer.toHexString(index), 8)).append(' ');
			index++;

			while (theByte != -1) {
				result.append(addLeadingZeros(Integer.toHexString(theByte), 2)).append(' ');

				// show chars < 32 and > 127 as '.'
				if ((theByte > 31) && (theByte < 128)) {
					chars.append((char) (theByte));
				} else {
					chars.append('.');
				}

				if ((index > 0) && (index % 16 == 0)) {
					result.append(chars).append('\n').append(
					        addLeadingZeros(Integer.toHexString(index), 8)).append(' ');

					chars = new StringBuilder();
				}
				index++;
				theByte = byteStream.read();
			}
			return result.toString();
		} catch (Exception e) {
			return result.toString() + "\nException: " + e.getMessage();
		}
	}

	/**
	 * copies an array
	 *
	 * @param array array to copy
	 * @return copy of array
	 */
	public static byte[] copy(byte[] array) {
		byte[] temp = new byte[array.length];
		System.arraycopy(array, 0, temp, 0, array.length);
		return temp;
	}

	/**
	 * copies an array
	 *
	 * @param array array to copy
	 * @return copy of array
	 */
	public static float[] copy(float[] array) {
		float[] temp = new float[array.length];
		System.arraycopy(array, 0, temp, 0, array.length);
		return temp;
	}

	/**
	 * copies an array
	 *
	 * @param array array to copy
	 * @return copy of array
	 */
	public static String[] copy(String[] array) {
		String[] temp = new String[array.length];
		System.arraycopy(array, 0, temp, 0, array.length);
		return temp;
	}
}
