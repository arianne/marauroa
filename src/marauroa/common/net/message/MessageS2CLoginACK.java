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
package marauroa.common.net.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
 * This message indicate the client that the server has accepted its login
 * Message
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CLoginACK extends Message {

	private List<String> previousLogins;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CLoginACK() {
		super(MessageType.S2C_LOGIN_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param events
	 * 			  The list of previous logins.
	 */
	public MessageS2CLoginACK(SocketChannel source, List<String> events) {
		super(MessageType.S2C_LOGIN_ACK, source);
		previousLogins = events;
	}

	/**
	 * Return a list of previous login attemps.
	 * @return a list of previous login attemps.
	 */
	public List<String> getPreviousLogins() {
		return previousLogins;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Login ACK) from (" + getAddress() + ") CONTENTS: (previousLogins: " + previousLogins + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);

		out.write((byte) previousLogins.size());
		for (String event : previousLogins) {
			out.write255LongString(event);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		int amount = in.readByte();
		previousLogins = new LinkedList<String>();
		for (int i = 0; i < amount; i++) {
			previousLogins.add(in.read255LongString());
		}

		if (type != MessageType.S2C_LOGIN_ACK) {
			throw new IOException();
		}
	}
}
