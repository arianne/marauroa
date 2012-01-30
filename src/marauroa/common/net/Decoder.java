/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import marauroa.common.Log4J;
import marauroa.common.net.message.Message;

/**
 * This class decodes a stream of bytes and builds a Marauroa message with it.
 * Decoder follows singleton pattern.
 *
 * @author miguel, hendrik
 */
public class Decoder {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(Decoder.class);

	/**
	 * This class handles incomplete messages.
	 */
	class MessageParts {

		public Vector<byte[]> parts;

		public MessageParts() {
			parts = new Vector<byte[]>();
		}

		/**
		 * Adds a new part to complete the message
		 *
		 * @param data data to add
		 */
		public void add(byte[] data) {
			parts.add(data);
		}
		
		public boolean isEmpty() {
			return parts.isEmpty();
		}

		/**
		 * Try to build the message for the channel using the existing parts or
		 * return null if it is not completed yet.
		 *
		 * @param channel SocketChannel to read from
		 * @return Message read message
		 * @throws IOException in case of an input / output error
		 * @throws InvalidVersionException in case the protocol version is not supported
		 */
		public Message build(SocketChannel channel) throws IOException, InvalidVersionException {
			int length = 0;
			for (byte[] p : parts) {
				length += p.length;
			}

			// the first 4 bytes are the size
			if (length < 4) {
				return null;
			}
			
			int size = readSizeOfMessage();
			
			/*
			 * If length is bigger than size that means that two messages on
			 * a row... so we need to run until the end of the first one.
			 */
			if (length < size) {
				/*
				 * Still missing parts, let's wait
				 */
				return null;
			}

			byte[] data = new byte[size];
			
			int remaining=size;

			int offset = 0;
			Iterator<byte[]> it=parts.iterator();
			while(it.hasNext()) {
				byte[] p=it.next();
				
				if(remaining-p.length<0) {
					/*
					 * This part completes first message and has stuff from the second one.
					 */
					System.arraycopy(p, 0, data, offset, remaining);
					
					/*
					 * Copy the rest of the array to a new array.					  
					 */
					byte[] rest = new byte[p.length-remaining];
					System.arraycopy(p, remaining, rest, 0, p.length-remaining);
					parts.set(0, rest);

					/*
					 * Stop iterating and process the currently complete message.
					 */
					break;
				} else {
					System.arraycopy(p, 0, data, offset, p.length);
					offset += p.length;
					remaining -=p.length;
					
					it.remove();
				}
			}

			Message msg = msgFactory.getMessage(data, channel, 4);
			return msg;
		}

		/**
		 * reads the size of the message. Important: It expects at least 4 bytes in the buffer.
		 *
		 * @return size
		 */
		private int readSizeOfMessage() {
			byte[] size = new byte[4];
			int offset = 0;

			loops:
			for (byte[] part : parts) {
				for (int i = 0; i < part.length; i++) {
					size[offset] = part[i];

					// if we have read four bytes, break
					if (offset == 3) {
						break loops;
					}
					offset++;
				}
			}

			return getSizeOfMessage(size);
		}
	}

	/** We map each channel with the sent content */
	private Map<SocketChannel, MessageParts> content;

	/** MessageFactory */
	private MessageFactory msgFactory;

	/** singleton instance */
	private static Decoder instance;

	/**
	 * Returns an unique instance of decoder
	 *
	 * @return an unique instance of decoder
	 */
	public static Decoder get() {
		if (instance == null) {
			instance = new Decoder();
		}

		return instance;
	}

	/**
	 * Constructor
	 *
	 */
	private Decoder() {
		content = new HashMap<SocketChannel, MessageParts>();
		msgFactory = MessageFactory.getFactory();
	}

	/**
	 * Removes any pending data from a connection
	 *
	 * @param channel
	 *            the channel to clear data from.
	 */
	public void clear(SocketChannel channel) {
		content.remove(channel);
	}
	
	static int getSizeOfMessage(byte[] data) {
		return (data[0] & 0xFF) + ((data[1] & 0xFF) << 8) + ((data[2] & 0xFF) << 16)
        + ((data[3] & 0xFF) << 24);
	}

	/**
	 * Decodes a message from a stream of bytes received from channel
	 *
	 * @param channel
	 *            the socket from where data was received
	 * @param data
	 *            the data received
	 * @return a message or null if it was not possible
	 * @throws IOException
	 *             if there is a problem building the message
	 * @throws InvalidVersionException
	 *             if the message version mismatch the expected version
	 */
	public List<Message> decode(SocketChannel channel, byte[] data) throws IOException,
	        InvalidVersionException {
		MessageParts buffers = content.get(channel);

		if (buffers == null) {
			/* First part of the message */
			/*
			 * We need to be *sure* that 4 bytes are at least to
			 * be received...
			 */
			if(data.length<4) {
				throw new IOException("Message is too short. Missing mandatory fields.");
			}
			
			int size = getSizeOfMessage(data);

			if(size<0) {
				throw new IOException("Message size is negative. Message ignored.");
			}

			if (data.length == size) {
				/* If we have the full data build the message */
				Message msg = msgFactory.getMessage(data, channel, 4);
				List<Message> list=new LinkedList<Message>();
				list.add(msg);
				
				return list;
			} else {
				logger.debug("Message full body missing ("+size+"), waiting for more data ("+data.length+").");
				/* If there is still data to store it. */
				buffers = new MessageParts();
				content.put(channel, buffers);
			}
		} else {
			logger.debug("Existing data, trying to complete Message full body");
		}

		buffers.add(data);
		List<Message> list = new LinkedList<Message>();

		while (!buffers.isEmpty()) {
			Message msg = buffers.build(channel);

			if (msg != null) {
				list.add(msg);
			} else {
				if (list.isEmpty()) {
					return null;
				}
				return list;
			}
		}
		
		content.remove(channel);

		return list;
	}
}
