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
package marauroa.common.net.message;

import java.io.IOException;
import java.util.Map;

import marauroa.common.net.Channel;

/**
 * The Logout Message is sent from client to server to indicate that it wants to
 * finish the session.
 */
public class MessageC2SLogout extends Message {

	/** Constructor for allowing creation of an empty message */
	public MessageC2SLogout() {
		super(MessageType.C2S_LOGOUT, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageC2SLogout(Channel source) {
		super(MessageType.C2S_LOGOUT, source);
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Logout) from (" + getAddress() + ") CONTENTS: ()";
	}


	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		if (type != MessageType.C2S_LOGOUT) {
			throw new IOException();
		}
	}

	@Override
	public void readFromMap(Map<String, Object> in) throws IOException {
		super.readFromMap(in);

		if (type != MessageType.C2S_LOGOUT) {
			throw new IOException();
		}
	}
}
