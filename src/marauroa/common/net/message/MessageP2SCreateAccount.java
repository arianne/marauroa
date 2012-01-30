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

/**
 * This message indicate the server to create an account.
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageP2SCreateAccount extends Message {
	/** authentication */
	private String credentials;

	/** the ip address this request is forwarded for */
	private String forwardedFor;

	/** Desired username */
	private String username;

	/** Desired password */
	private String password;

	/** email address for whatever thing it may be needed. */
	private String email;

	/** Constructor for allowing creation of an empty message */
	public MessageP2SCreateAccount() {
		super(MessageType.P2S_CREATEACCOUNT, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and username, password
	 * and email associated to the account to be created.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param credentials
	 *            authentication
	 * @param forwardedFor
	 *            forwarded for
	 * @param username
	 *            desired username
	 * @param password
	 *            desired password
	 * @param email
	 *            email of the player
	 */
	public MessageP2SCreateAccount(SocketChannel source, String credentials, String forwardedFor, String username, String password,
	        String email) {
		super(MessageType.P2S_CREATEACCOUNT, source);
		this.credentials = credentials;
		this.forwardedFor = forwardedFor;
		this.username = username;
		this.password = password;
		this.email = email;
	}

	/**
	 * Returns the credentials
	 *
	 * @return the credentials
	 */
	public String getCredentials() {
		return credentials;
	}

	/**
	 * the ip-address this request is forwarded for
	 *
	 * @return IP-address
	 */
	public String getForwardedFor() {
	    return forwardedFor;
    }

	/**
	 * Returns desired account's username
	 * @return desired account's username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns desired account's password
	 * @return desired account's password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Returns the account associated email.
	 * @return the account associated email.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S CreateAccount) from (" + getAddress() + " claiming to act for " + forwardedFor + ") CONTENTS: (" + username
		        + ";" + password + ";" + email + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(credentials);
		out.write(forwardedFor);
		out.write(username);
		out.write(password);
		out.write(email);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		credentials = in.readString();
		forwardedFor = in.readString();
		username = in.readString();
		password = in.readString();
		email = in.readString();

		if (type != MessageType.P2S_CREATEACCOUNT) {
			throw new IOException();
		}
	}
}
