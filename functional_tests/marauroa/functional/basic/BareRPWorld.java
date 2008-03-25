/* $Id: BareRPWorld.java,v 1.1 2008/03/25 17:55:25 arianne_rpg Exp $ */
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

import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.server.game.rp.MarauroaRPZone;
import marauroa.server.game.rp.RPWorld;

public class BareRPWorld extends RPWorld {

	private static BareRPWorld world;

	private BareRPWorld() {
		super();

		populate();
	}

	protected void populate() {
		IRPZone zone = new MarauroaRPZone("test");
		addRPZone(zone);

		RPObject hidden = new RPObject();
		zone.assignRPObjectID(hidden);
		hidden.put("hidden", "You don't see this object");
		hidden.hide();
		zone.add(hidden);
	}

	/**
	 * This method MUST be implemented in other for marauroa to be able to load
	 * this World implementation. There is no way of enforcing static methods on
	 * a Interface, so just keep this in mind when writting your own game.
	 * 
	 * @return an unique instance of world.
	 */
	public static RPWorld get() {
		if (world == null) {
			world = new BareRPWorld();
		}

		return world;
	}
}
