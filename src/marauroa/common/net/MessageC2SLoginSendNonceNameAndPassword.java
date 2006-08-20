/* $Id: MessageC2SLoginSendNonceNameAndPassword.java,v 1.2 2006/08/20 15:40:09 wikipedian Exp $ */
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
package marauroa.common.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import marauroa.common.crypto.Hash;

/**
 * This message indicate the server that the client wants to login and send the
 * needed info: username and password to login to server.
 * 
 * @see marauroa.common.net.Message
 */
public class MessageC2SLoginSendNonceNameAndPassword extends
		MessageSendByteArray {
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
	 * @param username
	 *            the username of the user that wants to login
	 * @param password
	 *            the plain password of the user that wants to login
	 */
	public MessageC2SLoginSendNonceNameAndPassword(InetSocketAddress source,
			byte[] nonce, String username, byte[] password) {
		super(MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD, source, nonce);
		this.username = username;
		this.password = password;
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
	public String toString() {
		return "Message (C2S Login) from ("
				+ source.getAddress().getHostAddress() + ") CONTENTS: (nonce:"
				+ Hash.toHexString(hash) + "\tusername:" + username
				+ "\tpassword:" + Hash.toHexString(password) + ")";
	}

	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
		out.write(username);
		out.write(password);
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		username = in.readString();
		password = in.readByteArray();
		if (type != MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD) {
			throw new java.lang.ClassNotFoundException();
		}
	}
}
