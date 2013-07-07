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
import java.math.BigInteger;

import marauroa.common.crypto.RSAPublicKey;
import marauroa.common.net.Channel;

/**
 * This message indicate the server that the client wants to login and send the
 * needed info: server public key
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CLoginSendKey extends Message {

	private RSAPublicKey key;

	private boolean ssl;

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
	 * @param ssl
	 *            activate ssl
	 */
	public MessageS2CLoginSendKey(Channel source, RSAPublicKey key, boolean ssl) {
		super(MessageType.S2C_LOGIN_SENDKEY, source);
		this.key = key;
		this.ssl = ssl;
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
	 * is ssl supported
	 *
	 * @return the ssl
	 */
	public boolean isSslSupported() {
		return ssl;
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
		if (ssl) {
			out.write(1);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		BigInteger n = new BigInteger(in.readByteArray());
		BigInteger e = new BigInteger(in.readByteArray());
		key = new RSAPublicKey(n, e);
		if (in.available() >= 1) {
			ssl = true;
		}


		if (type != MessageType.S2C_LOGIN_SENDKEY) {
			throw new IOException();
		}
	}

	@Override
	public void writeToJson(StringBuilder out) {
		super.writeToJson(out);
		out.append(",\"n\":\"");
		out.append(new BigInteger(key.getN().toByteArray()));
		out.append("\",\"e\":\"");
		out.append(new BigInteger(key.getE().toByteArray()));
		out.append("\"");
	}

}
