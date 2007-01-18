/* $Id: MessageS2CLoginACK.java,v 1.5 2007/01/18 12:37:46 arianne_rpg Exp $ */
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
 * This message indicate the client that the server has accepted its login
 * Message
 * 
 * @see marauroa.common.net.Message
 */
public class MessageS2CLoginACK extends Message {
	/** Constructor for allowing creation of an empty message */
	public MessageS2CLoginACK() {
		super(MessageType.S2C_LOGIN_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageS2CLoginACK(SocketChannel source) {
		super(MessageType.S2C_LOGIN_ACK, source);
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Login ACK) from ("
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
		if (type != MessageType.S2C_LOGIN_ACK) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
