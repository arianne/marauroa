/* $Id: NIONetworkServerManager.java,v 1.46 2010/11/25 08:25:03 martinfuchs Exp $ */
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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Log4J;
import marauroa.common.net.Channel;
import marauroa.common.net.ConnectionManager;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CConnectNACK;
import marauroa.server.net.nio.NIONetworkConnectionManager;
import marauroa.server.net.validator.ConnectionValidator;

/**
 * This is the implementation of a worker that sends messages, receives them, ...
 * This class also handles validation of connection and disconnection events
 * 
 * @author miguel
 * 
 */
public final class NetworkServerManager implements IServerManager, INetworkServerManager {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J
	        .getLogger(NetworkServerManager.class);

	/** We store the server for sending stuff. */
	private List<ConnectionManager> connectionManagers;

	/** A List of Message objects: List<Message> */
	private BlockingQueue<Message> messages;

	/** checks if the ip-address is banned */
	private ConnectionValidator connectionValidator;

	/** A list of the listeners to the onDisconnect event. */
	private List<IDisconnectedListener> listeners;

	/** A mapping from internal channels to Channels */ 
	private Map<Object, Channel> channels;

	/**
	 * Constructor
	 * 
	 * @throws IOException
	 *             if there any exception when starting the socket server.
	 */
	public NetworkServerManager() throws IOException {
		// init the packet validator (which can now only check if the address is banned)
		connectionValidator = new ConnectionValidator();

		messages = new LinkedBlockingQueue<Message>();
		channels = Collections.synchronizedMap(new HashMap<Object, Channel>());
		listeners = new LinkedList<IDisconnectedListener>();
		connectionManagers = new LinkedList<ConnectionManager>();

		logger.debug("NetworkServerManager started successfully");

		NIONetworkConnectionManager nio = new NIONetworkConnectionManager(this);
		nio.start();
		connectionManagers.add(nio);
	}


	@Override
	public void start() {
		// do nothing
	}

	/**
	 * Associate this object with a server. This model a master-slave approach
	 * for managing network messages.
	 * 
	 * @param server
	 *            the master server.
	 */
	public void addServer(ConnectionManager server) {
		this.connectionManagers.add(server);
	}

	/**
	 * This method notifies the thread to finish the execution
	 */
	public void finish() {
		logger.debug("shutting down NetworkServerManager");

		connectionValidator.finish();
		for (ConnectionManager server : connectionManagers) {
			server.finish();
		}

		boolean waiting;
		do {
			waiting = false;
			for (ConnectionManager server : connectionManagers) {
				if (!server.isFinished()) {
					waiting = true;
					break;
				}
			}
			Thread.yield();
		} while (waiting);

		logger.debug("NetworkServerManager is down");
	}

	/**
	 * This method blocks until a message is available
	 * 
	 * @return a Message
	 */
	public Message getMessage() {
		try {
			return messages.take();
		} catch (InterruptedException e) {
			/* If interrupted while waiting we just return null */
			return null;
		}
	}


	/**
	 * This method add a message to be delivered to the client the message is
	 * pointed to.
	 * 
	 * @param msg
	 *            the message to be delivered.
	 */
	public void sendMessage(Message msg) {
		if (logger.isDebugEnabled()) {
			logger.debug("send message(type=" + msg.getType() + ") from " + msg.getClientID()
			        + " full [" + msg + "]");
		}

		Channel channel = msg.getChannel();
		channel.getConnectionManager().send(channel.getInternalChannel(), msg);
	}

	/**
	 * This method disconnect a socket.
	 * 
	 * @param channel
	 *            the socket channel to close
	 */
	public void disconnectClient(Channel channel) {
		try {
			channel.getConnectionManager().close(channel.getInternalChannel());
		} catch (Exception e) {
			logger.error("Unable to disconnect a client " + channel.getInetAddress(), e);
		}

	}

	/**
	 * Returns a instance of the connection validator
	 * {@link ConnectionValidator} so that other layers can manipulate it for
	 * banning IP.
	 * 
	 * @return the Connection validator instance
	 */
	public ConnectionValidator getValidator() {
		return connectionValidator;
	}

	/**
	 * Register a listener for disconnection events.
	 * 
	 * @param listener
	 *            a listener for disconnection events.
	 */
	public void registerDisconnectedListener(IDisconnectedListener listener) {
		this.listeners.add(listener);
	}


	//
	// IServerManager
	//

	/**
	 * handles a connection from a client
	 */
	public Channel onConnect(ConnectionManager connectionManager, InetAddress address, Object internalChannel) {
		if (connectionValidator.checkBanned(address)) {
			logger.debug("Reject connection from banned IP: " + address);
			return null;
		}

		Channel channel = new Channel(connectionManager, address, internalChannel);
		this.channels.put(internalChannel, channel);
		return channel;
	}

	/**
	 * Removes stored parts of message for this channel at the decoder.
	 * 
	 * @param internalChannel
	 *            the channel to clear
	 */
	public void onDisconnect(ConnectionManager server, Object internalChannel) {
		Channel channel = channels.get(internalChannel);
		for (IDisconnectedListener listener : listeners) {
			listener.onDisconnect(channel);
		}
		channels.remove(internalChannel);
	}

	/**
	 * This method is called when data is received from a socket channel
	 *
	 * @param server NioServer
	 * @param internalChannel internal channel object
	 * @param msg a Message
	 */
	public void onMessage(ConnectionManager server, Object internalChannel, Message msg) {
		Channel channel = channels.get(internalChannel);
		msg.setChannel(channel);
		messages.add(msg);
	}

}
