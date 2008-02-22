/* $Id: TimeoutException.java,v 1.3 2008/02/22 10:28:31 arianne_rpg Exp $ */
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

public class TimeoutException extends Exception {

	private static final long serialVersionUID = -6977739824675973192L;

	public TimeoutException() {
		super("Timeout happened while waiting server reply");
	}
}
