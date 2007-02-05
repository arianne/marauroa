/* $Id: MessageC2SCreateAccount.java,v 1.1 2007/02/05 18:37:40 arianne_rpg Exp $ */
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
 * This message indicate the server to create an account.
 * 
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SCreateAccount extends Message {
	private String username;

	private String password;

	private String email;
	
	private RPObject template;

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
	 * @see marauroa.common.net.message.MessageS2CCharacterList
	 */
	public MessageC2SCreateAccount(SocketChannel source, String username,
			String password, String email, RPObject template) {
		super(MessageType.C2S_CREATEACCOUNT, source);
		this.username = username;
		this.password = password;
		this.email = email;
		this.template = template;
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
	
	public RPObject getTemplate() {
		return template;
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
				+ username + ";" + password + ";" + email + ";" + template +")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);
		out.write(username);
		out.write(password);
		out.write(email);
		out.write(template);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, java.lang.ClassNotFoundException {
		super.readObject(in);
		username = in.readString();
		password = in.readString();
		email = in.readString();
		template=(RPObject)in.readObject(new RPObject());

		if (type != MessageType.C2S_CREATEACCOUNT) {
			throw new java.lang.ClassNotFoundException();
		}
	}
};
