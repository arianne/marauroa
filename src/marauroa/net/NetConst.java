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
  /** Number of the protocol version */
  final static public byte NETWORK_PROTOCOL_VERSION=3;
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