/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
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
 * This exception is thrown when a invalid version message is received.
 */
public class InvalidVersionException extends Exception {

	private static final long serialVersionUID = 7892075553859015832L;

	private int version;

	/**
	 * Constructor
	 *
	 * @param version
	 *            the version that caused the exception
	 */
	public InvalidVersionException(int version) {
		super();
		this.version = version;
	}

	/**
	 * Return the version number
	 *
	 * @return the version number
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * gets the wrong version
	 *
	 * @return protocolVersion
	 */
	public int getProtocolVersion() {
		return version;
	}
}
