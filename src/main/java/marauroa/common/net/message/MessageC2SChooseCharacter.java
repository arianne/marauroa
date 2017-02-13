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

import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * This message indicate the server what of the available characters is chosen
 * for the session to play.
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SChooseCharacter extends Message {

	/** The choosen player */
	private String character;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SChooseCharacter() {
		super(MessageType.C2S_CHOOSECHARACTER, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param character
	 *            The name of the choosen character that <b>MUST</b> be one of
	 *            the returned by the marauroa.common.net.MessageS2CCharacters
	 * @see marauroa.common.net.message.MessageS2CCharacterList
	 */
	public MessageC2SChooseCharacter(Channel source, String character) {
		super(MessageType.C2S_CHOOSECHARACTER, source);
		this.character = character;
	}

	/**
	 * This methods returns the name of the chosen character
	 *
	 * @return the character name
	 */
	public String getCharacter() {
		return character;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S ChooseCharacter) from (" + getAddress() + ") CONTENTS: (" + character
		        + ")";
	}

	@Override
	public void writeObject(OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(character);
	}

	@Override
	public void readObject(InputSerializer in) throws IOException {
		super.readObject(in);
		character = in.readString();

		if (type != MessageType.C2S_CHOOSECHARACTER) {
			throw new IOException();
		}
	}

	@Override
	public void readFromMap(Map<String, Object> in) throws IOException {
		super.readFromMap(in);
		character = (String) in.get("character");

		if (type != MessageType.C2S_CHOOSECHARACTER) {
			throw new IOException();
		}
	}
}
