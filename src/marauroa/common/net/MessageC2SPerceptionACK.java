/* $Id: MessageC2SPerceptionACK.java,v 1.6 2007/02/05 18:24:37 arianne_rpg Exp $ */
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
import java.nio.channels.SocketChannel;

/**
 * This message indicate the server that the client has accepted its Perception
 * Message
 * 
 * @see marauroa.common.net.Message
 */
public class MessageC2SPerceptionACK extends Message {
	/** Constructor for allowing creation of an empty message */
	public MessageC2SPerceptionACK() {
		super(MessageType.C2S_PERCEPTION_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public MessageC2SPerceptionACK(SocketChannel source) {
		super(MessageType.C2S_PERCEPTION_ACK, source);
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S Perception ACK) from ("
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
		if (type != MessageType.C2S_PERCEPTION_ACK) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
