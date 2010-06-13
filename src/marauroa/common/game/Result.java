/* $Id: Result.java,v 1.14 2010/06/13 20:15:28 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.common.game;

/**
 * This enum represent the possible values returned by the create account
 * process. Caller should verify that the process ended in OK_ACCOUNT_CREATED.
 * 
 * @author miguel
 */
public enum Result {

	/** Account was created correctly. */
	OK_CREATED(true, "Account was created correctly."),

	/**
	 * Account was not created because one of the important parameters was
	 * missing.
	 */
	FAILED_EMPTY_STRING(false, "Account was not created because one of the important parameters was missing."),

	/**
	 * Account was not created because an invalid characters (letter, sign,
	 * number) was used
	 */
	FAILED_INVALID_CHARACTER_USED (false, "Account was not created because an invalid character (special letters, signs, numbers) was used."),

	/**
	 * Account was not created because any of the parameters are either too long
	 * or too short.
	 */
	FAILED_STRING_SIZE(false, "Account was not created because any of the parameters are either too long or too short."),

	/** Account was not created because this account already exists. */
	FAILED_PLAYER_EXISTS(false, "Account was not created because this account already exists."),

	/** Account was not created because there was an unspecified exception. */
	FAILED_EXCEPTION(false, "Account was not created because there was an unspecified exception."),

	/** Character was not created because this character already exists. */
	FAILED_CHARACTER_EXISTS(false, "Character was not created because this Character already exists."),

	/**
	 * The template passed to the create character method is not valid because
	 * it fails to pass the RP rules.
	 */
	FAILED_INVALID_TEMPLATE(false, "The template passed to the create character method is not valid because it fails to pass the RP rules."),

	/**
	 * String is too short
	 *
	 * @since 2.1
	 */
	FAILED_STRING_TOO_SHORT(false, "Account was not created because at least one of the parameters was too short."),

	/**
	 * String is too long
	 *
	 * @since 2.1
	 */
	FAILED_STRING_TOO_LONG(false, "Account was not created because at least one of the parameters was too long."),

	/**
	 * Name is reserved
	 *
	 * @since 2.1
	 */
	FAILED_RESERVED_NAME(false, "Account was not created because the name is reserved (or contains a reserved name)."),

	/**
	 * Password is too close to the username
	 *
	 * @since 2.1
	 */
	FAILED_PASSWORD_TOO_CLOSE_TO_USERNAME(false, "Account was not created because the password is too close to the username."),

	/**
	 * Password is too weak.
	 *
	 * @since 2.1
	 */
	FAILED_PASSWORD_TO_WEAK(false, "Account was not created because the password is too weak."),

	/**
	 * Too many accounts were created from this ip-address recently.
	 *
	 * @since 3.5
	 */
	FAILED_TOO_MANY(false, "Account was not created because the account creation limit for your network was reached.\nPlease try again later.");

	/**
	 * Textual description of the result 
	 */
	private String text;

	/**
	 * True if the account is successfully created. 
	 */
	private boolean created;

	Result(boolean created, String text) {
		this.created=created;
		this.text=text;
	}

	/**
	 * checks state of account creation represented by this result.
	 *  
	 * @return  true if account creation failed. false if account creation was successful.
	 */
	public boolean failed() {
		return !created;
	}

	/**
	 * Returns a textual description of the result.
	 * @return a textual description of the result.
	 */
	public String getText() {
		return text;
	}
}
