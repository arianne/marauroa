/* $Id: MessageC2SOutOfSync.java,v 1.3 2007/04/09 14:39:56 arianne_rpg Exp $ */
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

/**
 * If client gets out of sync with server because of a problem in communications, it can
 * request server to send a sync data frame to recover synchronization with server.
 */
public class MessageC2SOutOfSync extends Message {

	/** Constructor for allowing creation of an empty message */
	public MessageC2SOutOfSync() {
		super(MessageType.C2S_OUTOFSYNC, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageC2SOutOfSync(SocketChannel source) {
		super(MessageType.C2S_OUTOFSYNC, source);
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Out of Sync) from (" + getAddress() + ") CONTENTS: ()";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		if (type != MessageType.C2S_OUTOFSYNC) {
			throw new IOException();
		}
	}
};
