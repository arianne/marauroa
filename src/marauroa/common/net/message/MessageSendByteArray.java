/* $Id: MessageSendByteArray.java,v 1.6 2010/05/27 19:13:32 nhnb Exp $ */
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
	public MessageSendByteArray(MessageType type, SocketChannel source, byte[] hash) {
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
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(hash);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		hash = in.readByteArray();
	}
}
