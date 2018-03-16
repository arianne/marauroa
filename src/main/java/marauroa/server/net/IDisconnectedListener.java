/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net;

import marauroa.common.net.Channel;

/**
 * This interface provides a callback notification for disconnected clients.
 * 
 * @author miguel
 * 
 */
public interface IDisconnectedListener {

	/**
	 * This method is called when a connection is closed.
	 * 
	 * @param channel
	 *            the channel that was closed.
	 */
	public void onDisconnect(Channel channel);

}
