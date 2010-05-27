/* $Id: MessageC2SLoginSendNonceNameAndPassword.java,v 1.8 2010/05/27 19:13:32 nhnb Exp $ */
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
import marauroa.common.crypto.Hash;

/**
 * This message indicate the server that the client wants to login and send the
 * needed info: username and password to login to server. The password is 
 * protected by Crypt(xor(xor(client nonce, server nonce), password)) 
 * 
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SLoginSendNonceNameAndPassword extends MessageSendByteArray {

	private String username;

	private byte[] password;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SLoginSendNonceNameAndPassword() {
		super(MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD);
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
	 */
	public MessageC2SLoginSendNonceNameAndPassword(SocketChannel source, byte[] nonce,
	        String username, byte[] password) {
		super(MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD, source, nonce);
		this.username = username;
		this.password = Utility.copy(password);
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
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Login) from (" + getAddress() + ") CONTENTS: (nonce:"
		        + Hash.toHexString(hash) + "\tusername:" + username + "\tpassword:"
		        + Hash.toHexString(password) + ")";
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
		password = in.readByteArray();
		if (type != MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD) {
			throw new IOException();
		}
	}
}
