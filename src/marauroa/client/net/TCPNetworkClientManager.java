/* $Id: TCPNetworkClientManager.java,v 1.29 2010/02/27 16:49:54 nhnb Exp $ */
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Log4J;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;

/**
 * This is the basic implementation of a TCP network manager.
 * 
 * @author hendrik
 */
public class TCPNetworkClientManager implements INetworkClientManagerInterface {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J
	        .getLogger(TCPNetworkClientManager.class);

	/**
	 * Server will assign us a clientid, so we store it so that we remind it for
	 * the messages we send to server. Client id is unique per session.
	 */
	private int clientid;

	/** The server socket from where we receive the packets. */
	private Socket socket;

	/** The TCP/IP address where the server is running. */
	private InetSocketAddress address;

	/** While keepRunning is true, we keep receiving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isfinished;

	/** A instance of the thread that read stuff from network and build messages. */
	private NetworkClientManagerRead readManager;

	/** A instance of the thread that write messages to server. */
	private NetworkClientManagerWrite writeManager;

	/**
	 * List of already processed messages what the client will get using
	 * getMessage method.
	 */
	private BlockingQueue<Message> processedMessages;

	/**
	 * An instance to the encoder class that will build a stream of bytes from a
	 * Message.
	 */
	private Encoder encoder;

	/**
	 * An instance of the decoder class that will recreate a message from a
	 * stream of bytes.
	 */
	private Decoder decoder;

	/**
	 * This is true as long as we are connected to server.
	 */
	private boolean connected = false;

	/**
	 * Constructor that opens the socket on the marauroa_PORT and start the
	 * thread to receive new messages from the network.
	 * 
	 * @param host
	 *            the host where we connect to.
	 * @param port
	 *            the port of the server where we connect to.
	 * @throws IOException
	 */
	public TCPNetworkClientManager(String host, int port) throws IOException {
		this(Proxy.NO_PROXY, new InetSocketAddress(host, port));
	}
	/**
	 * Constructor that opens the socket on the marauroa_PORT and start the
	 * thread to receive new messages from the network.
	 *
	 * @param proxy proxy server and protocol to use
	 * @param serverAddress the host and port where we connect to.
	 * @throws IOException
	 */
	public TCPNetworkClientManager(Proxy proxy, InetSocketAddress serverAddress) throws IOException {
		clientid = Message.CLIENTID_INVALID;
		this.address = serverAddress;

		// check name (dns lookup)
		if (address.getAddress() == null) {
			throw new IOException("Unknown Host");
		}

		/* Create the socket */
		if (proxy.type() == Proxy.Type.HTTP) {
			socket = new HTTPConnectSocket(proxy.address());
		} else {
			socket = new Socket(proxy);
		}
		socket.connect(address);
		socket.setTcpNoDelay(true); // disable Nagle's algorithm
		socket.setReceiveBufferSize(128 * 1024);

		keepRunning = true;
		isfinished = false;
		connected = true;

		encoder = Encoder.get();
		decoder = Decoder.get();

		/*
		 * Because we access the list from several places we create a
		 * synchronized list.
		 */
		processedMessages = new LinkedBlockingQueue<Message>();

		readManager = new NetworkClientManagerRead();
		readManager.start();

		writeManager = new NetworkClientManagerWrite();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see marauroa.client.net.NetworkClientManagerInterface#finish()
	 */
	public void finish() {
		logger.debug("shutting down NetworkClientManager");
		keepRunning = false;
		connected = false;

		try {
			socket.close();
		} catch (IOException e) {
			// We don't care about the error.
		}

		readManager.interrupt();

		while (isfinished == false) {
			Thread.yield();
		}

		logger.debug("NetworkClientManager is down");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see marauroa.client.net.NetworkClientManagerInterface#getAddress()
	 */
	public InetSocketAddress getAddress() {
		return address;
	}

	private boolean shouldThrowException;

	private InvalidVersionException storedException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see marauroa.client.net.NetworkClientManagerInterface#getMessage(int)
	 */
	public synchronized Message getMessage(int timeout) throws InvalidVersionException {
		/*
		 * As read of message is done in a different thread we lost the
		 * exception information, so we store it in a variable and throw them
		 * now.
		 */
		if (shouldThrowException) {
			shouldThrowException = false;
			throw storedException;
		}

		try {
			return processedMessages.poll(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			/* If interrupted while waiting we just return null */
			return null;
		}
	}
	
	
	
	/**
	 * gets all messages received so far and removes them from the queue.
	 * 
	 * 
	 * @return the messages received
	 */
	public synchronized Collection<Message> getMessages() {
		Collection<Message> col = new LinkedList<Message>();
		 processedMessages.drainTo(col);
		 return col;
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see marauroa.client.net.NetworkClientManagerInterface#addMessage(marauroa.common.net.Message)
	 */
	public void addMessage(Message msg) {
		connected = writeManager.write(msg);
		logger.debug(connected?"connected":"disconnected");
	}

	/**
	 * Returns true if the connection is "connected" to server or false
	 * otherwise.
	 * 
	 * @return true if socket is connected.
	 */
	public boolean getConnectionState() {
		return connected && !socket.isClosed();
	}

	/**
	 * The active thread in charge of recieving messages from the network.
	 */
	class NetworkClientManagerRead extends Thread {

		private final marauroa.common.Logger logger = Log4J
		        .getLogger(NetworkClientManagerRead.class);

		/** We handle the data connection with the socket's input stream */
		private InputStream is = null;

		/**
		 * Constructor. This constructor doesn't start the thread.
		 */
		public NetworkClientManagerRead() {
			super("NetworkClientManagerRead");
			try {
				is = socket.getInputStream();
			} catch (IOException e) {
				logger.error(e, e);
			}
		}

		/**
		 * Decode a stream of bytes into a Message.
		 * 
		 * @param address
		 *            address the message comes from.
		 * @param data
		 *            data that represent the serialized message
		 * @throws IOException
		 */
		private synchronized void storeMessage(InetSocketAddress address, byte[] data)
		        throws IOException {
			try {
				List<Message> messages = decoder.decode(null, data);

				if (messages != null) {
					for (Message msg : messages) {
						/*
						 * If logger is enable, print the message so it shows useful
						 * debugging information.
						 */
						if (logger.isDebugEnabled()) {
							logger.debug("build message(type=" + msg.getType() + ") from "
							        + msg.getClientID() + " full [" + msg + "]");
						}

						// Once server assign us a clientid, store it for future
						// messages.
						if (msg.getType() == Message.MessageType.S2C_LOGIN_SENDNONCE) {
							clientid = msg.getClientID();
						}

						processedMessages.add(msg);
					}
				}
			} catch (InvalidVersionException e) {
				shouldThrowException = true;
				storedException = e;
				logger.error("Exception when processing pending packets", e);
			}

		}

		/**
		 * Keep reading from TCP stack until the whole Message has been read.
		 * 
		 * @return a byte stream representing the message or null if client has
		 *         requested to exit.
		 * @throws IOException
		 *             if there is any problem reading.
		 */
		private byte[] readByteStream() throws IOException {
			byte[] sizebuffer = new byte[4];

			while (is.available() < 4) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					logger.error(e, e);
				}				
			}
			
			if (is.read(sizebuffer) < 0) {
				isfinished = true;
				return null;
			}

			int size = (sizebuffer[0] & 0xFF) + ((sizebuffer[1] & 0xFF) << 8)
			        + ((sizebuffer[2] & 0xFF) << 16) + ((sizebuffer[3] & 0xFF) << 24);

			byte[] buffer = new byte[size];
			System.arraycopy(sizebuffer, 0, buffer, 0, 4);

			// read until everything is received. We have to call read
			// in a loop because the data may be split across several
			// packets.
			long startTime = System.currentTimeMillis();
			int start = 4;
			int read = 0;
			long waittime = 10;
			int counter = 0;
			do {
				start = start + read;
				read = is.read(buffer, start, size - start);
				if (read < 0) {
					isfinished = true;
					return null;
				}

				if (System.currentTimeMillis() - 2000 > startTime) {
					waittime = 1000;
				}
				try {
					Thread.sleep(waittime);
				} catch (InterruptedException e) {
					logger.error(e, e);
				}
				counter++;
			} while (start + read < size);

			logger.debug("Received Marauroa Packet");

			return buffer;
		}

		/** Method that execute the reading. It runs as a active thread forever. */
		@Override
		public void run() {
			logger.debug("run()");

			while (keepRunning) {
				try {
					byte[] buffer = readByteStream();
					if (buffer == null) {
						/* User has requested exit */
						return;
					}

					storeMessage(address, buffer);
				} catch (IOException e) {
					// TODO: Notify upper layers about connection broken
					/* Report the exception */
					logger.warn("Connection broken.");
					connected=false;
					keepRunning = false;
				}
			}

			isfinished = true;
			logger.warn("run() finished");
		}
	}

	/** A wrapper class for sending messages to clients */
	class NetworkClientManagerWrite {

		/** the logger instance. */
		private final marauroa.common.Logger logger = Log4J
		        .getLogger(NetworkClientManagerWrite.class);

		/** An output stream that represents the socket. */
		private OutputStream os = null;
		
		/**
		 * Constructor
		 */
		public NetworkClientManagerWrite() {
			try {
				os = socket.getOutputStream();
			} catch (IOException e) {
				logger.error(e, e);
			}
		}

		/**
		 * Method that execute the writting
		 * 
		 * @param msg
		 *            the message to send to server.
		 * @return true, if the message was sent successfully 
		 */
		public synchronized boolean write(Message msg) {
			try {
				if (keepRunning) {
					/* We enforce the remote endpoint */
					msg.setSocketChannel(null);
					msg.setClientID(clientid);

					/*
					 * If we are sending a out of sync message, clear the queue
					 * of messages.
					 */
					if (msg.getType() == Message.MessageType.C2S_OUTOFSYNC) {
						processedMessages.clear();
					}

					/*
					 * If logger is enable, print the message so it shows useful
					 * debugging information.
					 */
					if (logger.isDebugEnabled()) {
						logger.debug("build message(type=" + msg.getType() + ") from "
						        + msg.getClientID() + " full [" + msg + "]");
					}

					os.write(encoder.encode(msg));
					return true;
				} else {
					logger.warn("Write requested not to keeprunning");
					connected=false;
					return false;
				}
			} catch (IOException e) {
				/* Report the exception */
				logger.error("error while sending a packet (msg=(" + msg + "))", e);
				return false;
			}
		}
	}
}
