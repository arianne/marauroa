/* $Id: NetConst.java,v 1.21 2004/05/14 15:51:38 arianne_rpg Exp $ */
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
package marauroa.net;

import marauroa.*;

/** This class host several constants related to the network configuration of
 *  Marauroa */
public class NetConst
  {
  /** Port that will use the server for listen to incomming packets */
  static public int marauroa_PORT;
  /** Maximum size in bytes of the UDP packet. */
  final static public int UDP_PACKET_SIZE=1500;
  /** Number of the protocol version.
   *  0 - Initial version
   *  1 - Added Action message 
   *  2 - Added Perception message 
   *  3 - Multipacket udp
   *  4 - Added server info 
   *  5 - Modified Action message
   *  6 - Added zlib support to Perception message 
   *  7 - Modified Perception 
   *      Added Invalid Version message 
   *  8 - Added Delta-delta support
   *  9 - Added Timestamp to the Perception message  
   * 10 - Added conditional myRPObject send to Perception
   * 11 - Added Map message 
   *    - Added Timestamp to all Messages
   *    - Changed the ordering inside Perception message 
   * 12 - Changed perception to send only what is hidden on myRPObject
   *    - Added Out of Sync Message
   *    - Improved speed of perception creation */
  final static public byte NETWORK_PROTOCOL_VERSION=12;
  final static public long PACKET_TIMEOUT_VALUE=5000;
  static
    {
    marauroad.trace("NetConst::(static)",">");
    try
      {
      Configuration conf=Configuration.getConfiguration();

      marauroa_PORT=Integer.parseInt(conf.get("marauroa_PORT"));
      }
    catch(Exception e)
      {
      marauroa_PORT=3214;
      marauroad.trace("NetConst::(static)","X","Using default port "+  marauroa_PORT   +" : "+e.getMessage());
      marauroad.thrown("NetConst::(static)","X",e);
      }
    marauroad.trace("NetConst::(static)","<");
    }
  }
