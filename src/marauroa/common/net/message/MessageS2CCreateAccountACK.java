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
import java.nio.channels.SocketChannel;

/**
 * This message indicate the client that the server has accepted its create
 * account Message
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CCreateAccountACK extends Message {

	private String username;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCreateAccountACK() {
		super(MessageType.S2C_CREATEACCOUNT_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the confirmed username
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param username
	 * 			  The confirmed username from server.
	 */
	public MessageS2CCreateAccountACK(SocketChannel source, String username) {
		super(MessageType.S2C_CREATEACCOUNT_ACK, source);
		this.username = username;
	}

	/**
	 * Returns the username created by the server.
	 *
	 * @return the username created by the server.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C CreateAccount ACK) from (" + getAddress() + ") CONTENTS: ()";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(username);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		username = in.readString();

		if (type != MessageType.S2C_CREATEACCOUNT_ACK) {
			throw new IOException();
		}
	}
}
