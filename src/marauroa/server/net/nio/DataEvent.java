/* $Id: DataEvent.java,v 1.5 2007/03/23 20:39:21 arianne_rpg Exp $ */
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
package marauroa.server.net.nio;

import java.nio.channels.SocketChannel;

/** This class represents a data event.
 * It stores the socket associated and the data that has been recieved.
 * @author miguel
 *
 */
class DataEvent {

	/** Associated socket channel */
	public SocketChannel channel;

	/** Data associated to the event */
	public byte[] data;

	/** Constructor */
	public DataEvent(SocketChannel socket, byte[] data) {
		this.channel = socket;
		this.data = data;
	}
}