/***************************************************************************
 *                   (C) Copyright 2010-2012 - Marauroa                    *
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

import marauroa.common.net.NetConst;

/**
 * This message indicate the client that the server has reject its login Message
 *
 * @see marauroa.common.net.message.Message
 */

public class MessageS2CLoginMessageNACK extends Message {

	/** The reason of login rejection */
	private String reason;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CLoginMessageNACK() {
		super(MessageType.S2C_LOGIN_MESSAGE_NACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param reason
	 *            the reason to deny the login
	 */
	public MessageS2CLoginMessageNACK(SocketChannel source, String reason) {
		super(MessageType.S2C_LOGIN_MESSAGE_NACK, source);
		this.reason = reason;
	}

	/**
	 * This method returns the reason for the rejection of the login event
	 *
	 * @return reason.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Login NACK) from (" + getAddress() + ") CONTENTS: (" + reason + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		if (out.getProtocolVersion() >= NetConst.FIRST_VERSION_WITH_LONG_BAN_MESSAGE) {
			out.write65536LongString(reason);
		} else {
			String temp = reason;
			if (temp.length() > 250) {
				temp = temp.substring(0, 250);
			}
			out.write255LongString(temp);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		if (getProtocolVersion() >= NetConst.FIRST_VERSION_WITH_LONG_BAN_MESSAGE) {
			reason = in.read65536LongString();
		} else {
			reason = in.read255LongString();
		}

		if (type != MessageType.S2C_LOGIN_MESSAGE_NACK) {
			throw new IOException();
		}
	}
}
