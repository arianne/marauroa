/* $Id: LoginFailedException.java,v 1.4 2009/07/17 21:17:25 nhnb Exp $ */
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
package marauroa.client;

/**
 * this exception is thrown when a login failed for example because of a wrong password.
 */
public class LoginFailedException extends Exception {

	private static final long serialVersionUID = -6977739824675973192L;

	/**
	 * creates a new LoginFailedException
	 *
	 * @param reason a human readable error message
	 */
	public LoginFailedException(String reason) {
		super("Login failed: " + reason);
	}
}
