/* $Id: RPClassTestHelper.java,v 1.2 2010/01/31 20:45:11 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2007-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.game;

import marauroa.common.game.Definition.Type;

/**
 * helper class for test using RPClass
 *
 * @author hendrik
 */
public class RPClassTestHelper {

	public static void generateRPClasses() {
		if (!RPClass.hasRPClass("chat")) {
			final RPClass chat = new RPClass("chat");
			chat.addAttribute("text", Type.STRING);
		}
		if (!RPClass.hasRPClass("test")) {
			final RPClass chat = new RPClass("test");
			chat.addAttribute("value", Type.STRING);
		}
		if (!RPClass.hasRPClass("tax")) {
			final RPClass chat = new RPClass("tax");
			chat.addAttribute("bill", Type.STRING);
		}
	}
}
