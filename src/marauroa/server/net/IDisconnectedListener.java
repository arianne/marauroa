package marauroa.server.net;

import java.net.InetSocketAddress;

/** 
 * This interface provides a callback notification for disconnected clients.
 * 
 * @author miguel
 *
 */
public interface IDisconnectedListener {
	/** This method is called when a connection is closed. */
	public void onDisconnect(InetSocketAddress address);

}
