/* $Id: MessageP2SCreateCharacter.java,v 1.1 2010/07/22 19:19:21 nhnb Exp $ */
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

import marauroa.common.game.RPObject;

/**
 * This message indicate the server to create a character for the specified user
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageP2SCreateCharacter extends Message {
	/** authentication */
	private String credentials;

	/** name of account */
	private String username;

	/** Desired character name */
	private String character;

	/** Desired character configuration. */
	private RPObject template;

	/** Constructor for allowing creation of an empty message */
	public MessageP2SCreateCharacter() {
		super(MessageType.P2S_CREATECHARACTER, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and character name and character
	 * configuration.
	 *
	 * @param source
	 *            TCP/IP address associated to this message
	 * @param credentials
	 *            authentication
	 * @param username 
	 *            name of account
	 * @param character
	 *            the desired character name
	 * @param template
	 *            a RPObject that contains attributes that will be used on the
	 *            created character.
	 */
	public MessageP2SCreateCharacter(SocketChannel source, String credentials, String username, String character, RPObject template) {
		super(MessageType.P2S_CREATECHARACTER, source);
		this.credentials = credentials;
		this.username = username;
		this.character = character;
		this.template = template;
	}

	/**
	 * Returns the credentials
	 *
	 * @return the credentials
	 */
	protected String getCredentials() {
		return credentials;
	}

	/**
	 * Returns the username
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
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
		return "Message (P2S CreateCharacter) from (" + getAddress() + ") CONTENTS: ("
			+ username + ";" + character + ";" + template + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(credentials);
		out.write(username);
		out.write(character);
		out.write(template);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		credentials = in.readString();
		username = in.readString();
		character = in.readString();
		template = (RPObject) in.readObject(new RPObject());

		if (type != MessageType.C2S_CREATECHARACTER) {
			throw new IOException();
		}
	}
};
