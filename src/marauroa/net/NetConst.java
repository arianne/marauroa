/* $Id: NetConst.java,v 1.7 2003/12/31 13:03:06 arianne_rpg Exp $ */
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
  static public int marauroa_PORT=3214;
  /** Maximum size in bytes of the UDP packet. */
  final static public int UDP_PACKET_SIZE=1500;
  /** Number of the protocol version.
   *  0 - Initial version
   *  1 - Added Action message 
   *  2 - Added Perception message 
   *  3 - Multipacket udp
   *  4 - Added server info 
   *  5 - Modified Action message */
  final static public byte NETWORK_PROTOCOL_VERSION=5;
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
      marauroad.trace("NetConst::(static)","X","Using default: "+e.getMessage());
      marauroa_PORT=3214;
      }

    marauroad.trace("NetConst::(static)","<");
    }
  }