/* $Id: MessageC2SLoginSendPromise.java,v 1.5 2007/01/18 12:37:45 arianne_rpg Exp $ */
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
import java.nio.channels.SocketChannel;

import marauroa.common.crypto.Hash;

/**
 * This message indicate the server that the client wants to login and send the
 * needed info: username and password to login to server.
 * 
 * @see marauroa.common.net.Message
 */
public class MessageC2SLoginSendPromise extends MessageSendByteArray {
	/** Constructor for allowing creation of an empty message */
	public MessageC2SLoginSendPromise() {
		super(MessageType.C2S_LOGIN_SENDPROMISE);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
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
		return "Message (C2S Login Send Promise) from ("
				+ getAddress() + ") CONTENTS: (hash:"
				+ Hash.toHexString(hash) + ")";
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		if (type != MessageType.C2S_LOGIN_SENDPROMISE) {
			throw new java.lang.ClassNotFoundException();
		}
	}

}
