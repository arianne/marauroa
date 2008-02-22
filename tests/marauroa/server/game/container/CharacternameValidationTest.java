/* $Id: CharacternameValidationTest.java,v 1.2 2008/02/22 10:28:35 arianne_rpg Exp $ */
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
package marauroa.server.game.container;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the account validation
 *
 * @author hendrik
 */
public class CharacternameValidationTest {
	private static Logger logger = Logger.getLogger(CharacternameValidationTest.class);

	public boolean checkImpersonation(String username) {
		String name = username;
		name = name.replaceAll("[ _.,;.\\-\\\\ \"�$%&/()='<>|*+~#]", " ");
		if (name.startsWith(" ") || name.endsWith(" ") || (name.indexOf("gm ") > -1) || (name.indexOf(" gm") > -1)
		        || name.startsWith("gm") || name.endsWith("gm") || (name.indexOf("  ") > -1)) {
			logger.warn("Possible impersonation: " + username);
			return false;
		}
		
		return true;
	}
	
	@Test
	public void checkImpersonation() {
		Assert.assertTrue(checkImpersonation("hendrik"));
		Assert.assertTrue(checkImpersonation("hendrik1"));
		Assert.assertTrue(checkImpersonation("Hendrik"));
		Assert.assertTrue(checkImpersonation("hEndrik1"));
		Assert.assertFalse(checkImpersonation("hendrik_"));
		Assert.assertFalse(checkImpersonation("_hendrik"));
		Assert.assertFalse(checkImpersonation("hendrikgm"));
	}
}
