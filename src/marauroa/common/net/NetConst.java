/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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
 * This class hosts several constants related to the network configuration of
 * Marauroa
 */
public class NetConst {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(NetConst.class);

	/** Port that will use the server for listen to incoming packets */
	@Deprecated
	public static final int tcpPort;

	static {
		int myTcpPort = 3214;
		try {
			Configuration conf = Configuration.getConfiguration();
			myTcpPort = Integer.parseInt(conf.get("tcp_port"));
		} catch (Exception e) {
			logger.warn("Using default port " + myTcpPort, e);
		}
		tcpPort = myTcpPort;
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
	 *    - Added Out of Sync Message
	 *    - Improved speed of perception creation
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
	 *    - Dropped UDP support
	 * 31 - Added KeepAlive message.
	 * 32 - Include RPObjects in S2CCharacterList
	 * 33 - Added support for maps as attributes
	 * 34 - Added hash on content transfer, empty perceptions are now omittable
	 */
	public static final byte NETWORK_PROTOCOL_VERSION = 34;

	/** Oldest supported protocol version */
	public static final byte NETWORK_PROTOCOL_VERSION_MIN = 31;

	/** Newest supported protocol version */
	// 40 up to Marauroa version 3.8.8
	public static final byte NETWORK_PROTOCOL_VERSION_MAX = 80;

	/** the first version with details on the MessageS2CCharacterList. */
	public static final byte FIRST_VERSION_WITH_DETAILS_IN_CHARACTER_LIST = 32;

	/** the first version with support for older versions. */
	public static final byte FIRST_VERSION_WITH_MULTI_VERSION_SUPPORT = 33;

	/** the first version that supports maps in rpobjects. */
	public static final byte FIRST_VERSION_WITH_MAP_SUPPORT = 33;

	/** the first version that supports maps in rpobjects. */
	public static final byte FIRST_VERSION_WITH_CONTENT_HASH = 34;

	/**
	 * the first version which allows the omittion of empty perceptions
	 * because the client does not depend sending of the KeepAlive message
	 * anymore on counting perception messages
	 */
	public static final int FIRST_VERSION_WITH_OMITTABLE_EMPTY_PERCEPTIONS = 34;

	/** longer reasons for bans */
	public static final int FIRST_VERSION_WITH_LONG_BAN_MESSAGE = 34;

}
