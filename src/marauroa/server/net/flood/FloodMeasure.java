/* $Id: FloodMeasure.java,v 1.10 2010/05/14 19:39:42 kymara Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.server.net.flood;

import java.nio.channels.SocketChannel;

/**
 * Stores for each player the amount of messages and bytes sent since the last
 * timestamp.
 *
 * @author miguel
 *
 */
public class FloodMeasure {

	/** The socket channel associated. */
	public SocketChannel channel;

	/** The last timestamp when the flood was measured. */
	public long lasttimestamp;

	/** The amount of messages received from client since the timestamp. */
	public int sendMessages;

	/** The amount of bytes received from client since the timestamp */
	public int sendBytes;

	/** When this entry was created. */
	public long starttimestamp;

	/** Store how many times it has caused a flood warning. */
	public int floodWarnings;

	/**
	 * Constructor
	 *
	 * @param channel
	 *            the associated resource to this measure object.
	 */
	public FloodMeasure(SocketChannel channel) {
		this.channel = channel;
		floodWarnings = 0;
		starttimestamp = System.currentTimeMillis();

		resetPerSecondData();
	}

	/**
	 * Clears the flood measurement and reset the timestamp.
	 *
	 */
	public void resetPerSecondData() {
		lasttimestamp = System.currentTimeMillis();
		sendMessages = 0;
		sendBytes = 0;
	}

	/**
	 * Add a new message to the measure.
	 *
	 * @param length
	 */
	public void addMessage(int length) {
		sendMessages++;
		sendBytes += length;
	}

	/**
	 * Adds a new flood warning to the measurement.
	 *
	 */
	public void warning() {
		floodWarnings++;
	}

	/**
	 * Return the amount of bytes per second the client sent.
	 *
	 * @return the amount of bytes per second the client sent.
	 */
	public int getBytesPerSecond() {
		int seconds = (int) ((System.currentTimeMillis() - lasttimestamp) / 1000) + 1;
		return sendBytes / seconds;
	}

	/**
	 * Return the amount of messages per second the client sent.
	 *
	 * @return the amount of messages per second the client sent.
	 */
	public int getMessagesPerSecond() {
		int seconds = (int) ((System.currentTimeMillis() - lasttimestamp) / 1000) + 1;
		return sendMessages / seconds;
	}

	/**
	 * Return the amount of warnings done because of flood.
	 *
	 * @return the amount of warnings done because of flood.
	 */
	public int getWarnings() {
		return floodWarnings;
	}

	/**
	 * Return the number of seconds since the last reset.
	 *
	 * @return the number of seconds since the last reset.
	 */
	public int sinceLastReset() {
		return (int) ((System.currentTimeMillis() - lasttimestamp) / 1000);
	}

	/**
	 * Reset the number of warnings because of flood.
	 *
	 */
	public void resetWarnings() {
		floodWarnings = 0;

	}

	@Override
	public String toString() {
		StringBuffer os = new StringBuffer();
		os.append("[");
		os.append(" time: " + sinceLastReset());
		os.append(" send bytes: " + sendBytes);
		os.append(" send messages: " + sendMessages);
		os.append("]");

		return os.toString();
	}
}
