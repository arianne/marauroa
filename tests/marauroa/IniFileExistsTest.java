/* $Id: IniFileExistsTest.java,v 1.3 2008/02/22 10:28:34 arianne_rpg Exp $ */
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
package marauroa;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class IniFileExistsTest {

	@Test
	public void checkIniExists() {
		File ini = new File("server.ini");
		assertTrue("There must be a file named server.ini in the main folder of Marauroa.",ini.exists());
	}

}
