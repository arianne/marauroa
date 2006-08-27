/* $Id: ThreadedNetworkClientManager.java,v 1.10 2006/08/27 14:03:11 nhnb Exp $ */
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
package marauroa.client.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.Message;
import marauroa.common.net.MessageFactory;
import marauroa.common.net.NetConst;
import marauroa.common.net.OutputSerializer;

import org.apache.log4j.Logger;

public final class ThreadedNetworkClientManager implements
		NetworkClientManagerInterface {
	final static private int PACKET_SIGNATURE_SIZE = 4;

	final static private int CONTENT_PACKET_SIZE = NetConst.UDP_PACKET_SIZE
			- PACKET_SIGNATURE_SIZE;

	/** the logger instance. */
	private static final Logger logger = Log4J
			.getLogger(ThreadedNetworkClientManager.class);

	private int clientid;

	/** The server socket from where we recieve the packets. */
	private DatagramSocket socket;

	private InetSocketAddress address;

	/** While keepRunning is true, we keep recieving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isfinished;

	private MessageFactory msgFactory;

	private NetworkClientManagerRead readManager;

	private NetworkClientManagerWrite writeManager;

	private Map<Short, PacketContainer> pendingPackets;

	private List<Message> processedMessages;

	/**
	 * Constructor that opens the socket on the marauroa_PORT and start the
	 * thread to recieve new messages from the network.
	 */
	public ThreadedNetworkClientManager(String host, int port)
			throws SocketException {
		Log4J.startMethod(logger, "ThreadedNetworkClientManager");

		clientid = 0;

		/* Create the socket and set a timeout of 1 second */
		address = new InetSocketAddress(host, port);
		if (address.getAddress() == null) {
			throw new SocketException("Unknown Host");
		}
		socket = new DatagramSocket();
		socket.setSoTimeout(TimeoutConf.SOCKET_TIMEOUT);
		socket.setReceiveBufferSize(128 * 1024);

		msgFactory = MessageFactory.getFactory();

		keepRunning = true;
		isfinished = false;

		/*
		 * Because we access the list from several places we create a
		 * synchronized list.
		 */
		processedMessages = Collections
				.synchronizedList(new LinkedList<Message>());
		pendingPackets = Collections
				.synchronizedMap(new LinkedHashMap<Short, PacketContainer>());

		readManager = new NetworkClientManagerRead();
		readManager.start();

		writeManager = new NetworkClientManagerWrite();

		logger.debug("ThreadedNetworkClientManager started successfully");
	}

	/** This method notify the thread to finish it execution */
	public void finish() {
		logger.debug("shutting down NetworkClientManager");
		keepRunning = false;
		while (isfinished == false) {
			Thread.yield();
		}

		socket.close();
		logger.debug("NetworkClientManager is down");
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	private synchronized void newMessageArrived() {
		notifyAll();
	}

	/**
	 * This method returns a Message from the list or block for timeout
	 * milliseconds until a message is available or null if timeout happens.
	 * 
	 * @param timeout
	 *            timeout time in milliseconds
	 * @return a Message or null if timeout happens
	 */
	public synchronized Message getMessage(int timeout) {
		Log4J.startMethod(logger, "getMessage");

		// try
		// {
		// readManager.processPendingPackets();
		// }
		// catch(Exception e)
		// {
		// logger.error("Exception when processing pending packets",e);
		// clear();
		// }

		if (processedMessages.size() == 0) {
			try {
				wait(timeout);
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		Message message = null;
		if (processedMessages.size() > 0) {
			message = readManager.getOldestProcessedMessage();
		}

		Log4J.finishMethod(logger, "getMessage");
		return message;
	}

	private void clear() {
		logger.info("Cleaning pending packets and messages");
		pendingPackets.clear();
		processedMessages.clear();
	}

	/**
	 * This method add a message to be delivered to the client the message is
	 * pointed to.
	 * 
	 * @param msg
	 *            the message to be delivered.
	 */
	public void addMessage(Message msg) {
		Log4J.startMethod(logger, "addMessage");
		writeManager.write(msg);
		Log4J.finishMethod(logger, "addMessage");
	}

	static private class PacketContainer {
		private static final Logger logger = Log4J
				.getLogger(PacketContainer.class);

		public short signature;

		public boolean[] remaining;

		public byte[] content;

		public InetSocketAddress address;

		public Date timestamp;

		public PacketContainer(short signature, int total) {
			this.signature = signature;
			remaining = new boolean[total];
			content = new byte[CONTENT_PACKET_SIZE * total];
			Arrays.fill(remaining, false);
		}

		public void recieved(int pos) {
			if (pos >= remaining.length) {
				logger.error("Confused messages: Recieved packet " + pos
						+ " of (total) " + remaining.length);
			} else {
				remaining[pos] = true;
			}
		}

		public boolean isRecieved(int pos) {
			return !remaining[pos];
		}

		public boolean isComplete() {
			for (boolean test : remaining) {
				if (test == false) {
					return false;
				}
			}

			return true;
		}
	}

	/** The active thread in charge of recieving messages from the network. */
	class NetworkClientManagerRead extends Thread {
		private final Logger logger = Log4J
				.getLogger(NetworkClientManagerRead.class);

		public NetworkClientManagerRead() {
			super("NetworkClientManagerRead");
		}

		private synchronized Message getOldestProcessedMessage() {
			Message choosenMsg = (processedMessages.get(0));
			int smallestTimestamp = choosenMsg.getMessageTimestamp();

			for (Message msg : processedMessages) {
				if (msg.getMessageTimestamp() < smallestTimestamp) {
					choosenMsg = msg;
					smallestTimestamp = msg.getMessageTimestamp();
				}
			}

			processedMessages.remove(choosenMsg);
			return choosenMsg;
		}

		private synchronized void processPendingPackets() throws IOException,
				InvalidVersionException {
			List<Short> packetsToRemove = new LinkedList<Short>();
			try {
				Iterator<Short> it = pendingPackets.keySet().iterator();
				while (it.hasNext()) {
					short value = it.next();
					PacketContainer message = pendingPackets.get(value);

					if (message.isComplete()) {
						// delete the message from queue to prevent loop if it
						// is a bad message
						packetsToRemove.add(value);

						Message msg = msgFactory.getMessage(message.content,
								message.address);

						if (logger.isDebugEnabled()) {
							logger.debug("build message(type=" + msg.getType()
									+ ") from packet(" + message.signature
									+ ") from " + msg.getClientID() + " full ["
									+ msg + "]");
						}

						if (msg.getType() == Message.MessageType.S2C_LOGIN_SENDNONCE) {
							clientid = msg.getClientID();
						}

						processedMessages.add(msg);
						continue;
					}

					if (System.currentTimeMillis()
							- message.timestamp.getTime() > TimeoutConf.CLIENT_MESSAGE_DROPPED_TIMEOUT) {
						logger
								.debug("deleted incompleted message after timedout");
						packetsToRemove.add(value);
					}
				}
			} finally {
				for (Short value : packetsToRemove) {
					pendingPackets.remove(value);
				}
			}
		}

		private synchronized void storePacket(InetSocketAddress address,
				byte[] data) {
			/*
			 * A multipart message. We try to read the rest now. We need to
			 * check on the list if the message exist and it exist we add this
			 * one.
			 */
			byte total = data[0];
			byte position = data[1];
			short signature = (short) (data[2] & 0xFF + ((data[3] & 0xFF) << 8));

			logger.debug("receive" + (total > 1 ? " multipart " : " ")
					+ "message(" + signature + "): " + (position + 1) + " of "
					+ total);
			if (!pendingPackets.containsKey(new Short(signature))) {
				/** This is the first packet */
				PacketContainer message = new PacketContainer(signature, total);

				message.address = address;
				message.timestamp = new Date();

				message.recieved(position);
				System.arraycopy(data, PACKET_SIGNATURE_SIZE, message.content,
						CONTENT_PACKET_SIZE * position, data.length
								- PACKET_SIGNATURE_SIZE);

				pendingPackets.put(new Short(signature), message);
			} else {
				PacketContainer message = pendingPackets
						.get(new Short(signature));

				message.recieved(position);
				if (!message.isRecieved(position)) {
					System.arraycopy(data, PACKET_SIGNATURE_SIZE,
							message.content,
							(NetConst.UDP_PACKET_SIZE - PACKET_SIGNATURE_SIZE)
									* position, data.length
									- PACKET_SIGNATURE_SIZE);
				}
			}

			try {
				processPendingPackets();
			} catch (Exception e) {
				logger.error("Exception when processing pending packets", e);
			}
		}

		/** Method that execute the reading. It runs as a active thread forever. */
		@Override
		public void run() {
			logger.debug("run()");
			while (keepRunning) {
				byte[] buffer = new byte[NetConst.UDP_PACKET_SIZE];
				DatagramPacket packet = new DatagramPacket(buffer,
						buffer.length);

				try {
					socket.receive(packet);
					logger.debug("Received UDP Packet");

					storePacket((InetSocketAddress) packet.getSocketAddress(),
							packet.getData());
					newMessageArrived();
				} catch (java.net.SocketTimeoutException e) {
					/*
					 * We need the thread to check from time to time if user has
					 * requested an exit
					 */
				} catch (IOException e) {
					/* Report the exception */
					logger.error("error while processing udp-packets", e);
				}
			}

			isfinished = true;
			logger.debug("run() finished");
		}
	}

	/** A wrapper class for sending messages to clients */
	class NetworkClientManagerWrite {
		private final Logger logger = Log4J
				.getLogger(NetworkClientManagerWrite.class);

		private int last_signature;

		public NetworkClientManagerWrite() {
			last_signature = 0;
		}

		private byte[] serializeMessage(Message msg) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputSerializer s = new OutputSerializer(out);

			s.write(msg);
			return out.toByteArray();
		}

		final private int PACKET_SIGNATURE_SIZE = 4;

		final private int CONTENT_PACKET_SIZE = NetConst.UDP_PACKET_SIZE
				- PACKET_SIGNATURE_SIZE;

		/** Method that execute the writting */
		public void write(Message msg) {
			Log4J.startMethod(logger, "write");
			try {
				/* TODO: Looks like hardcoded, write it in a better way */
				if (keepRunning) {
					/* We enforce the remote endpoint */
					msg.setAddress(address);
					msg.setClientID(clientid);

					if (msg.getType() == Message.MessageType.C2S_OUTOFSYNC) {
						clear();
					}

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					OutputSerializer s = new OutputSerializer(out);

					logger.debug("send message(" + msg.getType() + ") from "
							+ msg.getClientID());
					s.write(msg);

					byte[] buffer = out.toByteArray();
					DatagramPacket pkt = new DatagramPacket(buffer,
							buffer.length, msg.getAddress());

					socket.send(pkt);
				}
				Log4J.finishMethod(logger, "write");
			} catch (IOException e) {
				/* Report the exception */
				logger.error(
						"error while sending a packet (msg=(" + msg + "))", e);
			}
		}
	}
}
