/* $Id: MessageC2SCreateAccount.java,v 1.4 2007/01/18 12:37:45 arianne_rpg Exp $ */
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
 * This message indicate the server to create an account.
 * 
 * @see marauroa.common.net.Message
 */
public class MessageC2SCreateAccount extends Message {
	private String username;

	private String password;

	private String email;

	/** Constructor for allowing creation of an empty message */
	public MessageC2SCreateAccount() {
		super(MessageType.C2S_CREATEACCOUNT, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and the name
	 * of the choosen character.
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param character
	 *            The name of the choosen character that <b>MUST</b> be one of
	 *            the returned by the marauroa.common.net.MessageS2CCharacters
	 * @see marauroa.common.net.MessageS2CCharacterList
	 */
	public MessageC2SCreateAccount(SocketChannel source, String username,
			String password, String email) {
		super(MessageType.C2S_CREATEACCOUNT, source);
		this.username = username;
		this.password = password;
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

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
		return "Message (C2S CreateAccount) from ("
				+ getAddress() + ") CONTENTS: ("
				+ username + ";" + password + ";" + email + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
		out.write(username);
		out.write(password);
		out.write(email);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		username = in.readString();
		password = in.readString();
		email = in.readString();

		if (type != MessageType.C2S_CREATEACCOUNT) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
