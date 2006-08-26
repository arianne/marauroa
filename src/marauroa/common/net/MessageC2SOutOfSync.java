/* $Id: MessageC2SOutOfSync.java,v 1.4 2006/08/26 20:00:30 nhnb Exp $ */
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
 * The Logout Message is sent from client to server to indicate that it wants to
 * finish the session.
 */
public class MessageC2SOutOfSync extends Message {
	/** Constructor for allowing creation of an empty message */
	public MessageC2SOutOfSync() {
		super(MessageType.C2S_OUTOFSYNC, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageC2SOutOfSync(InetSocketAddress source) {
		super(MessageType.C2S_OUTOFSYNC, source);
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Out of Sync) from ("
				+ source.getAddress().getHostAddress() + ") CONTENTS: ()";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		if (type != MessageType.C2S_OUTOFSYNC) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
