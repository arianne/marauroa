/* $Id: MessageC2SLogout.java,v 1.5 2007/01/18 12:37:45 arianne_rpg Exp $ */
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
import java.nio.channels.SocketChannel;

/**
 * The Logout Message is sent from client to server to indicate that it wants to
 * finish the session.
 */
public class MessageC2SLogout extends Message {
	/** Constructor for allowing creation of an empty message */
	public MessageC2SLogout() {
		super(MessageType.C2S_LOGOUT, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageC2SLogout(SocketChannel source) {
		super(MessageType.C2S_LOGOUT, source);
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Logout) from ("
				+ getAddress() + ") CONTENTS: ()";
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
		if (type != MessageType.C2S_LOGOUT) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
