/* $Id: MessageS2CCreateAccountACK.java,v 1.2 2007/02/27 14:05:30 arianne_rpg Exp $ */
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

import marauroa.common.game.RPObject;

/**
 * This message indicate the client that the server has accepted its create
 * account Message
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CCreateAccountACK extends Message {
	private String username;

	private RPObject template;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCreateAccountACK() {
		super(MessageType.S2C_CREATEACCOUNT_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageS2CCreateAccountACK(SocketChannel source, String username, RPObject template) {
		super(MessageType.S2C_CREATEACCOUNT_ACK, source);
		this.username=username;
		this.template=template;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C CreateAccount ACK) from ("
				+ getAddress() + ") CONTENTS: ()";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
		out.write(username);
		out.write(template);
		}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);

		username = in.readString();
		template=(RPObject)in.readObject(new RPObject());

		if (type != MessageType.S2C_CREATEACCOUNT_ACK) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
