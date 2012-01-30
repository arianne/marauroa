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

import marauroa.common.game.RPObject;

/**
 * This message indicate the client that the server has accepted its create
 * character Message
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CCreateCharacterACK extends Message {

	/** The approved character name */
	private String character;

	/** the approved avatar configuration */
	private RPObject template;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCreateCharacterACK() {
		super(MessageType.S2C_CREATECHARACTER_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and approved character name and
	 * avatar configuration.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param character
	 * 			  the character name
	 * @param template
	 * 			  the avatar configuration.
	 */
	public MessageS2CCreateCharacterACK(SocketChannel source, String character, RPObject template) {
		super(MessageType.S2C_CREATECHARACTER_ACK, source);
		this.character = character;
		this.template = template;
	}

	/**
	 * Returns the name of the character the server finally assigned us.
	 *
	 * @return the name of the character the server finally assigned us.
	 */
	public String getCharacter() {
		return character;
	}

	/**
	 * The modifications of the template that the server did.
	 *
	 * @return The modifications of the template that the server did.
	 */
	public RPObject getTemplate() {
		return template;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C CreateCharacter ACK) from (" + getAddress() + ") CONTENTS: ()";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(character);
		out.write(template);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		character = in.readString();
		template = (RPObject) in.readObject(new RPObject());

		if (type != MessageType.S2C_CREATECHARACTER_ACK) {
			throw new IOException();
		}
	}
}
