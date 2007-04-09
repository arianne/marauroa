/* $Id: MessageS2CCreateCharacterNACK.java,v 1.5 2007/04/09 14:47:09 arianne_rpg Exp $ */
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

package marauroa.common.net.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * This message indicate the client that the server has reject its create character Message
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CCreateCharacterNACK extends Message {

	public enum Reasons {
		UNKNOWN_REASON, CHARACTER_EXISTS, FIELD_TOO_SHORT, TEMPLATE_INVALID
	}

	static private String[] text = { "Unknown reason", "Character already exists.",
	        "Field is too short", "Template is invalid" };

	/** The reason to reject character creation */
	private Reasons reason;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCreateCharacterNACK() {
		super(MessageType.S2C_CREATECHARACTER_NACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the reason to deny
	 * character creation.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param resolution
	 *            the reason to deny the login
	 */
	public MessageS2CCreateCharacterNACK(SocketChannel source, Reasons resolution) {
		super(MessageType.S2C_CREATECHARACTER_NACK, source);
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
	@Override
	public String toString() {
		return "Message (S2C Create Character NACK) from (" + getAddress() + ") CONTENTS: ("
		        + getResolution() + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write((byte) reason.ordinal());
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		reason = Reasons.values()[in.readByte()];

		if (type != MessageType.S2C_CREATECHARACTER_NACK) {
			throw new IOException();
		}
	}
};
