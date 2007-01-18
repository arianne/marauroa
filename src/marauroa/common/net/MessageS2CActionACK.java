/* $Id: MessageS2CActionACK.java,v 1.5 2007/01/18 12:37:46 arianne_rpg Exp $ */
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
 * This message indicate the client that the server has accepted its Action
 * Message
 * 
 * @see marauroa.common.net.Message
 */
public class MessageS2CActionACK extends Message {
	private int actionId;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CActionACK() {
		super(MessageType.S2C_ACTION_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageS2CActionACK(SocketChannel source, int actionId) {
		super(MessageType.S2C_ACTION_ACK, source);
		this.actionId = actionId;
	}

	public int getActionID() {
		return actionId;
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Action ACK) from ("
				+ getAddress()
				+ ") CONTENTS: (action_id=" + actionId + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
		out.write(actionId);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		actionId = in.readInt();
		if (type != MessageType.S2C_ACTION_ACK) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
