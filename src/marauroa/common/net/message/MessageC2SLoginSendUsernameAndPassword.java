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
import java.util.Map;

import marauroa.common.crypto.Hash;

/**
 * This message indicate the server that the client wants to login. This message
 * is only to be used over a secure channel.
 *
 * @see marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword
 */
public class MessageC2SLoginSendUsernameAndPassword extends MessageSendByteArray {

	private String username;

	private byte[] password;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SLoginSendUsernameAndPassword() {
		super(MessageType.C2S_LOGIN_SENDUSERNAMEANDPASSWORD);
	}

	/**
	 * This method returns the username
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * This method returns the encoded password
	 *
	 * @return the password
	 */
	public byte[] getPassword() {
		return password;

	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Login) from (" + getAddress() + ") CONTENTS: (nonce:"
		        + Hash.toHexString(hash) + "\tusername:" + username + "\tpassword:"
		        + Hash.toHexString(Hash.hash(password)) + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(username);
		out.write(password);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		username = in.readString();
		password = Hash.hash(in.readString());
		if (type != MessageType.C2S_LOGIN_SENDUSERNAMEANDPASSWORD) {
			throw new IOException();
		}
	}

	@Override
	public void readFromMap(Map<String, Object> in) throws IOException {
		super.readFromMap(in);
		username = (String) in.get("u");
		password = Hash.hash((String) in.get("p"));

		if (type != MessageType.C2S_LOGIN_SENDUSERNAMEANDPASSWORD) {
			throw new IOException();
		}
	}
}
