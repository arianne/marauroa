/* $Id: IWorker.java,v 1.9 2010/06/12 15:08:42 nhnb Exp $ */
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
 */
public interface IWorker {

	/**
	 * This method associate this worker with a NIO Server.
	 *
	 * @param server NioServer
	 */
	public void setServer(NioServer server);

	/** 
	 * This is a callback method that is called onConnect
	 *
	 * @param channel SocketChannel
	 */
	public void onConnect(SocketChannel channel);

	/**
	 * This method is called when data is received from a socket channel
	 *
	 * @param server NioServer
	 * @param channel SocketChannel
	 * @param data byte area containing the data
	 * @param count number of bytes used in the provided array
	 */
	public void onData(NioServer server, SocketChannel channel, byte[] data, int count);
}