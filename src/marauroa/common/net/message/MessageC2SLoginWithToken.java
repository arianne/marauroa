/***************************************************************************
 *                   (C) Copyright 2003-2020 - Marauroa                    *
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
public class MessageC2SLoginWithToken extends MessageSendByteArray {

	private String username;
	private String tokenType;
	private byte[] token;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SLoginWithToken() {
		super(MessageType.C2S_LOGIN_WITH_TOKEN);
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
	 * @param tokenType
	 *            type of token
	 * @param token
	 *            authentication token
	 */
	public MessageC2SLoginWithToken(Channel source, byte[] nonce,
	        String username, String tokenType, byte[] token) {
		super(MessageType.C2S_LOGIN_WITH_TOKEN, source, nonce);
		this.username = username;
		this.tokenType = tokenType;
		this.token = Utility.copy(token);
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
	 * This method returns the tokenType
	 *
	 * @return the tokenType
	 */
	public String getTokenType() {
		return tokenType;
	}

	/**
	 * This method returns the encoded token
	 *
	 * @return the token
	 */
	public byte[] getToken() {
		return Utility.copy(token);
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Login With Token) from (" + getAddress() + ") CONTENTS: (nonce:"
		        + Hash.toHexString(hash) + "\tusername:" + username + "\ttokenType:"
				+ tokenType + "\ttoken:"+ Hash.toHexString(token) + ")";
	}

	@Override
	public void writeObject(OutputSerializer out) throws IOException {
		super.writeObject(out);
		if (username != null) {
			out.write(username);
		} else {
			out.write("");
		}
		out.write(tokenType);
		out.write(token);
	}

	@Override
	public void readObject(InputSerializer in) throws IOException {
		super.readObject(in);
		username = in.readString();
		tokenType = in.readString();
		token = in.readByteArray();
		if (type != MessageType.C2S_LOGIN_WITH_TOKEN) {
			throw new IOException();
		}
	}

}
