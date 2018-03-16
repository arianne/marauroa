/***************************************************************************
 *                   (C) Copyright 2007-2010 - Marauroa                    *
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
import java.util.Properties;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.flood.FloodMeasure;
import marauroa.server.net.flood.IFloodCheck;

/**
 * A basic implementation of a flooding check. We check that client doesn't send
 * us more than 1024 bytes per second or more than 20 messages per second. If this
 * happen, we warn client ( well, in fact we don't ), and at the third time it
 * happens we consider this a flooding.
 * 
 * @author miguel
 */
public class FloodCheck implements IFloodCheck {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(FloodCheck.class);

	private INetworkServerManager netMan;

	private int allowedBytesPerSecond = 1024;
	private int allowedMessagesPerSecond = 20;

	/**
	 * creates a new FloodChecker
	 *
	 * @param netMan INetworkServerManager
	 */
	public FloodCheck(INetworkServerManager netMan) {
		this.netMan = netMan;
		try {
			Properties config = Configuration.getConfiguration().getAsProperties();
			allowedBytesPerSecond = Integer.parseInt(config.getProperty("allowed_bytes_per_second", "1024"));
			allowedMessagesPerSecond = Integer.parseInt(config.getProperty("allowed_messages_per_second", "20"));
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	public boolean isFlooding(FloodMeasure entry) {
		if (entry.getBytesPerSecond() > allowedBytesPerSecond || entry.getMessagesPerSecond() > allowedMessagesPerSecond) {
			entry.warning();
		}

		/*
		 * We reset data each minute
		 */
		if (entry.sinceLastReset() > 60) {
			entry.resetPerSecondData();
		}

		return (entry.getWarnings() >= 3);
	}

	public void onFlood(FloodMeasure entry) {
		if (entry.getBytesPerSecond() > 2048 || entry.getMessagesPerSecond() > 24) {
			/*
			 * Ban for 10 minutes.
			 */
			logger.warn("Banning " + entry.channel + " for flooding server: " + entry);
			netMan.getValidator().addBan(entry.channel, 10 * 60);
		} else if (entry.getBytesPerSecond() > 1024) {
			/*
			 * Just kick him
			 */
			logger.info("Disconnecting " + entry.channel + " for flooding server: " + entry);
			netMan.disconnectClient(entry.channel);
		} else {
			logger.info("Giving another chance to " + entry.channel + " for flooding server: " + entry);
			/*
			 * Give another chance.
			 */
			entry.resetWarnings();
		}
	}

}
