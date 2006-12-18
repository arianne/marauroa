/* $Id: DataEvent.java,v 1.2 2006/12/18 21:11:06 arianne_rpg Exp $ */
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


class DataEvent {
	public SocketChannel channel;
	public byte[] data;
	
	public DataEvent(SocketChannel socket, byte[] data) {
		this.channel = socket;
		this.data = data;
	}
}