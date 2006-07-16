/* $Id: NetworkServerManager.java,v 1.25 2006/07/16 15:08:35 nhnb Exp $ */
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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.Message;
import marauroa.common.net.MessageFactory;
import marauroa.common.net.MessageS2CInvalidMessage;
import marauroa.common.net.NetConst;
import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;


/** The NetworkServerManager is the active entity of the marauroa.net package,
 *  it is in charge of sending and recieving the packages from the network. */
public final class NetworkServerManager implements NetworkServerManagerCallback, Runnable {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(NetworkServerManager.class);

	/** The server socket from where we recieve the packets. */
	private DatagramSocket socket;

	/** While keepRunning is true, we keep recieving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isfinished;

	/** A List of Message objects: List<Message> */
	private List<Message> messages;

	/** MessageFactory */
	private MessageFactory msgFactory;

	private HashMap<InetSocketAddress, Socket> tcpSockets = new HashMap<InetSocketAddress, Socket>();

	private NetworkServerManagerRead udpReader;
	private NetworkServerManagerWrite udpWriter;
	
	private TCPWriter tcpWriter;
	private TCPReader tcpReader;

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
		msgFactory = MessageFactory.getFactory();
		keepRunning = true;
		isfinished = false;

		/* Create the socket and set a timeout of 1 second */
		socket = new DatagramSocket(NetConst.marauroa_PORT);
		socket.setSoTimeout(1000);
		try {
			socket.setTrafficClass(0x08 | 0x10);
		} catch (Exception e) {
			logger.warn("Cannot setTrafficClass " + e);
		}
		socket.setSendBufferSize(1500 * 64);

		Thread tcpListener = new Thread(this, "TCP-Listener");
		tcpListener.setDaemon(true);
		tcpListener.start();
		
		/* Because we access the list from several places we create a synchronized list. */
		messages = Collections.synchronizedList(new LinkedList<Message>());
		stats = Statistics.getStatistics();
		udpReader = new NetworkServerManagerRead(this, socket, stats);
		udpReader.start();
		udpWriter = new NetworkServerManagerWrite(this, socket, stats);
		tcpReader = new TCPReader(this, tcpSockets, stats);
		tcpReader.start();
		tcpWriter = new TCPWriter(this, stats);
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
	private synchronized void newMessageArrived() {
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
	 * Puts a message received by the Networklayer into the list of messages.
	 *
	 * @param data of message that was received
	 * @param inetSocketAddress the address of the client-socket (ip+port)
	 */
	public void receiveMessage(byte[] data, InetSocketAddress inetSocketAddress) throws IOException {
		
		if (!packetValidator.checkBanned(inetSocketAddress.getAddress())) {
			try {
				Message msg = msgFactory.getMessage(data, inetSocketAddress);
				logger.debug("Received message: " + msg.toString());
				messages.add(msg);
				newMessageArrived();
			} catch (InvalidVersionException e) {
				stats.add("Message invalid version", 1);
				MessageS2CInvalidMessage msg = new MessageS2CInvalidMessage(inetSocketAddress, "Invalid client version: Update client");
				sendMessage(msg);
			}
		} else {
			logger.debug("UDP Packet discarded - client(" + inetSocketAddress + ") is banned.");
		}
		
	}

	/**
	 * This method add a message to be delivered to the client the message
	 * is pointed to.
	 *
	 * @param msg the message to be delivered.
	 */
	public void sendMessage(Message msg) {
		Log4J.startMethod(logger, "addMessage");
		Socket socket = tcpSockets.get(msg.getAddress());
		if (socket != null) {
			tcpWriter.write(msg, socket);
		} else {
			udpWriter.write(msg);
		}
		Log4J.finishMethod(logger, "addMessage");
	}

	public boolean isStillRunning() {
		return keepRunning;
	}
	
	public void finishedReadThread() {
		isfinished = true;
	}

	public void run() {
		try {
			ServerSocket tcpSocket = new ServerSocket(NetConst.marauroa_PORT);
			socket.setSoTimeout(1000);
			try {
				socket.setTrafficClass(0x08 | 0x10);
			} catch (Exception e) {
				logger.warn("Cannot setTrafficClass " + e);
			}
			while (keepRunning) {
				Socket socket = tcpSocket.accept();
				InetSocketAddress inetSocketAddress = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
				tcpSockets.put(inetSocketAddress, socket);
				
			}
			tcpSocket.close();
			
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	public void disconnectClient(InetSocketAddress inetSocketAddress) {
		Socket socket = tcpSockets.get(inetSocketAddress);
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				logger.warn(e, e);
			}
			tcpSockets.remove(inetSocketAddress);
		}
	}
}
