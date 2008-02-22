/* $Id: IFloodCheck.java,v 1.5 2008/02/22 10:28:33 arianne_rpg Exp $ */
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
package marauroa.server.net.flood;

/**
 * Implement this interface to assert if an entry is flooding or not.
 *
 * @author miguel
 *
 */
public interface IFloodCheck {

	/**
	 * Returns true if with the information stored an entry implementation
	 * determines that there is a flood attack.
	 *
	 * @param entry
	 *            Flood measures to help us take a decision
	 * @return true if there is a flood from this entry.
	 */
	public boolean isFlooding(FloodMeasure entry);

	/**
	 * Called by FloodValidator when the connection is found to be flooding.
	 *
	 * @param entry
	 *            the channel that is causing the flood.
	 */
	public void onFlood(FloodMeasure entry);
}
