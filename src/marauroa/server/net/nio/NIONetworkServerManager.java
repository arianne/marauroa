/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Log4J;
import marauroa.common.Utility;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.NetConst;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CConnectNACK;
import marauroa.common.net.message.MessageS2CInvalidMessage;
import marauroa.server.game.Statistics;
import marauroa.server.net.IDisconnectedListener;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.flood.FloodValidator;
import marauroa.server.net.flood.IFloodCheck;
import marauroa.server.net.validator.ConnectionValidator;

/**
 * This is the implementation of a worker that sends messages, receives them,
 * This class also handles validation of connection and disconnection events
 *
 * @author miguel
 */
public final class NIONetworkServerManager extends Thread implements IWorker, IDisconnectedListener,
        INetworkServerManager {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J
	        .getLogger(NIONetworkServerManager.class);

	/** We store the server for sending stuff. */
	private NioServer server;

	/** While keepRunning is true, we keep receiving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isFinished;

	/** A List of Message objects: List<Message> */
	private final BlockingQueue<Message> messages;

	/** Statistics */
	private final Statistics stats;

	/** checks if the ip-address is banned */
	private final ConnectionValidator connectionValidator;

	/** Checks if a connection is flooding the server */
	private final FloodValidator floodValidator;

	/** We queue here the data events. */
	private final BlockingQueue<DataEvent> queue;

	/** encoder is in charge of getting a Message and creating a stream of bytes. */
	private final Encoder encoder;

	/** decoder takes a stream of bytes and create a message */
	private final Decoder decoder;

	/**
	 * Constructor
	 *
	 * @throws IOException
	 *             if there any exception when starting the socket server.
	 */
	@SuppressWarnings("deprecation")
	public NIONetworkServerManager() throws IOException {
		super("NetworkServerManager");
		/*
		 * init the packet validator (which can now only check if the address is
		 * banned)
		 */
		connectionValidator = new ConnectionValidator();

		/* create a flood check on connections */
		IFloodCheck check = new FloodCheck(this);
		floodValidator = new FloodValidator(check);

		keepRunning = true;
		isFinished = false;

		encoder = Encoder.get();
		decoder = Decoder.get();

		/*
		 * Because we access the list from several places we create a
		 * synchronized list.
		 */
		messages = new LinkedBlockingQueue<Message>();
		stats = Statistics.getStatistics();
		queue = new LinkedBlockingQueue<DataEvent>();

		logger.debug("NetworkServerManager started successfully");

		server = new NioServer(null, NetConst.tcpPort, this);
		server.start();

		/*
		 * Register network listener for get disconnection events.
		 */
		server.registerDisconnectedListener(this);
		server.registerDisconnectedListener(floodValidator);
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

		connectionValidator.finish();
		server.finish();
		interrupt();

		while (isFinished == false) {
			Thread.yield();
		}

		logger.debug("NetworkServerManager is down");
	}

	/**
	 * This method blocks until a message is available
	 *
	 * @return a Message
	 */
	public synchronized Message getMessage() {
		try {
			return messages.take();
		} catch (InterruptedException e) {
			/* If interrupted while waiting we just return null */
			return null;
		}
	}

	/**
	 * We check that this socket is not banned. We do it just on connect so we
	 * save lots of queries.
	 */
	public void onConnect(SocketChannel channel) {
		Socket socket = channel.socket();

		logger.info("Connected from " + socket.getRemoteSocketAddress());
		if (connectionValidator.checkBanned(socket)) {
			logger.info("Reject connection from banned IP: " + socket.getInetAddress());

			/*
			 * Sends a connect NACK message if the address is banned.
			 */
			MessageS2CConnectNACK msg = new MessageS2CConnectNACK();
			msg.setSocketChannel(channel);
			sendMessage(msg);

			/*
			 * NOTE: We should wait a bit to close the channel... to make sure
			 * message is sent *before* closing it, otherwise client won't know
			 * about it.
			 */

			/* If address is banned, just close connection */
			server.close(channel);
		} else {
			/*
			 * If the address is not banned, add it to flood validator for
			 * checking flooding.
			 */
			floodValidator.add(channel);
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
	 * @param msg
	 *            the message to be delivered.
	 */
	public void sendMessage(Message msg) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("send message(type=" + msg.getType() + ") from " + msg.getClientID()
				        + " full [" + msg + "]");
			}

			byte[] data = encoder.encode(msg);

			stats.add("Bytes send", data.length);
			stats.add("Message send", 1);

			server.send(msg.getSocketChannel(), data);
		} catch (IOException e) {
			e.printStackTrace();
			/**
			 * I am not interested in the exception. NioServer will detect this
			 * and close connection
			 */
		}
	}

	/**
	 * This method disconnect a socket.
	 *
	 * @param channel
	 *            the socket channel to close
	 */
	public void disconnectClient(SocketChannel channel) {
		try {
			server.close(channel);
		} catch (Exception e) {
			logger.error("Unable to disconnect a client " + channel.socket(), e);
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
		server.registerDisconnectedListener(listener);
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

							messages.add(msg);
						}
					}
				} catch (InvalidVersionException e) {
					logger.warn("Invalid version message: \n" + Utility.dumpByteArray(event.data), e);
					logger.warn("sender was: " + event.channel.socket().getRemoteSocketAddress());
					stats.add("Message invalid version", 1);
					MessageS2CInvalidMessage invMsg = new MessageS2CInvalidMessage(event.channel, "Invalid client version: Update client");
					invMsg.setProtocolVersion(e.getProtocolVersion());
					sendMessage(invMsg);
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
	}
}
