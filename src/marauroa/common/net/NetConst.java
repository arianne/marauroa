/* $Id: NetConst.java,v 1.19 2007/03/02 23:26:14 arianne_rpg Exp $ */
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
package marauroa.common.net;

import marauroa.common.Configuration;
import marauroa.common.Log4J;

/**
 * This class host several constants related to the network configuration of
 * Marauroa
 */
public class NetConst {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(NetConst.class);

	/** Port that will use the server for listen to incomming packets */
	static public int tcpPort;

	static {
		try {
			Configuration conf = Configuration.getConfiguration();
			tcpPort = Integer.parseInt(conf.get("tcp_port"));
		} catch (Exception e) {
			tcpPort = 3214;
			logger.warn("Using default port " + tcpPort, e);
		}
	}

	/**
	 * Number of the protocol version.
	 * 0 - Initial version
	 * 1 - Added Action message
	 * 2 - Added Perception message
	 * 3 - Multipacket udp
	 * 4 - Added server info
	 * 5 - Modified Action message
	 * 6 - Added zlib support to Perception message
	 * 7 - Modified Perception Added Invalid Version message
	 * 8 - Added Delta-delta support
	 * 9 - Added Timestamp to the Perception message
	 * 10 - Added conditional myRPObject send to Perception
	 * 11 - Added Map message
	 *    - Added Timestamp to all Messages
	 *    - Changed the ordering inside Perception message
	 * 12 - Changed perception to send only what is hidden on myRPObject
	 *    - Added Out of Sync Message - Improved speed of perception creation
	 * 13 - Redefined Map message
	 * 14 - Compressed the Map message
	 * 15 - Modified ServerInfo message to add RPClass stuff
	 * 16 - Added Transfer Content
	 *    - Added zone in perception
	 * 17 - Secured login
	 * 18 - Game name and version C2S and S2C on Login messages
	 * 19 - Added Perception Delta^2 MyRPObject
	 * 20 - Compressed server info message.
	 * 21 - Added capacity to RPSlot
	 *    - Changed the RPClass serialization scheme
	 * 22 - Changed signature method. Two bytes now.
	 * 30 - Marauroa 2.0 refactoring
	 */
	final static public byte NETWORK_PROTOCOL_VERSION = 30;
}
