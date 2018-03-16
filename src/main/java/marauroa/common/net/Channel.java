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
import java.net.InetSocketAddress;



/**
 * a connection between client and server
 *
 * @author hendrik
 */
public class Channel {

	private Object internalChannel;
	private InetSocketAddress address;
	private ConnectionManager connectionManager;
	private boolean waitingForPerception;

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
	 * @param address the ip-address and port Oof the other end
	 * @param internalChannel an internal channel object used by the manager
	 */
	public Channel(ConnectionManager connectionManager, InetSocketAddress address, Object internalChannel) {
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
		return address.getAddress();
	}

	/**
	 * gets the ip-address and port of the other end
	 *
	 * @return InetSocketAddress
	 */
	public InetSocketAddress getInetSocketAddress() {
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

	/**
	 * are we waiting for a perception?
	 *
	 * @return true, if we are waiting for a perception
	 */
	public boolean isWaitingForPerception() {
		return waitingForPerception;
	}

	/**
	 * sets the flag indicating whether this connection is waiting for a perception
	 *
	 * @param waitingForPerception waiting for perception
	 */
	public void setWaitingForPerception(boolean waitingForPerception) {
		this.waitingForPerception = waitingForPerception;
	}

	@Override
	public String toString() {
		return "Channel [internalChannel=" + internalChannel + ", address=" + address + "]";
	}

}
