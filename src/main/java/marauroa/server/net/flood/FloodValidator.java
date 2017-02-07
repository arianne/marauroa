/***************************************************************************
 *                   (C) Copyright 2003-2008 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.flood;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.net.Channel;
import marauroa.server.net.IDisconnectedListener;
import marauroa.server.net.INetworkServerManager;

/**
 * This class implements a Flood checking for all the connections to server.
 * What is a flood is implemented at the IFloodCheck interface.
 *
 * @author miguel
 *
 */
public class FloodValidator implements IDisconnectedListener, Iterable<FloodMeasure> {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(FloodValidator.class);

	private INetworkServerManager netMan;

	/** Stores the connections */
	Map<Channel, FloodMeasure> connections;

	/** This interface implements the flood checking. */
	IFloodCheck floodCheck;

	/**
	 * Constructor
	 *
	 * @param netMan INetworkServerManager
	 * @param check
	 *            the implementation of the flood check.
	 */
	public FloodValidator(INetworkServerManager netMan, IFloodCheck check) {
		this.netMan = netMan;
		this.floodCheck = check;
		connections = new HashMap<Channel, FloodMeasure>();
	}

	/**
	 * Adds a new channel to the flood validator.
	 *
	 * @param channel
	 *            the new added channel.
	 */
	public void add(Channel channel) {
		connections.put(channel, new FloodMeasure(channel));
	}

	/**
	 * Callback method. It will be called by NIOServer when the connection is
	 * closed.
	 */
	public void onDisconnect(Channel channel) {
		connections.remove(channel);
	}

	/**
	 * Returns true if the channel passed as param is considered to be flooding
	 *
	 * @param channel
	 *            the channel we got the new message from
	 * @param length
	 *            the length in bytes of the message.
	 * @return true if it is flooding.
	 */
	public boolean isFlooding(SocketChannel channel, int length) {
		FloodMeasure entry = connections.get(netMan.getChannel(channel));
		if (entry == null) {
			logger.warn("This connection is not registered. Impossible: " + channel);
			return true;
		}

		entry.addMessage(length);

		boolean result = floodCheck.isFlooding(entry);

		if (result) {
			logger.info("Connection determined to be flooding");
			entry.warning();
		}

		return result;
	}

	/**
	 * This method will call onFlood method of the flood check so appropiate
	 * actions can be taken for a flooding channel
	 *
	 * @param channel SocketChannel
	 */
	public void onFlood(SocketChannel channel) {
		FloodMeasure entry = connections.get(netMan.getChannel(channel));
		if (entry == null) {
			logger.warn("This connection is not registered. Impossible: " + channel);
			return;
		}

		floodCheck.onFlood(entry);
	}

	/**
	 * Returns an iterator over the flood measure entries.
	 *
	 * @return an iterator over the flood measure entries.
	 */
	public Iterator<FloodMeasure> iterator() {
		return connections.values().iterator();
	}
}
