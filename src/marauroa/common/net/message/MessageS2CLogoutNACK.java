/***************************************************************************
 *                   (C) Copyright 2003-2016 - Marauroa                    *
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

import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;

/**
 * This message indicate the client that the server has rejected its Logout
 * Message
 * 
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CLogoutNACK extends Message {

	/** Constructor for allowing creation of an empty message */
	public MessageS2CLogoutNACK() {
		super(MessageType.S2C_LOGOUT_NACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageS2CLogoutNACK(Channel source) {
		super(MessageType.S2C_LOGOUT_NACK, source);
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Logout NACK) from (" + getAddress() + ") CONTENTS: ()";
	}


	@Override
	public void readObject(InputSerializer in) throws IOException {
		super.readObject(in);
		if (type != MessageType.S2C_LOGOUT_NACK) {
			throw new IOException();
		}
	}
}
