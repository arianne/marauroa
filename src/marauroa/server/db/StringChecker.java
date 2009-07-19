/* $Id: StringChecker.java,v 1.3 2009/07/19 09:40:28 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db;

/**
 * Helper class to validate strings and escape SQL strings.
 *
 * @author miguel
 *
 */
public class StringChecker {

	/**
	 * This method returns true if a string is valid because it lacks of any kind of
	 * control or escape character.
	 *
	 * @param string
	 *            The string to check
	 * @return true if the string is valid for storing it at database or as XML.
	 */
	public static boolean validString(String string) {
		if (string.indexOf('\\') != -1) {
			return false;
		}
		if (string.indexOf('\'') != -1) {
			return false;
		}
		if (string.indexOf('"') != -1) {
			return false;
		}
		if (string.indexOf('%') != -1) {
			return false;
		}
		if (string.indexOf(';') != -1) {
			return false;
		}
		if (string.indexOf(':') != -1) {
			return false;
		}
		if (string.indexOf('#') != -1) {
			return false;
		}
		if (string.indexOf('<') != -1) {
			return false;
		}
		if (string.indexOf('>') != -1) {
			return false;
		}
		return true;
	}

	/**
	 * Escapes ' and \ in a string so that the result can be passed into an SQL
	 * command. The parameter has be quoted using ' in the sql. Most database
	 * engines accept single quotes around numbers as well.
	 * <p>
	 * Please note that special characters for LIKE and other matching commands
	 * are not quotes. The result of this method is suitable for INSERT, UPDATE
	 * and an "=" operator in the WHERE part.
	 *
	 * @param param
	 *            string to quote
	 * @return quoted string
	 */
	public static String escapeSQLString(String param) {
		if (param == null) {
			return param;
		}
		return param.replace("'", "''").replace("\\", "\\\\");
	}

	/**
	 * Trims the string to the specified size without error in case it is already shorter. 
	 * Escapes ' and \ in a string so that the result can be passed into an SQL
	 * command. The parameter has be quoted using ' in the sql. Most database
	 * engines accept single quotes around numbers as well.
	 * <p>
	 * Please note that special characters for LIKE and other matching commands
	 * are not quotes. The result of this method is suitable for INSERT, UPDATE
	 * and an "=" operator in the WHERE part.
	 *
	 * @param param
	 *            string to quote
	 * @param size maximal length of this string before encoding
	 * @return quoted string
	 */
	public static String trimAndEscapeSQLString(String param, int size) {
		if (param == null) {
			return param;
		}
		String res = param;
		if (res.length() > size) {
			res = res.substring(0, size);
		}
		return escapeSQLString(res);
	}
}
