/* $Id: NIONetworkServerManager.java,v 1.12 2007/02/09 16:13:28 arianne_rpg Exp $ */
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
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Log4J;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.NetConst;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CInvalidMessage;
import marauroa.server.game.Statistics;
import marauroa.server.net.IDisconnectedListener;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.validator.ConnectionValidator;

import org.apache.log4j.Logger;


/**
 * This is the implementation of a worker that send messages, recieve it, ...
 * This class also handles validation of connections and disconnections events
 * @author miguel
 *
 */
public class NIONetworkServerManager extends Thread implements IWorker, INetworkServerManager {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(NIONetworkServerManager.class);
	
	/** We store the server for sending stuff. */
	private NioServer server;

	/** While keepRunning is true, we keep recieving messages */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isFinished;

	/** A List of Message objects: List<Message> */
	private BlockingQueue<Message> messages;

	/** Statistics */
	private Statistics stats;

	/** checkes if the ip-address is banned */
	private ConnectionValidator connectionValidator;

	private BlockingQueue<DataEvent> queue;
	
	private Encoder encoder;
	private Decoder decoder;

	
	public NIONetworkServerManager() throws IOException {
		/* init the packet validater (which can now only check if the address is banned)*/
		connectionValidator = new ConnectionValidator();
		keepRunning = true;
		isFinished = false;
		
		encoder=Encoder.get();
		decoder=Decoder.get();

		/* Because we access the list from several places we create a synchronized list. */
		messages = new LinkedBlockingQueue<Message>();
		stats = Statistics.getStatistics();
		queue = new LinkedBlockingQueue<DataEvent>();
		
		logger.debug("NetworkServerManager started successfully");
		
		server=new NioServer(null, NetConst.marauroa_PORT, this);
		server.start();
	}

	public void setServer(NioServer server) {
		this.server=server;		
	}

	/** 
	 * This method notify the thread to finish it execution
	 */
	public void finish() {
		logger.info("shutting down NetworkServerManager");
		keepRunning = false;
		
		server.finish();
		interrupt();
		
		while (isFinished == false) {
			Thread.yield();
		}		

		logger.info("NetworkServerManager is down");
	}

	/** 
	 * This method returns a Message from the list or block for timeout milliseconds
	 * until a message is available or null if timeout happens.
	 *
	 * @param timeout timeout time in milliseconds
	 * @return a Message or null if timeout happens
	 */
	public synchronized Message getMessage(int timeout) {
		try {
			return messages.poll(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			/* If interrupted while waiting we just return null */
			return null;
		}
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

	/** We check that this socket is not banned.
	 *  We do it just on connect so we save lots of queries. */
	public void onConnect(SocketChannel channel) {
		Socket socket=channel.socket();
		
		if(connectionValidator.checkBanned(socket)) {
			logger.info("Reject connect from banned IP: "+socket.getInetAddress());
			
			/* If address is banned, just close connection */
			try {
				server.close(channel);
			} catch (IOException e) {
				/* I don't think I want to listen to complains... */
				logger.info(e);
			}
		}
	}

	public void onData(NioServer server, SocketChannel channel, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		try {
			queue.put(new DataEvent(channel, dataCopy));
		} catch (InterruptedException e) {
			/* This is never going to happen */
		}
	}

	/**
	 * This method add a message to be delivered to the client the message
	 * is pointed to.
	 *
	 * @param msg the message to be delivered.
	 * @throws IOException 
	 */
	public void sendMessage(Message msg) {
		try {
			byte[] data = encoder.encode(msg);
			server.send(msg.getSocketChannel(), data);
		} catch (IOException e) {
			e.printStackTrace();
			/** I am not interested in the exception. NioServer will detect this and close connection  */
		}
	}

	public void disconnectClient(SocketChannel channel) {
			try {
			server.close(channel);
			} catch (Exception e) {
				logger.error("Unable to disconnect a client "+channel.socket(),e);
			}
		
	}

	public ConnectionValidator getValidator() {
		return connectionValidator;
	}
	
	public void registerDisconnectedListener(IDisconnectedListener listener) {
		server.registerDisconnectedListener(listener);
	}	
	
	@Override
	public void run() {
		try {
			while(keepRunning) {
				DataEvent event=queue.take();

				try {
					Message msg = decoder.decode(event.channel, event.data);
					if(msg!=null) {
						messages.add(msg);
					}
				} catch (InvalidVersionException e) {
					stats.add("Message invalid version", 1);
					MessageS2CInvalidMessage invMsg = new MessageS2CInvalidMessage(event.channel, "Invalid client version: Update client");
					sendMessage(invMsg);
				} catch (IOException e) {
					logger.warn("IOException while building message.",e);
				}
			}			
		} catch (InterruptedException e) {
			keepRunning=false;
		}
		
		isFinished=true;
	}
}
