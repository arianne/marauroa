/* $Id: StringChecker.java,v 1.2 2007/02/03 17:33:44 arianne_rpg Exp $ */
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
package marauroa.server.game.db.nio;

public class StringChecker {
	/** 
	 * This method returns if a string is valid because it lacks of any kind of control
	 * or escape character. 
	 * 
	 * @param string The string to check
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
	 * Escapes ' and \ in a string so that the result can be passed into an
	 * SQL command. The parameter has be quoted using ' in the sql. Most
	 * database engines accept single quotes around numbers as well.
     * <p>Please note that special characters for LIKE and other matching
	 * commands are not quotes. The result of this method is suiteable for
	 * INSERT, UPDATE and an "=" operator in the WHERE part.
	 * 
	 * @param param string to quote
	 * @return quoted string
	 */
	public static String escapeSQLString(String param) {
		if (param == null) {
			return param;
		}
		return param.replace("'", "''").replace("\\", "\\\\");
	}	

}
