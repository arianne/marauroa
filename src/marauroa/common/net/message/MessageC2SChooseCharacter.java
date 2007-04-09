/* $Id: MessageC2SChooseCharacter.java,v 1.4 2007/04/09 14:47:08 arianne_rpg Exp $ */
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
	public MessageC2SChooseCharacter(SocketChannel source, String character) {
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
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(character);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		character = in.readString();

		if (type != MessageType.C2S_CHOOSECHARACTER) {
			throw new IOException();
		}
	}
};
