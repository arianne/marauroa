/* $Id: MessageS2CCharacterList.java,v 1.2 2007/03/23 20:39:18 arianne_rpg Exp $ */
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
 * The CharacterListMessage is sent from server to client to inform client about
 * the possible election of character to play with.
 */
public class MessageS2CCharacterList extends Message {

	private String[] characters;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCharacterList() {
		super(MessageType.S2C_CHARACTERLIST, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
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
		return characters;
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
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException,
	        java.lang.ClassNotFoundException {
		super.readObject(in);
		characters = in.readStringArray();
		if (type != MessageType.S2C_CHARACTERLIST) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
