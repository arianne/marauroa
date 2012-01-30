/***************************************************************************
 *                   (C) Copyright 2003-2007 - Marauroa                    *
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

import marauroa.common.crypto.Hash;

/**
 * This message indicate the client the choosen server NONCE value by sending it a
 * hash of it.
 *
 * @see marauroa.common.net.message.Message
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
	public MessageS2CLoginSendNonce(SocketChannel source, byte[] hash) {
		super(MessageType.S2C_LOGIN_SENDNONCE, source, hash);
	}

	@Override
	public String toString() {
		return "Message (S2C Login Send Nonce) from (" + getAddress() + ") CONTENTS: (nonce:"
		        + Hash.toHexString(hash) + ")";
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		if (type != MessageType.S2C_LOGIN_SENDNONCE) {
			throw new IOException();
		}
	}

}
