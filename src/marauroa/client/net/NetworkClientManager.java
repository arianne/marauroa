/* $Id: NetworkClientManager.java,v 1.28 2006/12/18 20:08:13 arianne_rpg Exp $ */
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

/**
 * The NetworkClientManager is in charge of sending and recieving the packages
 * from the network.
 */
@Deprecated
public class NetworkClientManager implements NetworkClientManagerInterface {
	/** the logger instance. */
	private static final Logger logger = Log4J
			.getLogger(NetworkClientManager.class);

	private DatagramSocket socket;

	private InetSocketAddress address;

	private int clientid;

	private MessageFactory msgFactory;

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
			return remaining[pos];
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

	private Map<Short, PacketContainer> pendingPackets;

	private List<Message> processedMessages;

	/**
	 * Constructor that opens the socket on the marauroa_PORT and start the
	 * thread to recieve new messages from the network.
	 */
	public NetworkClientManager(String host, int port) throws SocketException {
		Log4J.startMethod(logger, "NetworkClientManager");
		clientid = 0;
		// Disable java internal dns cache to handle ip-address change of server
		// (TODO: implement automatic reconnect)
		// The normal dns cache by the operarting system which obeys the ttl will
		// continue to work. We only disable the Java dns cache because it ignores
		// ttl and caches everything until the end of the virtual machine.
		java.security.Security.setProperty("networkaddress.cache.ttl" , "0");

		address = new InetSocketAddress(host, port);
		socket = new DatagramSocket();
		socket.setSoTimeout(TimeoutConf.SOCKET_TIMEOUT);
		// This line mades Mac OS X to throw an exception so, let's comment it.
		// It suggest the UDP stack to deliver this packet with maximum
		// performance.
		// socket.setTrafficClass(0x08|0x10);
		socket.setReceiveBufferSize(128 * 1024);

		msgFactory = MessageFactory.getFactory();
		pendingPackets = new LinkedHashMap<Short, PacketContainer>();
		processedMessages = new LinkedList<Message>();
		Log4J.finishMethod(logger, "NetworkClientManager");
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	/** This method notify the thread to finish it execution */
	public void finish() {
		Log4J.startMethod(logger, "finish");
		socket.close();
		Log4J.finishMethod(logger, "finish");
	}

	private Message getOldestProcessedMessage() {
		Message choosenMsg = processedMessages.get(0);
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

	private void processPendingPackets() throws IOException,
			InvalidVersionException {
		for (Iterator<PacketContainer> it = pendingPackets.values().iterator(); it
				.hasNext();) {
			PacketContainer message = it.next();

			if (System.currentTimeMillis() - message.timestamp.getTime() > TimeoutConf.CLIENT_MESSAGE_DROPPED_TIMEOUT) {
				logger.debug("deleted incompleted message after timedout");
				it.remove();
				continue;
			}

			if (message.isComplete()) {
				// delete the message from queue to prevent loop if it is a bad
				// message
				it.remove();

				Message msg = msgFactory.getMessage(message.content,
						message.address);

				if (logger.isDebugEnabled()) {
					logger.debug("build message(type=" + msg.getType()
							+ ") from packet(" + message.signature + ") from "
							+ msg.getClientID() + " full [" + msg + "]");
				}

				if (msg.getType() == Message.MessageType.S2C_LOGIN_SENDNONCE) {
					clientid = msg.getClientID();
				}

				processedMessages.add(msg);
				// NOTE: Break??? Why not run all the array...
				// break;
			}
		}
	}

	public static void main(String[] args) {
		byte[] data = { 2, 3, 5, 1 };

		short used_signature = 15271;

		data[2] = (byte) (used_signature & 0xFF);
		data[3] = (byte) ((used_signature >> 8) & 0xFF);

		System.out.println(data[2]);
		System.out.println(data[3]);

		short signature = (short) (data[2] & 0xFF + ((data[3] & 0xFF) << 8));

		System.out.println(signature);
	}

	final static private int PACKET_SIGNATURE_SIZE = 4;

	final static private int CONTENT_PACKET_SIZE = NetConst.UDP_PACKET_SIZE
			- PACKET_SIGNATURE_SIZE;

	private void storePacket(InetSocketAddress address, byte[] data) {
		/*
		 * A multipart message. We try to read the rest now. We need to check on
		 * the list if the message exist and it exist we add this one.
		 */
		byte total = data[0];
		byte position = data[1];
		short signature = (short) (data[2] & 0xFF + ((data[3] & 0xFF) << 8));

		logger.debug("receive" + (total > 1 ? " multipart " : " ") + "message("
				+ signature + "): " + (position + 1) + " of " + total);
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
			PacketContainer message = pendingPackets.get(new Short(signature));

			message.recieved(position);
			if (message.isRecieved(position)) {
				System
						.arraycopy(
								data,
								PACKET_SIGNATURE_SIZE,
								message.content,
								(NetConst.UDP_PACKET_SIZE - PACKET_SIGNATURE_SIZE)
										* position, data.length
										- PACKET_SIGNATURE_SIZE);
			}
		}
	}

	/**
	 * This method returns a message if it is available or null
	 * 
	 * @return a Message
	 */
	public Message getMessage() throws InvalidVersionException {
		Log4J.startMethod(logger, "getMessage");
		try {
			if (processedMessages.size() > 0) {
				return getOldestProcessedMessage();
			}

			processPendingPackets();
		} catch (InvalidVersionException e) {
			logger.warn("got getMessage with invalid version", e);
			throw e;
		} catch (Exception e) {
			/* Report the exception */
			logger.error("error getting Message", e);
		}

		try {
			byte[] buffer = new byte[NetConst.UDP_PACKET_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			int i = 0;

			/**
			 * We want to avoid this to block the whole client recieving
			 * messages
			 */
			while (i < TimeoutConf.CLIENT_NETWORK_NUM_READ) {
				++i;
				socket.receive(packet);

				byte[] data = packet.getData();
				storePacket((InetSocketAddress) packet.getSocketAddress(), data);
			}
		} catch (java.net.SocketTimeoutException e) {
			/*
			 * We need the thread to check from time to time if user has
			 * requested an exit
			 */
		} catch (IOException e) {
			/* Report the exception */
			logger.error("error getting Message", e);
		} finally {
			Log4J.finishMethod(logger, "getMessage");
		}

		return null;
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
	 *            the message to ve delivered.
	 */
	public synchronized void addMessage(Message msg) {
		Log4J.startMethod(logger, "addMessage");
		try {
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
			DatagramPacket pkt = new DatagramPacket(buffer, buffer.length, msg
					.getAddress());

			socket.send(pkt);
		} catch (IOException e) {
			/* Report the exception */
			logger.error("error while adding Message", e);
		} finally {
			Log4J.finishMethod(logger, "addMessage");
		}
	}

	public Message getMessage(int timeout) {
		try {
			return getMessage();
		} catch (InvalidVersionException e) {
			logger.error(e, e);
			return null;
		}
	}

	public boolean getConnectionState() {
		return true;
	}
}
