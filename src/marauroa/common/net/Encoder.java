/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.net.message.Message;

/**
 * This class encodes a Message as a stream of bytes.
 * Encoder follows singleton pattern.
 *
 * @author miguel
 */
public class Encoder {

	/**
	 * Constructor
	 */
	private Encoder() {
		// hide constructor, this is a Singleton
	}

	/** the singleton instance */
	private static Encoder instance;

	/**
	 * Returns an unique instance of encoder
	 *
	 * @return an unique instance of encoder
	 */
	public static Encoder get() {
		if (instance == null) {
			instance = new Encoder();
		}

		return instance;
	}

	/**
	 * This method uses the Marauroa protocol to encode a Message as a stream of
	 * bytes.
	 *
	 * @param msg
	 *            The message to encode
	 * @return a byte array
	 * @throws IOException
	 *             if there is any error encoding the message.
	 */
	public byte[] encode(Message msg) throws IOException {
		int size = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer s = new OutputSerializer(out);
		s.setProtocolVersion(msg.getProtocolVersion());

		/*
		 * HACK: We make room for 4 bytes that we will set later to specify how big the packet is.
		 */
		s.write(size);
		s.write(msg);

		byte[] data = out.toByteArray();

		size = data.length;
		data[0] = (byte) ((size >> 0) & 255);
		data[1] = (byte) ((size >> 8) & 255);
		data[2] = (byte) ((size >> 16) & 255);
		data[3] = (byte) ((size >> 24) & 255);

		return data;
	}
}
