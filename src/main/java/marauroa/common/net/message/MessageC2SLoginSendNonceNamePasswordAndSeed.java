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

import marauroa.common.Utility;
import marauroa.common.crypto.Hash;
import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * This message indicate the server that the client wants to login and send the
 * needed info: username, password and seed to the server. The password is
 * protected by Crypt(xor(xor(client nonce, server nonce), password))
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SLoginSendNonceNamePasswordAndSeed extends MessageSendByteArray {

	private String username;
	private byte[] password;
	private byte[] seed;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SLoginSendNonceNamePasswordAndSeed() {
		super(MessageType.C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param nonce
	 *            random number to prevent replay attacks
	 * @param username
	 *            the username of the user that wants to login
	 * @param password
	 *            the plain password of the user that wants to login
	 * @param seed
	 *            the seed
	 */
	public MessageC2SLoginSendNonceNamePasswordAndSeed(Channel source, byte[] nonce,
	        String username, byte[] password, byte[] seed) {
		super(MessageType.C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED, source, nonce);
		this.username = username;
		this.password = Utility.copy(password);
		this.seed = Utility.copy(seed);
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
		return Utility.copy(password);
	}

	/**
	 * This method returns the seed
	 *
	 * @return the seed
	 */
	public byte[] getSeed() {
		return seed;
	}
	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Login) from (" + getAddress() + ") CONTENTS: (nonce:"
		        + Hash.toHexString(hash) + "\tusername:" + username + "\tseed:"
				+ Hash.toHexString(seed) + ")";
	}

	@Override
	public void writeObject(OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(username);
		out.write(password);
		out.write(seed);
	}

	@Override
	public void readObject(InputSerializer in) throws IOException {
		super.readObject(in);
		username = in.readString();
		password = in.readByteArray();
		seed = in.readByteArray();
		if (type != MessageType.C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED) {
			throw new IOException();
		}
	}
}
