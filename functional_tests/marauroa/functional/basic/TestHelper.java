/* $Id: TestHelper.java,v 1.1 2008/03/25 17:55:25 arianne_rpg Exp $ */
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
package marauroa.functional.basic;

public class TestHelper {

	public static void assertEquals(Object expected, Object val) {
		if (!expected.equals(val)) {
			throw new FailedException("expected " + expected + " but got " + val);
		}
	}

	public static void fail() {
		throw new FailedException("Forced fail.");
	}

	public static void assertNotNull(Object result) {
		if (result == null) {
			throw new FailedException("Unexpected null value");
		}
	}

}
