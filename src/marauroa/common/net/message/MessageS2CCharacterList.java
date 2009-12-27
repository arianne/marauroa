/* $Id: MessageS2CCharacterList.java,v 1.5 2009/12/27 19:30:37 nhnb Exp $ */
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

import marauroa.common.Utility;

/**
 * The CharacterListMessage is sent from server to client to inform client about
 * the possible election of character to play with.
 */
public class MessageS2CCharacterList extends Message {

	/** The list of available characters to choose. */
	private String[] characters;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCharacterList() {
		super(MessageType.S2C_CHARACTERLIST, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and a list with the
	 * characters available to be played.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param characters
	 *            the list of characters of the player
	 */
	public MessageS2CCharacterList(SocketChannel source, String[] characters) {
		super(MessageType.S2C_CHARACTERLIST, source);
		this.characters = characters;
	}

	/**
	 * This method returns the list of characters that the player owns
	 *
	 * @return the list of characters that the player owns
	 */
	public String[] getCharacters() {
		return Utility.copy(characters);
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(" ");

		for (int i = 0; i < characters.length; ++i) {
			text.append(characters[i] + ",");
		}
		return "Message (S2C Character List) from (" + getAddress() + ") CONTENTS: ("
		        + text.substring(0, text.length() - 1) + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(characters);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		characters = in.readStringArray();

		if (type != MessageType.S2C_CHARACTERLIST) {
			throw new IOException();
		}
	}
};
