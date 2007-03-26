package marauroa.server.net.flood;

import java.nio.channels.SocketChannel;

/**
 * Stores for each player the amount of messages and bytes send since the last timestamp.
 * 
 * @author miguel
 *
 */
public class FloodMeasure {
	/** The socket channel associated. */
	public SocketChannel channel;
	/** The last timestamp when the flood was measured. */
	public long lasttimestamp;
	/** The amount of messages recieved from client since the timestamp. */
	public int sendMessages;
	/** The amount of bytes recieved from client since the timestamp */
	public int sendBytes;

	/**
	 * Clears the  
	 *
	 */
	public void reset() {
		lasttimestamp=System.currentTimeMillis();
		sendMessages=0;
		sendBytes=0;
	}
}
