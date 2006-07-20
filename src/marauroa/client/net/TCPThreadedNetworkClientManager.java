// E X P E R I M E N T A L    TCP    C L I E N T

/* $Id: TCPThreadedNetworkClientManager.java,v 1.7 2006/07/20 21:36:54 nhnb Exp $ */
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.Utility;
import marauroa.common.net.Message;
import marauroa.common.net.MessageFactory;
import marauroa.common.net.OutputSerializer;

import org.apache.log4j.Logger;


public final class TCPThreadedNetworkClientManager implements NetworkClientManagerInterface {
	final static private int PACKET_SIGNATURE_SIZE = 4;

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(TCPThreadedNetworkClientManager.class);
	private int clientid;

	/** The server socket from where we recieve the packets. */
	private Socket socket;
	private InetSocketAddress address;

	/** While keepRunning is true, we keep recieving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isfinished;
	private MessageFactory msgFactory;
	private NetworkClientManagerRead readManager;
	private NetworkClientManagerWrite writeManager;
	private List<Message> processedMessages;

	/** Constructor that opens the socket on the marauroa_PORT and start the thread
	 to recieve new messages from the network. */
	public TCPThreadedNetworkClientManager(String host, int port) throws SocketException {
		Log4J.startMethod(logger, "ThreadedNetworkClientManager");

		try {
			clientid = 0;
	
			/* Create the socket and set a timeout of 1 second */
			address = new InetSocketAddress(host, port);
			socket = new Socket(address.getAddress(), port);
			socket.setTcpNoDelay(true); // disable Nagle's algorithm
			socket.setReceiveBufferSize(128 * 1024);
	
			msgFactory = MessageFactory.getFactory();
	
			keepRunning = true;
			isfinished = false;
	
			/* Because we access the list from several places we create a synchronized list. */
			processedMessages = Collections.synchronizedList(new LinkedList<Message>());
	
			readManager = new NetworkClientManagerRead();
			readManager.start();
	
			writeManager = new NetworkClientManagerWrite();
		} catch (IOException e) {
			// TODO: rewrite this in a cleaner way. This is a simple hack
			// to be interface compatible because i know only Stendhal.
			throw new SocketException(e.getMessage());
		}

		logger.debug("ThreadedNetworkClientManager started successfully");
	}

	/* (non-Javadoc)
	 * @see marauroa.client.net.NetworkClientManagerInterface#finish()
	 */
	public void finish() {
		logger.debug("shutting down NetworkClientManager");
		keepRunning = false;
		while (isfinished == false) {
			Thread.yield();
		}

		try {
			socket.close();
		} catch (IOException e) {
			logger.error(e, e);
		}
		logger.debug("NetworkClientManager is down");
	}

	/* (non-Javadoc)
	 * @see marauroa.client.net.NetworkClientManagerInterface#getAddress()
	 */
	public InetSocketAddress getAddress() {
		return address;
	}

	private synchronized void newMessageArrived() {
		notifyAll();
	}

	/* (non-Javadoc)
	 * @see marauroa.client.net.NetworkClientManagerInterface#getMessage(int)
	 */
	public synchronized Message getMessage(int timeout) {
		Log4J.startMethod(logger, "getMessage");

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
		processedMessages.clear();
	}

	/* (non-Javadoc)
	 * @see marauroa.client.net.NetworkClientManagerInterface#addMessage(marauroa.common.net.Message)
	 */
	public void addMessage(Message msg) {
		Log4J.startMethod(logger, "addMessage");
		writeManager.write(msg);
		Log4J.finishMethod(logger, "addMessage");
	}

	/** The active thread in charge of recieving messages from the network. */
	class NetworkClientManagerRead extends Thread {
		private final Logger logger = Log4J.getLogger(NetworkClientManagerRead.class);
		private InputStream is = null;

		public NetworkClientManagerRead() {
			super("NetworkClientManagerRead");
			try {
				is = socket.getInputStream();
			} catch (IOException e) {
				logger.error(e, e);
			}
		}

		private synchronized Message getOldestProcessedMessage() {
			Message choosenMsg = processedMessages.remove(0);
			return choosenMsg;
		}

		private synchronized void storeMessage(InetSocketAddress address, byte[] data) {
			short signature = (short) (data[2] & 0xFF + ((data[3] & 0xFF) << 8));

			logger.debug("receive message(" + signature + ")");

			try {
				Message msg = msgFactory.getMessage(data, address, PACKET_SIGNATURE_SIZE);

				if (logger.isDebugEnabled()) {
					logger.debug("build message(type=" + msg.getType() + ") from packet(" + signature + ") from " + msg.getClientID() + " full [" + msg + "]");
				}

				if (msg.getType() == Message.MessageType.S2C_LOGIN_SENDNONCE) {
					clientid = msg.getClientID();
				}

				processedMessages.add(msg);
			} catch (Exception e) {
				logger.error("Exception when processing pending packets", e);
			}
		}

		/** Method that execute the reading. It runs as a active thread forever. */
		public void run() {
			logger.debug("run()");
			while (keepRunning) {
				try {
					byte[] sizebuffer = new byte[4];
					is.read(sizebuffer);
					int size = (sizebuffer[0] & 0xFF)
						+ ((sizebuffer[1] & 0xFF) << 8)
						+ ((sizebuffer[2] & 0xFF) << 16)
						+ ((sizebuffer[3] & 0xFF) << 24);

					byte[] buffer = new byte[size];

					// read until everything is received. We have to call read
					// in a loop because the data may be split accross several
					// packets.
					long startTime = System.currentTimeMillis();
					int start = 0;
					int read = 0;
					long waittime = 10;
					int counter = 0;
					do {
						start = read;
						read = is.read(buffer, start, size - start);
						if (read < 0) {
							logger.error("Read is negative counter=" + counter + " start=" +start + " read=" + read + " size=" + size + " time=" + (System.currentTimeMillis() - startTime));
							read = 0;
							waittime = 100;
						}
						if (System.currentTimeMillis() - 2000 > startTime) {
							logger.error("Waiting to long for follow-packets: counter=" + counter + " start=" +start + " read=" + read + " size=" + size + " time=" + (System.currentTimeMillis() - startTime));
							waittime = 1000;
						}
						try {
							Thread.sleep(waittime);
						} catch (InterruptedException e) {
							logger.error(e, e);
						}
						counter++;
					} while (start + read < size);
					
					//logger.debug("size: " + size + "\r\n" + Utility.dumpByteArray(buffer));
					
					logger.debug("Received TCP Packet");

					storeMessage(address, buffer);
					newMessageArrived();
				} catch (java.net.SocketTimeoutException e) {
					/* We need the thread to check from time to time if user has requested
					 * an exit */
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
		private final Logger logger = Log4J.getLogger(NetworkClientManagerWrite.class);
		private OutputStream os = null;
		
		public NetworkClientManagerWrite() {
			try {
				os = socket.getOutputStream();
			} catch (IOException e) {
				logger.error(e, e);
			}
		}

		private byte[] serializeMessage(Message msg) throws IOException {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputSerializer s = new OutputSerializer(out);
			logger.debug("send message(" + msg.getType() + ") from " + msg.getClientID());

			s.write(msg);
			return out.toByteArray();
		}

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

					// write size and then the message
					byte[] buffer = serializeMessage(msg);
					byte[] sizebuffer = new byte[4];
					int size = buffer.length;
					sizebuffer[0] = (byte) (size & 255);
					sizebuffer[1] = (byte) ((size >>  8) & 255);
					sizebuffer[2] = (byte) ((size >> 16) & 255);
					sizebuffer[3] = (byte) ((size >> 24) & 255);
					os.write(sizebuffer);
					os.write(buffer);
				}
				Log4J.finishMethod(logger, "write");
			} catch (IOException e) {
				/* Report the exception */
				logger.error("error while sending a packet (msg=(" + msg + "))", e);
			}
		}
	}
}
