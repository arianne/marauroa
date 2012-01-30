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
 * This message indicate the server that the client wants to login and send the
 * needed info: Hash of a random byte array (N1).
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SLoginSendPromise extends MessageSendByteArray {

	/** Constructor for allowing creation of an empty message */
	public MessageC2SLoginSendPromise() {
		super(MessageType.C2S_LOGIN_SENDPROMISE);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and promise
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param hash
	 *            The hash code of the nonce to use.
	 */
	public MessageC2SLoginSendPromise(SocketChannel source, byte[] hash) {
		super(MessageType.C2S_LOGIN_SENDPROMISE, source, hash);
	}

	@Override
	public String toString() {
		return "Message (C2S Login Send Promise) from (" + getAddress() + ") CONTENTS: (hash:"
		        + Hash.toHexString(hash) + ")";
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		if (type != MessageType.C2S_LOGIN_SENDPROMISE) {
			throw new IOException();
		}
	}

}
