/* $Id: StringCheckerTest.java,v 1.2 2008/02/22 10:28:36 arianne_rpg Exp $ */
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
package marauroa.server.game.db;

import org.junit.Assert;
import org.junit.Test;


/**
 * Tests for StringChecker
 *
 * @author hendrik
 */
public class StringCheckerTest {
	
	@Test
	public void testTrimAndEscapeSQLString() {
		Assert.assertEquals(null, StringChecker.trimAndEscapeSQLString(null, 0));

		Assert.assertEquals("", StringChecker.trimAndEscapeSQLString("", 0));
		Assert.assertEquals("", StringChecker.trimAndEscapeSQLString("a", 0));
		Assert.assertEquals("", StringChecker.trimAndEscapeSQLString("aa", 0));

		Assert.assertEquals("",  StringChecker.trimAndEscapeSQLString("", 1));
		Assert.assertEquals("a", StringChecker.trimAndEscapeSQLString("a", 1));
		Assert.assertEquals("a", StringChecker.trimAndEscapeSQLString("aa", 1));

		Assert.assertEquals("7 o''cloc",  StringChecker.trimAndEscapeSQLString("7 o'clock", 8));
		Assert.assertEquals("7 o''clock", StringChecker.trimAndEscapeSQLString("7 o'clock", 9));
		Assert.assertEquals("7 o''clock", StringChecker.trimAndEscapeSQLString("7 o'clock", 10));
	}
}
