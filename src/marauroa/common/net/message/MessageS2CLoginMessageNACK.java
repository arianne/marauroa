/* $Id: MessageS2CLoginMessageNACK.java,v 1.2 2010/02/08 21:39:57 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2010 - Marauroa                      *
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
		if (this.reason.length() > 250) {
			this.reason = this.reason.substring(0, 250);
		}
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
		out.write255LongString(reason);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		reason = in.read255LongString();

		if (type != MessageType.S2C_LOGIN_MESSAGE_NACK) {
			throw new IOException();
		}
	}
};
