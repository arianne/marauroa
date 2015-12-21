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
import java.util.Locale;
import java.util.Map;

import marauroa.common.net.Channel;

/**
 * This message indicate the server to create an account.
 *
 * @see marauroa.common.net.message.Message
 */
public class MessageC2SCreateAccount extends Message {

	/** Desired username */
	private String username;

	/** Desired password */
	private String password;

	/** email address for whatever thing it may be needed. */
	private String email;

	/** client language */
	private String language = Locale.ENGLISH.getLanguage();

	/** Constructor for allowing creation of an empty message */
	public MessageC2SCreateAccount() {
		super(MessageType.C2S_CREATEACCOUNT, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and username, password
	 * and email associated to the account to be created.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param username
	 *            desired username
	 * @param password
	 *            desired password
	 * @param email
	 *            email of the player
	 * @param language
	 *            client language 
	 */
	public MessageC2SCreateAccount(Channel source, String username, String password,
	        String email, String language) {
		super(MessageType.C2S_CREATEACCOUNT, source);
		this.username = username;
		this.password = password;
		this.email = email;
		this.language = language;
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
	 * gets the language
	 *
	 * @return language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Message (C2S CreateAccount) from (" + getAddress() + ") CONTENTS: (" + username
		        + ";" + password + ";" + email + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);
		out.write(username);
		out.write(password);
		out.write(email);
		out.write255LongString(language);
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);
		username = in.readString();
		password = in.readString();
		email = in.readString();
		if (in.available() > 0) {
			language = in.read255LongString();
		}

		if (type != MessageType.C2S_CREATEACCOUNT) {
			throw new IOException();
		}
	}

	@Override
	public void readFromMap(Map<String, Object> in) throws IOException {
		super.readFromMap(in);
		if (in.get("u") != null) {
			username = in.get("u").toString();
		}
		if (in.get("p") != null) {
			password = in.get("p").toString();
		}
		if (in.get("e") != null) {
			email = in.get("e").toString();
		}
		if (in.get("l") != null) {
			language = in.get("l").toString();
		}
		if (type != MessageType.C2S_CREATEACCOUNT) {
			throw new IOException();
		}
	}
}
