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
	/** When this entry was created. */
	private long starttimestamp;
	/** Store how many times it has caused a flood warning. */
	public int floodWarnings;

	/**
	 * Constructor
	 * @param channel the associated resource to this measure object.
	 */
	public FloodMeasure(SocketChannel channel) {
		this.channel=channel;
		floodWarnings=0;
		starttimestamp=System.currentTimeMillis();

		reset();
	}

	/**
	 * Clears the flood measurement and reset the timestamp.
	 *
	 */
	public void reset() {
		lasttimestamp=System.currentTimeMillis();
		sendMessages=0;
		sendBytes=0;
	}

	/**
	 * Add a new message to the measure.
	 * @param length
	 */
	public void addMessage(int length) {
		sendMessages++;
		sendBytes+=length;
	}

	/**
	 * Adds a new flood warning to the measurement.
	 *
	 */
	public void warning() {
		floodWarnings++;
	}
}
