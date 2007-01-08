/* $Id: IWorker.java,v 1.3 2007/01/08 19:26:14 arianne_rpg Exp $ */
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

import marauroa.server.net.IDisconnectedListener;


public interface IWorker extends IDisconnectedListener{
	public abstract void setServer(NioServer server);

	public abstract void onConnect(SocketChannel socket);

	public abstract void onData(NioServer server, SocketChannel socket,
			byte[] data, int count);

}