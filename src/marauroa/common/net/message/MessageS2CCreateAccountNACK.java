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
package marauroa.common.net.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.game.Result;


/**
 * This message indicate the client that the server has reject its create account Message
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageS2CCreateAccountNACK extends Message {
	private static Logger logger = Log4J.getLogger(MessageS2CCreateAccountNACK.class);

	private Result reason;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CCreateAccountNACK() {
		super(MessageType.S2C_CREATEACCOUNT_NACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the cause of the
	 * rejection.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param resolution
	 *            the reason to deny the create account
	 */
	public MessageS2CCreateAccountNACK(SocketChannel source, Result resolution) {
		super(MessageType.S2C_CREATEACCOUNT_NACK, source);
		reason = resolution;
	}

	/**
	 * This method returns the resolution of the login event
	 *
	 * @return a byte representing the resolution given.
	 */
	public Result getResolutionCode() {
		return reason;
	}

	/**
	 * This method returns a String that represent the resolution given to the
	 * login event
	 *
	 * @return a string representing the resolution.
	 */
	public String getResolution() {
		return reason.getText();
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Create Account NACK) from (" + getAddress() + ") CONTENTS: ("
		        + getResolution() + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write((byte) reason.ordinal());
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		try {
			reason = Result.values()[in.readByte()];
		} catch (ArrayIndexOutOfBoundsException e) {
			logger.error(e, e);
			reason = Result.FAILED_EXCEPTION;
		}

		if (type != MessageType.S2C_CREATEACCOUNT_NACK) {
			throw new IOException();
		}
	}
}
