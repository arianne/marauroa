/***************************************************************************
 *                   (C) Copyright 2007-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.client;

import marauroa.common.net.message.MessageS2CLoginNACK.Reasons;

/**
 * this exception is thrown when a login failed for example because of a wrong password.
 */
public class LoginFailedException extends Exception {

	private static final long serialVersionUID = -6977739824675973192L;
	private Reasons reason;

	/**
	 * creates a new LoginFailedException
	 *
	 * @param reason a human readable error message
	 */
	public LoginFailedException(String reason) {
		super("Login failed: " + reason);
	}

	/**
	 * creates a new LoginFailedException
	 *
	 * @param reason a human readable error message
	 * @param reasonCode enumeration instance
	 */
	public LoginFailedException(String reason, Reasons reasonCode) {
		this(reason);
		this.reason = reasonCode;
	}

	/**
	 * gets the reason
	 *
	 * @return Reason
	 */
	public Reasons getReason() {
		return reason;
	}
}
