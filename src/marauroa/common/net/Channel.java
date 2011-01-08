/* $Id: IDisconnectedListener.java,v 1.6 2007/04/09 14:40:01 arianne_rpg Exp $ */
/***************************************************************************
 *                    (C) Copyright 2007-2011 - Marauroa                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net;

import java.net.InetAddress;



/**
 * a connection between client and server
 *
 * @author hendrik
 */
public class Channel {

	private Object internalChannel;
	private InetAddress address;
	private ConnectionManager connectionManager;

	/**
	 * creates a new Channel object.
	 */
	public Channel() {
		// default constructor
	}

	/**
	 * creates a new Channel object.
	 *
	 * @param connectionManager the manager to contact to send anything using this channel
	 * @param address the ip-address of the other end
	 * @param internalChannel an internal channel object used by the manager
	 */
	public Channel(ConnectionManager connectionManager, InetAddress address, Object internalChannel) {
		this.connectionManager = connectionManager;
		this.address = address;
		this.internalChannel = internalChannel;
	}

	/**
	 * gets the internal channel
	 *
	 * @return internal channel
	 */
	public Object getInternalChannel() {
		return internalChannel;
	}

	/**
	 * gets the ip-address of the other end
	 *
	 * @return InetAddress
	 */
	public InetAddress getInetAddress() {
		return address;
	}

	/**
	 * gets the connection manager
	 *
	 * @return connection manager
	 */
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}
}
