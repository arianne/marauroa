/* $Id: PythonRPWorld.java,v 1.14 2009/12/27 15:41:33 nhnb Exp $ */
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

/**
 * Python implementation of RPWorld. You can't inherit directly RPWorld, so you
 * need to inherit in your Python code the PythonWorld class.
 * 
 * You should set world in server.ini to this class.
 * 
 * @author miguel
 * 
 */
public class PythonRPWorld extends RPWorld {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(PythonRPWorld.class);

	/** Link with Jython engine. */
	private GameScript gameScript;

	/** The python instance of the world */
	private PythonWorld pythonWorld;
	
	private static PythonRPWorld instance;

	public static PythonRPWorld get() {
		if (instance == null) {
			PythonRPWorld myInstance = new PythonRPWorld();
			myInstance.initialize();
			instance = myInstance;
		}
		return instance;
	}	
	
	/**
	 * Constructor
	 */
	public PythonRPWorld() {
		super();

		try {
			gameScript = GameScript.getGameScript();
			gameScript.setRPWorld(this);
			pythonWorld = gameScript.getWorld();
		} catch (Exception e) {
			logger.error("cannot initialize PythonRPWorld()", e);
		}
	}

	/**
	 * Called on server start up
	 */
	@Override
	public void onInit() {
		pythonWorld.onInit();
	}

	/**
	 * Called on server shutdown.
	 */
	@Override
	public void onFinish() {
		pythonWorld.onFinish();
	}
}
