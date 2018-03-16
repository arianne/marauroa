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
import java.util.Map;

import marauroa.common.game.RPObject;
import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * This message indicate the server to create a character.
 * In order to create a character you need to correctly login into the server with an account.
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SCreateCharacter extends Message {

	/** Desired character name */
	private String character;

	/** Desired character configuration. */
	private RPObject template;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SCreateCharacter() {
		super(MessageType.C2S_CREATECHARACTER, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and character name and character
	 * configuration.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param character
	 *            the desired character name
	 * @param template
	 *            a RPObject that contains attributes that will be used on the
	 *            created character.
	 */
	public MessageC2SCreateCharacter(Channel source, String character, RPObject template) {
		super(MessageType.C2S_CREATECHARACTER, source);
		this.character = character;
		this.template = template;
	}

	/**
	 * Returns the character name
	 *
	 * @return the character name
	 */
	public String getCharacter() {
		return character;
	}

	/**
	 * Returns the object template
	 *
	 * @return the object template
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
		return "Message (C2S CreateCharacter) from (" + getAddress() + ") CONTENTS: (" + character
		        + ";" + template + ")";
	}

	@Override
	public void writeObject(OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(character);
		out.write(template);
	}

	@Override
	public void readObject(InputSerializer in) throws IOException {
		super.readObject(in);
		character = in.readString();
		template = (RPObject) in.readObject(new RPObject());

		if (type != MessageType.C2S_CREATECHARACTER) {
			throw new IOException();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readFromMap(Map<String, Object> in) throws IOException {
		super.readFromMap(in);
		character = (String) in.get("charname");
		template = new RPObject();
		template.readFromMap((Map<String, Object>) in.get("template"));

		if (type != MessageType.C2S_CREATECHARACTER) {
			throw new IOException();
		}
	}
}
