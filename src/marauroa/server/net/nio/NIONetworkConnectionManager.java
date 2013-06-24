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
package marauroa.server.net.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Utility;
import marauroa.common.net.Channel;
import marauroa.common.net.ConnectionManager;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.NetConst;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CConnectNACK;
import marauroa.common.net.message.MessageS2CInvalidMessage;
import marauroa.server.game.Statistics;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.IServerManager;
import marauroa.server.net.flood.FloodValidator;
import marauroa.server.net.flood.IFloodCheck;

/**
 * This is the implementation of a worker that sends messages, receives them, ...
 * This class also handles validation of connection and disconnection events
 *
 * @author miguel
 *
 */
public final class NIONetworkConnectionManager extends Thread implements IWorker, ConnectionManager {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J
	        .getLogger(NIONetworkConnectionManager.class);

	/** We store the server for sending stuff. */
	private NioServer server;

	/** While keepRunning is true, we keep receiving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isFinished;

	/** Statistics */
	private final Statistics stats;

	/** Checks if a connection is flooding the server */
	private final FloodValidator floodValidator;

	/** We queue here the data events. */
	private final BlockingQueue<DataEvent> queue;

	/** encoder is in charge of getting a Message and creating a stream of bytes. */
	private final Encoder encoder;

	/** decoder takes a stream of bytes and create a message */
	private final Decoder decoder;

	/** the central server manager */
	private final IServerManager serverManager;

	/**
	 * Constructor
	 *
	 * @throws IOException
	 *             if there any exception when starting the socket server.
	 */
	public NIONetworkConnectionManager(IServerManager serverManager) throws IOException {
		super("NetworkServerManager");

		/* create a flood check on connections */
		IFloodCheck check = new FloodCheck((INetworkServerManager) serverManager);
		floodValidator = new FloodValidator((INetworkServerManager) serverManager, check);

		keepRunning = true;
		isFinished = false;

		encoder = Encoder.get();
		decoder = Decoder.get();

		stats = Statistics.getStatistics();
		queue = new LinkedBlockingQueue<DataEvent>();

		logger.debug("NetworkServerManager started successfully");

		Configuration conf = Configuration.getConfiguration();
		int port = Integer.parseInt(conf.get("tcp_port"));
		server = new NioServer(null, port, this);
		server.start();

		// Register network listener for get disconnection events.
		serverManager.registerDisconnectedListener(floodValidator);

		this.serverManager = serverManager;
	}

	/**
	 * Associate this object with a server. This model a master-slave approach
	 * for managing network messages.
	 *
	 * @param server
	 *            the master server.
	 */
	public void setServer(NioServer server) {
		this.server = server;
	}

	/**
	 * This method notifies the thread to finish the execution
	 */
	public void finish() {
		logger.debug("shutting down NetworkServerManager");
		keepRunning = false;

		server.finish();
		interrupt();
	}

	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * We check that this socket is not banned. We do it just on connect so we
	 * save lots of queries.
	 */
	public void onConnect(SocketChannel internalChannel) {

		Channel channel = serverManager.onConnect(this, (InetSocketAddress) internalChannel.socket().getRemoteSocketAddress(), internalChannel);
		if (channel != null) {
			/*
			 * If the address is not banned, add it to flood validator for
			 * checking flooding.
			 */
			floodValidator.add(channel);

		} else {
			/*
			 * Sends a connect NACK message if the address is banned.
			 */
			MessageS2CConnectNACK msg = new MessageS2CConnectNACK();
			send(internalChannel, msg, true);

			/*
			 * NOTE: We should wait a bit to close the channel... to make sure
			 * message is sent *before* closing it, otherwise client won't know
			 * about it.
			 */

			/* If address is banned, just close connection */
			server.close(internalChannel);
		}

	}

	/**
	 * This method is called when new data is received on server from channel.
	 *
	 * @param server
	 *            the master server
	 * @param channel
	 *            socket channel associated to the event
	 * @param data
	 *            the data received
	 * @param count
	 *            the amount of data received.
	 */
	public void onData(NioServer server, SocketChannel channel, byte[] data, int count) {
		logger.debug("Received from channel:"+channel+" "+count+" bytes");

		stats.add("Bytes recv", count);
		stats.add("Message recv", 1);

		/*
		 * We check the connection in case it is trying to flood server.
		 */
		if (floodValidator.isFlooding(channel, count)) {
			/*
			 * If it is flooding, let the validator decide what to do.
			 */
			logger.warn("Channel: "+channel+" is flooding");
			floodValidator.onFlood(channel);
		} else {
			/*
			 * If it is not flooding, just queue the message.
			 */
			logger.debug("queueing message");

			byte[] dataCopy = new byte[count];
			System.arraycopy(data, 0, dataCopy, 0, count);
			try {
				queue.put(new DataEvent(channel, dataCopy));
			} catch (InterruptedException e) {
				/* This is never going to happen */
				logger.error("Not expected",e);
			}
		}
	}

	/**
	 * This method add a message to be delivered to the client the message is
	 * pointed to.
	 *
	 * @param internalChannel the channel to the client
	 * @param msg the message to be delivered.
	 * @param isPerceptionRequired true indicates that the message may not be skipped
	 */
	public void send(Object internalChannel, Message msg, boolean isPerceptionRequired) {
		if (!isPerceptionRequired && msg.isSkippable() && (msg.getProtocolVersion() >= NetConst.FIRST_VERSION_WITH_OMITTABLE_EMPTY_PERCEPTIONS)) {
			return;
		}
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("send message(type=" + msg.getType() + ") from " + msg.getClientID()
				        + " full [" + msg + "]");
			}

			byte[] data = encoder.encode(msg);

			stats.add("Bytes send", data.length);
			stats.add("Message send", 1);

			server.send((SocketChannel) internalChannel, data);
		} catch (IOException e) {
			// I am not interested in the exception. NioServer will detect this
			// and close connection
			logger.debug(e, e);
		}
	}


	/**
	 * This method disconnect a socket.
	 *
	 * @param channel
	 *            the socket channel to close
	 */
	public void close(Object channel) {
		try {
			server.close((SocketChannel) channel);
		} catch (Exception e) {
			logger.error("Unable to disconnect a client " + ((SocketChannel) channel).socket(), e);
		}
	}

	@Override
	public void run() {
		try {
			while (keepRunning) {
				DataEvent event = queue.take();

				try {
					List<Message> recvMessages = decoder.decode(event.channel, event.data);
					if (recvMessages != null) {
						for (Message msg : recvMessages) {
							if (logger.isDebugEnabled()) {
								logger.debug("recv message(type=" + msg.getType() + ") from "
								        + msg.getClientID() + " full [" + msg + "]");
							}
							serverManager.onMessage(this, event.channel, msg);
						}
					}
				} catch (InvalidVersionException e) {
					logger.warn("Invalid version message: \n" + Utility.dumpByteArray(event.data), e);
					logger.warn("sender was: " + event.channel.socket().getRemoteSocketAddress());
					stats.add("Message invalid version", 1);
					MessageS2CInvalidMessage invMsg = new MessageS2CInvalidMessage(null, "Invalid client version: Update client");
					invMsg.setProtocolVersion(e.getProtocolVersion());
					send(event.channel, invMsg, true);
				} catch (IOException e) {
					logger.warn("IOException while building message:\n" + Utility.dumpByteArray(event.data), e);
					logger.warn("sender was: " + event.channel.socket().getRemoteSocketAddress());
				} catch (RuntimeException e) {
					logger.warn("RuntimeException while building message:\n" + Utility.dumpByteArray(event.data), e);
					logger.warn("sender was: " + event.channel.socket().getRemoteSocketAddress());
				}
			}
		} catch (InterruptedException e) {
			logger.warn(getName()+" interrupted. Finishing network layer.");
			keepRunning = false;
		}

		isFinished = true;
	}

	/**
	 * Removes stored parts of message for this channel at the decoder.
	 *
	 * @param channel
	 *            the channel to clear
	 */
	public void onDisconnect(SocketChannel channel) {
		logger.info("NET Disconnecting " + channel.socket().getRemoteSocketAddress());
		decoder.clear(channel);
		serverManager.onDisconnect(this, channel);
	}

}
