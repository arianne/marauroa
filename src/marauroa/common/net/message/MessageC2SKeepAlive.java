/* $Id: MessageC2SKeepAlive.java,v 1.1 2008/01/25 20:44:00 arianne_rpg Exp $ */
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
 * This message is sent from client to server to indicate that he is still there
 * and that wants to be considered connected. 
 * <p>
 * There are strange situations on TCP that cause a considerable amount of time to 
 * be ellapsed until the stack realized that a timeout happens.
 */
public class MessageC2SKeepAlive extends Message {

	/** Constructor for allowing creation of an empty message */
	public MessageC2SKeepAlive() {
		super(MessageType.C2S_KEEPALIVE, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageC2SKeepAlive(SocketChannel source) {
		super(MessageType.C2S_KEEPALIVE, source);
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Keep Alive) from (" + getAddress() + ") CONTENTS: ()";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		if (type != MessageType.C2S_KEEPALIVE) {
			throw new IOException();
		}
	}
};
