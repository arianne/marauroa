/* $Id: MessageS2CCreateAccountNACK.java,v 1.2 2006/08/20 15:40:13 wikipedian Exp $ */
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
import java.net.InetSocketAddress;

/**
 * This message indicate the client that the server has reject its login Message
 * 
 * @see marauroa.common.net.Message
 */

public class MessageS2CCreateAccountNACK extends Message {
	public enum Reasons {
		UNKNOWN_REASON, USERNAME_EXISTS, FIELD_TOO_SHORT,
	}

	static private String[] text = { "Unknown reason",
			"Username already exists.", "Field is too short", };

	private Reasons reason;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCreateAccountNACK() {
		super(MessageType.S2C_CREATEACCOUNT_NACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param resolution
	 *            the reason to deny the login
	 */
	public MessageS2CCreateAccountNACK(InetSocketAddress source,
			Reasons resolution) {
		super(MessageType.S2C_CREATEACCOUNT_NACK, source);
		reason = resolution;
	}

	/**
	 * This method returns the resolution of the login event
	 * 
	 * @return a byte representing the resolution given.
	 */
	public Reasons getResolutionCode() {
		return reason;
	}

	/**
	 * This method returns a String that represent the resolution given to the
	 * login event
	 * 
	 * @return a string representing the resolution.
	 */
	public String getResolution() {
		return text[reason.ordinal()];
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	public String toString() {
		return "Message (S2C Create Account NACK) from ("
				+ source.getAddress().getHostAddress() + ") CONTENTS: ("
				+ getResolution() + ")";
	}

	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
		out.write((byte) reason.ordinal());
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		reason = Reasons.values()[in.readByte()];
		if (type != MessageType.S2C_CREATEACCOUNT_NACK) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
