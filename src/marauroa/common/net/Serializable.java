/* $Id: Serializable.java,v 1.5 2007/11/04 20:47:28 nhnb Exp $ */
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

import java.io.IOException;

/**
 * Interface of all the object that wants to be able to be converted into a
 * stream of bytes.
 */
public interface Serializable {

	/**
	 * Method to convert the object into a stream
	 *
	 * @param out OutputSerializer to write the object to
	 * @throws IOException in case of an IO-error
	 */
	void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException;

	/** 
	 * Method to build the object from a stream of bytes
	 *
	 * @param in InputSerializer to read from
	 * @throws IOException in case of an IO-error
	 */
	void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException;
}
