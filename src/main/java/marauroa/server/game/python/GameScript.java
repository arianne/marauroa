/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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

import marauroa.common.Configuration;
import marauroa.server.game.rp.RPWorld;

import org.python.core.PyInstance;
import org.python.util.PythonInterpreter;

/**
 * This class is a wrapper for calling python in a better way. All the jython
 * specific code is here.
 *
 * You are expected to define the file that contains your python script at
 * server.ini in the field python_script
 *
 * For example:
 *
 * <pre>
 *   python_script=foo.py
 *   python_script_world=MyPythonRPWorld
 *   python_script_ruleprocessor=MyPythonRuleProcessor
 * </pre>
 */
class GameScript {

	/** The Jython interpreter. */
	private PythonInterpreter interpreter;

	/** an instance of the Marauroa configuration system */
	private Configuration conf;

	/**
	 * Constructor (singleton)
	 *
	 * @throws Exception
	 */
	private GameScript() throws Exception {
		conf = Configuration.getConfiguration();
		interpreter = new PythonInterpreter();
		interpreter.execfile(conf.get("python_script"));
	}

	private static GameScript gameScript = null;

	/**
	 * Gets an instance of the GameScript
	 *
	 * @return GameScript
	 * @throws Exception in case of an unexpected error
	 */
	public static GameScript getGameScript() throws Exception {
		if (gameScript == null) {
			gameScript = new GameScript();
		}

		return gameScript;
	}

	/**
	 * Set the RPWorld on the script inside the Python variable
	 * gamescript__world
	 *
	 * @param world
	 *            the world instace.
	 */
	public void setRPWorld(RPWorld world) {
		interpreter.set("gamescript__world", world);
	}

	/**
	 * Get Python RPWorld implementation
	 *
	 * @return Python RPWorld implementation
	 * @throws Exception
	 */
	public PythonWorld getWorld() throws Exception {
		String pythonZoneClass = conf.get("python_script_world");
		PyInstance object = (PyInstance) interpreter.eval(pythonZoneClass + "()");
		return (PythonWorld) object.__tojava__(PythonWorld.class);
	}

	/**
	 * Get the Python IRPRuleProcessor implemenation
	 *
	 * @return Python IRPRuleProcessor implemenation
	 * @throws Exception
	 */
	public PythonRP getGameRules() throws Exception {
		String pythonRPClass = conf.get("python_script_ruleprocessor");
		PyInstance object = (PyInstance) interpreter.eval(pythonRPClass + "(gamescript__world)");
		return (PythonRP) object.__tojava__(PythonRP.class);
	}
}
