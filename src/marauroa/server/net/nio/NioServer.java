/* $Id: NioServer.java,v 1.29 2010/06/12 15:09:47 nhnb Exp $ */
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.server.net.IDisconnectedListener;

/**
 * This class is the basic schema for a nio server. It works in a pattern of
 * master/slave.
 * 
 * @author miguel
 * 
 */
class NioServer extends Thread {
	private static final int BACKLOG_WARNING_SIZE = 10;

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(NioServer.class);

	/** The host:port combination to listen on */
	private InetAddress hostAddress;

	private int port;

	/** While keepRunning is true, we keep receiving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isFinished;

	/** The channel on which we'll accept connections */
	private ServerSocketChannel serverChannel;

	/** The selector we'll be monitoring */
	private Selector selector;

	/** The buffer into which we'll read data when it's available */
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	/**
	 * This is the slave associated with this master. As it is a simple thread,
	 * we only need one slave.
	 */
	private IWorker worker;

	/** A list of PendingChange instances */
	private List<ChangeRequest> pendingChanges = new LinkedList<ChangeRequest>();

	private List<ChangeRequest> pendingClosed;

	/** Maps a SocketChannel to a list of ByteBuffer instances */
	private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();

	/** A list of the listeners to the onDisconnect event. */
	private List<IDisconnectedListener> listeners;

	public NioServer(InetAddress hostAddress, int port, IWorker worker) throws IOException {
		super("NioServer");

		keepRunning = true;
		isFinished = false;

		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.worker = worker;
		this.worker.setServer(this);

		pendingClosed = new LinkedList<ChangeRequest>();
		listeners = new LinkedList<IDisconnectedListener>();
	}

	/**
	 * This method closes a channel. It also notify any listener about the
	 * event.
	 * 
	 * @param channel
	 *            the channel to close.
	 */
	public void close(SocketChannel channel) {
		for (IDisconnectedListener listener : listeners) {
			listener.onDisconnect(channel);
		}

		/*
		 * We ask the server to close the channel
		 */	
		synchronized (this.pendingClosed) {
		  pendingClosed.add(new ChangeRequest(channel, ChangeRequest.CLOSE, 0));
		}
		
		/*
		 * Wake up to make the closure effective.
		 */
		selector.wakeup();
	}

	/**
	 * This method is used to send data on a socket.
	 * 
	 * @param socket
	 *            the socketchannel to use.
	 * @param data
	 *            a byte array of data to send
	 */
	public void send(SocketChannel socket, byte[] data) {
		synchronized (this.pendingChanges) {
			// Indicate we want the interest ops set changed
			this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS,
			        SelectionKey.OP_WRITE));

			// And queue the data we want written
			synchronized (this.pendingData) {
				List<ByteBuffer> queue = this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList<ByteBuffer>();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
				if (queue.size() > BACKLOG_WARNING_SIZE) {
					logger.debug(socket + ": " + queue.size());
				}
			}
		}

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	/**
	 * Finish this thread in a correct way.
	 */
	public void finish() {
		keepRunning = false;

		selector.wakeup();

		while (isFinished == false) {
			Thread.yield();
		}

		try {
			selector.close();
		} catch (IOException e) {
			// We really don't care about the exception.
		}
	}

	@Override
	public void run() {
		while (keepRunning) {
			try {
				// Process any pending changes
				synchronized (this.pendingChanges) {
					Iterator<?> changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						if (change.socket.isConnected()) {
							switch (change.type) {
								case ChangeRequest.CHANGEOPS:
									SelectionKey key = change.socket.keyFor(this.selector);
									if (key.isValid()) {
										key.interestOps(change.ops);
									}
							}
						}
					}
					this.pendingChanges.clear();
				}

				synchronized (this.pendingClosed) {
					Iterator<?> it = pendingClosed.iterator();
					while (it.hasNext()) {
						ChangeRequest change = (ChangeRequest) it.next();
						if (change.socket.isConnected()) {
							switch (change.type) {
								case ChangeRequest.CLOSE:
									try {
										/*
										 * Force data to be sent if there is data
										 * waiting.
										 */
										if (pendingData.containsKey(change.socket)) {
											SelectionKey key = change.socket.keyFor(selector);
											if (key.isValid()) {
												write(key);
											}
										}

										/*
										 * Close the socket
										 */
										change.socket.close();
									} catch (Exception e) {
										logger.info("Exception happened when closing socket", e);
									}
									break;
							}
						} else {
							logger.info("Closing a not connected socket");
						}
					}
					pendingClosed.clear();
				}

				// Wait for an event one of the registered channels
				this.selector.select();

				// Iterate over the set of keys for which events are available
				Iterator<?> selectedKeys = this.selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();

					if (!key.isValid()) {
						continue;
					}

					// Check what event is available and deal with it
					if (key.isAcceptable()) {
						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (IOException e) {
				logger.error("Error on NIOServer", e);
			} catch (RuntimeException e) {
				logger.error("Error on NIOServer", e);
			}
		}

		isFinished = true;
	}

	private void accept(SelectionKey key) throws IOException {
		// For an accept to be pending the channel must be a server socket
		// channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(this.selector, SelectionKey.OP_READ);

		worker.onConnect(socketChannel);
	}

	private void read(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		this.readBuffer.clear();

		// Attempt to read off the channel
		int numRead;
		try {
			numRead = socketChannel.read(this.readBuffer);
		} catch (IOException e) {
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			logger.debug("Remote closed connnection", e);
			key.cancel();

			close(socketChannel);

			return;
		}

		if (numRead == -1) {
			// Remote entity shut the socket down cleanly. Do the
			// same from our end and cancel the channel.
			logger.debug("Remote closed connnection cleanly");
			close((SocketChannel) key.channel());

			key.cancel();
			return;
		}

		// Hand the data off to our worker thread
		this.worker.onData(this, socketChannel, this.readBuffer.array(), numRead);
	}

	private void write(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List<ByteBuffer> queue = this.pendingData.get(socketChannel);

			try {
				// Write until there's not more data ...
				while (!queue.isEmpty()) {
					ByteBuffer buf = queue.get(0);
					socketChannel.write(buf);

					if (buf.remaining() > 0) {
						// ... or the socket's buffer fills up
						break;
					}
					queue.remove(0);
				}

				if (queue.isEmpty()) {
					// We wrote away all data, so we're no longer interested
					// in writing on this socket. Switch back to waiting for
					// data.
					key.interestOps(SelectionKey.OP_READ);
				}
			} catch (IOException e) {
				// The remote forcibly closed the connection, cancel
				// the selection key and close the channel.
				logger.debug("Remote closed connnection", e);
				queue.clear();
				key.cancel();

				close(socketChannel);

				return;
			}
		}
	}

	private Selector initSelector() throws IOException {
		// Create a new selector
		Selector socketSelector = SelectorProvider.provider().openSelector();

		// Create a new non-blocking server socket channel
		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
		serverChannel.socket().bind(isa);
		serverChannel.socket().setPerformancePreferences(0, 2, 1);

		// Register the server socket channel, indicating an interest in
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	/** 
	 * Register a listener to notify about disconnected events
	 *
	 * @param listener listener to add
	 */
	public void registerDisconnectedListener(IDisconnectedListener listener) {
		this.listeners.add(listener);
	}
}
