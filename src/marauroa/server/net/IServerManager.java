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
package marauroa.server.net;

import java.net.InetSocketAddress;

import marauroa.common.net.Channel;
import marauroa.common.net.ConnectionManager;
import marauroa.common.net.message.Message;

/**
 * This interface allows ConnectionManagers to talk to the central server manager.
 *
 * @author miguel
 */
public interface IServerManager {

	/**
	 * Adds a server to the worker
	 *
	 * @param server IServer
	 */
	public void addServer(ConnectionManager server);

	/**
	 * This is a callback method that is called onConnect
	 *
	 * @param server IServer
	 * @param address of the client
	 * @param internalChannel internal channel object
	 * @return the channel, if the connection was accepted; <code>null</code> otherwise
	 */
	public Channel onConnect(ConnectionManager server, InetSocketAddress address, Object internalChannel);

	/**
	 * This method is called when data is received from a socket channel
	 *
	 * @param server ConnectionManager
	 * @param internalChannel internal channel object
	 * @param message a Message
	 */
	public void onMessage(ConnectionManager server, Object internalChannel, Message message);

	/**
	 * This method is called when the client is disconnected
	 *
	 * @param server ConnectionManager
	 * @param internalChannel internal channel object
	 */
	public void onDisconnect(ConnectionManager server, Object internalChannel);

	/**
	 * Register a listener for disconnection events.
	 *
	 * @param listener
	 *            a listener for disconnection events.
	 */
	public void registerDisconnectedListener(IDisconnectedListener listener);

}