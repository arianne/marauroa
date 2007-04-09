/* $Id: IWorker.java,v 1.7 2007/04/09 14:40:02 arianne_rpg Exp $ */
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

/**
 * This interface should be implemented by all the classes that are added as
 * workers to NIOServer
 * 
 * @author miguel
 * 
 */
public interface IWorker {

	/** This method associate this worker with a NIO Server. */
	public abstract void setServer(NioServer server);

	/** This is a callback method that is called onConnect */
	public abstract void onConnect(SocketChannel socket);

	/** This method is called when data is recieved from a socket channel */
	public abstract void onData(NioServer server, SocketChannel socket, byte[] data, int count);
}