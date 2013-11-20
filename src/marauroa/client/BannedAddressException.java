/***************************************************************************
 *                   (C) Copyright 2003-2013 - Marauroa                    *
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
 * this acception is thrown when the ip-address is banned.
 */
public class BannedAddressException extends Exception {

	private static final long serialVersionUID = -6977739824675973192L;

	/**
	 * created a new BannedAddressException.
	 */
	public BannedAddressException() {
		super(_("Your IP Address has been banned."));
	}
}
