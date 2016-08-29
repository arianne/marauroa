/***************************************************************************
 *                   (C) Copyright 2003-2016 - Marauroa                    *
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
import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * This message is a generic message that send a byte array.
 * 
 * @see marauroa.common.net.message.Message
 */
public class MessageSendByteArray extends Message {

	protected byte[] hash;

	/** 
	 * Constructor for allowing creation of an empty message
	 *
	 * @param type tpye of message
	 */
	public MessageSendByteArray(MessageType type) {
		super(type, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the byte
	 * array to send.
	 * 
	 * @param type tpye of message
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param hash
	 *            The byte array you want to send.
	 */
	public MessageSendByteArray(MessageType type, Channel source, byte[] hash) {
		super(type, source);
		this.hash = Utility.copy(hash);
	}

	/**
	 * This method returns the byte array.
	 * 
	 * @return the byte array
	 */
	public byte[] getHash() {
		return Utility.copy(hash);

	}

	@Override
	public void writeObject(OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(hash);
	}

	@Override
	public void readObject(InputSerializer in) throws IOException {
		super.readObject(in);
		hash = in.readByteArray();
	}
}
