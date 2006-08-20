/* $Id: MessageS2CLoginSendNonce.java,v 1.3 2006/08/20 15:40:12 wikipedian Exp $ */
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
public class MessageS2CLoginSendNonce extends MessageSendByteArray {
	/** Constructor for allowing creation of an empty message */
	public MessageS2CLoginSendNonce() {
		super(MessageType.S2C_LOGIN_SENDNONCE);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param hash
	 *            The nonce to send to the client.
	 */
	public MessageS2CLoginSendNonce(InetSocketAddress source, byte[] hash) {
		super(MessageType.S2C_LOGIN_SENDNONCE, source, hash);
	}

	public String toString() {
		return "Message (S2C Login Send Nonce) from ("
				+ source.getAddress().getHostAddress() + ") CONTENTS: (nonce:"
				+ Hash.toHexString(hash) + ")";
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		if (type != MessageType.S2C_LOGIN_SENDNONCE) {
			throw new java.lang.ClassNotFoundException();
		}
	}

}
