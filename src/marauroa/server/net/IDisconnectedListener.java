package marauroa.server.net;

import java.nio.channels.SocketChannel;

/** 
 * This interface provides a callback notification for disconnected clients.
 * 
 * @author miguel
 *
 */
public interface IDisconnectedListener {
	/** This method is called when a connection is closed. */
	public void onDisconnect(SocketChannel channel);

}
