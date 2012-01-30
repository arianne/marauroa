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

/**
 * This message indicates the client that it is running an incompatible version
 * of marauroa.
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CInvalidMessage extends Message {

	private String reason;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CInvalidMessage() {
		super(MessageType.S2C_INVALIDMESSAGE, null);
		reason = "";
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and reason of
	 * the failure.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param reason
	 *            Explains why the message is tagged as invalid.
	 */
	public MessageS2CInvalidMessage(SocketChannel source, String reason) {
		super(MessageType.S2C_INVALIDMESSAGE, source);
		this.reason = reason;
	}

	/**
	 * This method returns the reason
	 *
	 * @return the reason
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
		return "Message (S2C Message Invalid) from (" + getAddress() + ") CONTENTS: (reason:"
		        + reason + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(reason);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		reason = in.readString();

		if (type != MessageType.S2C_INVALIDMESSAGE) {
			throw new IOException();
		}
	}
}
