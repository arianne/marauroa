/* $Id: MessageSendByteArray.java,v 1.5 2006/08/26 20:00:31 nhnb Exp $ */
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

/**
 * This message is a generic message that send a byte array.
 * 
 * @see marauroa.common.net.Message
 */
public class MessageSendByteArray extends Message {
	protected byte[] hash;

	/** Constructor for allowing creation of an empty message */
	public MessageSendByteArray(MessageType type) {
		super(type, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the byte
	 * array to send.
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param hash
	 *            The byte array you want to send.
	 */
	public MessageSendByteArray(MessageType type, InetSocketAddress source,
			byte[] hash) {
		super(type, source);
		this.hash = hash;
	}

	/**
	 * This method returns the byte array.
	 * 
	 * @return the byte array
	 */
	public byte[] getHash() {
		return hash;
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
		out.write(hash);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		hash = in.readByteArray();
	}
}
