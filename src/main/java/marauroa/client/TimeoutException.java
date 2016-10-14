/***************************************************************************
 *                   (C) Copyright 2007-2013 - Marauroa                    *
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

import static marauroa.common.i18n.I18N._;

/**
 * this exception is thrown if a connection to the server cannot be established
 * within the given time.
 */
public class TimeoutException extends Exception {

	private static final long serialVersionUID = -6977739824675973192L;

	/**
	 * creates a new TimeoutException
	 */
	public TimeoutException() {
		super(_("Timeout happened while waiting server reply."));
	}
}
