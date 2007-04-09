/* $Id: MessageS2CLoginSendKey.java,v 1.5 2007/04/09 14:47:09 arianne_rpg Exp $ */
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
import java.math.BigInteger;
import java.nio.channels.SocketChannel;

import marauroa.common.crypto.RSAPublicKey;

/**
 * This message indicate the server that the client wants to login and send the
 * needed info: server public key
 * 
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CLoginSendKey extends Message {

	private RSAPublicKey key;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CLoginSendKey() {
		super(MessageType.S2C_LOGIN_SENDKEY, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the
	 * public key server is using.
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param key
	 *            the server public key.
	 */
	public MessageS2CLoginSendKey(SocketChannel source, RSAPublicKey key) {
		super(MessageType.S2C_LOGIN_SENDKEY, source);
		this.key = key;
	}

	/**
	 * This method returns the username
	 * 
	 * @return the username
	 */
	public RSAPublicKey getKey() {
		return key;
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Login Send key) from (" + getAddress() + ") CONTENTS: (n:"
		        + key.getN() + "\te:" + key.getE() + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(key.getN().toByteArray());
		out.write(key.getE().toByteArray());
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		BigInteger n = new BigInteger(in.readByteArray());
		BigInteger e = new BigInteger(in.readByteArray());
		key = new RSAPublicKey(n, e);
		if (type != MessageType.S2C_LOGIN_SENDKEY) {
			throw new IOException();
		}
	}
}
