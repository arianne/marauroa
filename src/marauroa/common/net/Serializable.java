/* $Id: Serializable.java,v 1.4 2007/04/09 14:39:56 arianne_rpg Exp $ */
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
package marauroa.common.net;

/**
 * Interface of all the object that wants to be able to be converted into a
 * stream of bytes.
 */
public interface Serializable {

	/** Method to convert the object into a stream */
	void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException;

	/** Method to build the object from a stream of bytes */
	void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException;
}
