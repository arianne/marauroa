/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
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

import marauroa.common.net.NetConst;

/**
 * This message indicate the client that the server has reject its login Message
 *
 * @see marauroa.common.net.message.Message
 */

public class MessageS2CLoginNACK extends Message {

	public enum Reasons {
		USERNAME_WRONG, 
		/** @since will be replaced by TOO_MANY_TRIES_USERNAME and TOO_MANY_TRIES_IP in the future */
		TOO_MANY_TRIES, 
		USERNAME_BANNED, 
		SERVER_IS_FULL, 
		GAME_MISMATCH, 
		PROTOCOL_MISMATCH, 
		INVALID_NONCE,
		/** @since 3.0 */
		USERNAME_INACTIVE,
		/** @since 3.0 */
		TOO_MANY_TRIES_USERNAME, 
		/** @since 3.0 */
		TOO_MANY_TRIES_IP,
		/** @since 3.7 */
		SEED_WRONG,
		/** @since 3.7.1 */
		ACCOUNT_MERGED;
	}

	static private String[] text = {
	        "Username/Password incorrect.",
	        "There have been too many failed login attempts for your account or network. "
	        	+ "Please wait a couple of minutes or contact support.",
		    "Account is banned.",
	        "Server is full.",
	        "Server is running an incompatible version of game. Please update.",
	        "marauroa.common.network Protocol invalid version: Running "
	                + Integer.toString(NetConst.NETWORK_PROTOCOL_VERSION),
	        "The hash you sent does not correspond to the nonce you sent.",
	        "You account has been marked as inactive, please contact support.",
	        "There have been too many failed login attempts for your account. "
        	+ "Please wait a couple of minutes or contact support.",
	        "There have been too many failed login attempts from your network. "
        	+ "Please wait a couple of minutes or contact support.",
        	"Login expired. Please click again on your character on the web page.",
        	"This account was merged into another account. Please use the username "
        	+"of the other account to login or contact support."};

	/** The reason of login rejection */
	private Reasons reason;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CLoginNACK() {
		super(MessageType.S2C_LOGIN_NACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param resolution
	 *            the reason to deny the login
	 */
	public MessageS2CLoginNACK(SocketChannel source, Reasons resolution) {
		super(MessageType.S2C_LOGIN_NACK, source);
		reason = resolution;
	}

	/**
	 * This method returns the resolution of the login event
	 *
	 * @return a byte representing the resolution given.
	 */
	public Reasons getResolutionCode() {
		return reason;
	}

	/**
	 * This method returns a String that represent the resolution given to the
	 * login event
	 *
	 * @return a string representing the resolution.
	 */
	public String getResolution() {
		return text[reason.ordinal()];
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (S2C Login NACK) from (" + getAddress() + ") CONTENTS: (" + getResolution()
		        + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write((byte) reason.ordinal());
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		reason = Reasons.values()[in.readByte()];

		if (type != MessageType.S2C_LOGIN_NACK) {
			throw new IOException();
		}
	}
}
