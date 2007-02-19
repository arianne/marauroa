/* $Id: PythonRPWorld.java,v 1.9 2007/02/19 18:37:25 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.python;

import marauroa.common.Log4J;
import marauroa.server.game.rp.RPWorld;

@Deprecated
public class PythonRPWorld extends RPWorld {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(PythonRPWorld.class);

	private GameScript gameScript;

	private PythonWorld pythonWorld;

	public PythonRPWorld() throws Exception {
		super();

		try {
			gameScript = GameScript.getGameScript();
			gameScript.setRPWorld(this);
			pythonWorld = gameScript.getWorld();
		} catch (Exception e) {
			logger.error("cannot initialize PythonRPWorld()", e);
		}
	}

	@Override
	public void onInit() {
		pythonWorld.onInit();
	}

	@Override
	public void onFinish() {
		pythonWorld.onFinish();
	}

}
