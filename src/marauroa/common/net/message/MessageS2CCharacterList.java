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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import marauroa.common.game.RPObject;
import marauroa.common.net.NetConst;

/**
 * The CharacterListMessage is sent from server to client to inform client about
 * the possible election of character to play with.
 */
public class MessageS2CCharacterList extends Message {

	/** The list of available characters to choose. */
	private Map<String, RPObject> characters;

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
		this.characters = new LinkedHashMap<String, RPObject>();
		for (String character : characters) {
			this.characters.put(character, new RPObject());
		}
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
	public MessageS2CCharacterList(SocketChannel source, Map<String, RPObject> characters) {
		super(MessageType.S2C_CHARACTERLIST, source);
		this.characters = new LinkedHashMap<String, RPObject>(characters);
	}

	
	/**
	 * This method returns the list of characters that the player owns
	 *
	 * @return the list of characters that the player owns
	 */
	public String[] getCharacters() {
		String[] res = new String[characters.size()];
		characters.keySet().toArray(res);
		return res;
	}

	/**
	 * This method returns the list of characters that the player owns
	 *
	 * @return the list of characters that the player owns
	 */
	public HashMap<String, RPObject> getCharacterDetails() {
		return new LinkedHashMap<String, RPObject>(characters);
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Character List) from (" + getAddress() + ") CONTENTS: ("
		        + characters.keySet() + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(getCharacters());
		if (super.protocolVersion >= NetConst.FIRST_VERSION_WITH_DETAILS_IN_CHARACTER_LIST) {
			String[] character = getCharacters();
			RPObject[] objects = new RPObject[characters.size()];
			for (int i = 0; i < characters.size(); i++) {
				objects[i] = this.characters.get(character[i]);
			}
			out.write(objects);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		String[] characters = in.readStringArray();
		this.characters = new LinkedHashMap<String, RPObject>();
	
		// read the map or list, depending on protocol version
		if (super.protocolVersion >= NetConst.FIRST_VERSION_WITH_DETAILS_IN_CHARACTER_LIST) {
			Object[] objects = in.readObjectArray(RPObject.class);
			for (int i = 0; i < characters.length; i++) {
				this.characters.put(characters[i], (RPObject) objects[i]);
			}
		} else {
			for (String character : characters) {
				this.characters.put(character, new RPObject());
			}
		}

		if (type != MessageType.S2C_CHARACTERLIST) {
			throw new IOException();
		}
	}
}
