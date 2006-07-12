/* $Id: NetworkServerManager.java,v 1.20 2006/07/12 14:18:21 nhnb Exp $ */
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

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.net.Message;
import marauroa.common.net.MessageFactory;
import marauroa.common.net.NetConst;
import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;


/** The NetworkServerManager is the active entity of the marauroa.net package,
 *  it is in charge of sending and recieving the packages from the network. */
public final class NetworkServerManager implements NetworkServerManagerCallback {
	/** the logger instance. */
	static final Logger logger = Log4J.getLogger(NetworkServerManager.class);

	/** The server socket from where we recieve the packets. */
	DatagramSocket socket;

	/** While keepRunning is true, we keep recieving messages */
	boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	boolean isfinished;

	/** A List of Message objects: List<Message> */
	List<Message> messages;

	/** MessageFactory */
	MessageFactory msgFactory;

	private NetworkServerManagerRead readManager;

	private NetworkServerManagerWrite writeManager;

	/** Statistics */
	Statistics stats;

	/** checkes if the ip-address is banned */
	PacketValidator packetValidator;

	/** 
	 * Constructor that opens the socket on the marauroa_PORT and start the thread
	 * to recieve new messages from the network.
	 *
	 * @throws SocketException if the server socket cannot be created or bound.
	 */
	public NetworkServerManager() throws SocketException {
		Log4J.startMethod(logger, "NetworkServerManager");
		/* init the packet validater (which can now only check if the address is banned)*/
		packetValidator = new PacketValidator();

		/* Create the socket and set a timeout of 1 second */
		socket = new DatagramSocket(NetConst.marauroa_PORT);
		socket.setSoTimeout(1000);
		try {
			socket.setTrafficClass(0x08 | 0x10);
		} catch (Exception e) {
			logger.warn("Cannot setTrafficClass " + e);
		}
		socket.setSendBufferSize(1500 * 64);

		msgFactory = MessageFactory.getFactory();
		keepRunning = true;
		isfinished = false;
		/* Because we access the list from several places we create a synchronized list. */
		messages = Collections.synchronizedList(new LinkedList<Message>());
		stats = Statistics.getStatistics();
		readManager = new NetworkServerManagerRead(this);
		readManager.start();
		writeManager = new NetworkServerManagerWrite(this, socket, stats);
		logger.debug("NetworkServerManager started successfully");
	}

	/** 
	 * This method notify the thread to finish it execution
	 */
	public void finish() {
		logger.debug("shutting down NetworkServerManager");
		keepRunning = false;
		while (isfinished == false) {
			Thread.yield();
		}

		socket.close();
		logger.debug("NetworkServerManager is down");
	}

	/** 
	 * This methods notifies waiting threads to continue
	 */
	synchronized void newMessageArrived() {
		notifyAll();
	}

	/** 
	 * This method returns a Message from the list or block for timeout milliseconds
	 * until a message is available or null if timeout happens.
	 *
	 * @param timeout timeout time in milliseconds
	 * @return a Message or null if timeout happens
	 */
	public synchronized Message getMessage(int timeout) {
		Log4J.startMethod(logger, "getMessage");
		if (messages.size() == 0) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		Message message;
		if (messages.size() == 0) {
			logger.debug("Message not available.");
			message = null;
		} else {
			logger.debug("Message returned.");
			message = messages.remove(0);
		}
		Log4J.finishMethod(logger, "getMessage");
		return message;
	}

	/** 
	 * This method blocks until a message is available
	 *
	 * @return a Message
	 */
	public synchronized Message getMessage() {
		Log4J.startMethod(logger, "getMessage[blocking]");
		while (messages.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		Log4J.finishMethod(logger, "getMessage[blocking]");
		return messages.remove(0);
	}

	/**
	 * This method add a message to be delivered to the client the message
	 * is pointed to.
	 *
	 * @param msg the message to be delivered.
	 */
	public void addMessage(Message msg) {
		Log4J.startMethod(logger, "addMessage");
		writeManager.write(msg);
		Log4J.finishMethod(logger, "addMessage");
	}

	public boolean isStillRunning() {
		return keepRunning;
	}
}
