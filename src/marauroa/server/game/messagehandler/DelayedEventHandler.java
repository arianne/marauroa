/* $Id: DelayedEventHandler.java,v 1.3 2010/05/16 15:24:24 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package marauroa.server.game.messagehandler;

import marauroa.server.game.rp.RPServerManager;

/**
 * A handler for delayed events.
 *
 * @author hendrik
 */
public interface DelayedEventHandler {

	/**
	 * handles an delayed event
	 *
	 * @param rpMan RPServerManager
	 * @param data some data as defined by the handler
	 */
	public void handleDelayedEvent(RPServerManager rpMan, Object data);
}
