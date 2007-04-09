/* $Id: PythonWorld.java,v 1.7 2007/04/09 14:47:12 arianne_rpg Exp $ */
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

/**
 * This class is a abstract class for you to inherit in in Python. You can
 * implement onInit and onFinish methods to define the behaviour to do when
 * server init and server shutdown.
 * 
 * @author miguel
 * 
 */
public class PythonWorld {

	/**
	 * Constructor
	 * 
	 */
	public PythonWorld() {
	}

	/**
	 * Called on server start up
	 * 
	 */
	public void onInit() {
	}

	/**
	 * Called on server shutdown
	 * 
	 */
	public void onFinish() {
	}
}
