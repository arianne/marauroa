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
import java.util.Locale;

import marauroa.common.crypto.Hash;
import marauroa.common.net.Channel;
import marauroa.common.net.OutputSerializer;

/**
 * This message indicate the server that the client wants to login and send the
 * needed info: Hash of a random byte array (N1).
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SLoginSendPromise extends MessageSendByteArray {

	/** client language */
	private String language = Locale.ENGLISH.getLanguage();

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
	 * @param language 
	 *            Client language 
	 */
	public MessageC2SLoginSendPromise(Channel source, byte[] hash, String language) {
		super(MessageType.C2S_LOGIN_SENDPROMISE, source, hash);
		this.language = language;
	}

	/**
	 * gets the language
	 *
	 * @return language
	 */
	public String getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return "Message (C2S Login Send Promise) from (" + getAddress() + ") CONTENTS: (hash: "
		        + Hash.toHexString(hash) + " language: " + language + ")";
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		if (in.available() > 0) {
			language = in.read255LongString();
		}

		if (type != MessageType.C2S_LOGIN_SENDPROMISE) {
			throw new IOException();
		}
	}

	@Override
	public void writeObject(OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write255LongString(language);
	}
}
