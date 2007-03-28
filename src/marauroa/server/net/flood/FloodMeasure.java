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
		this.channel = channel;
		floodWarnings = 0;
		starttimestamp = System.currentTimeMillis();

		resetPerSecondData();
	}

	/**
	 * Clears the flood measurement and reset the timestamp.
	 *
	 */
	public void resetPerSecondData() {
		lasttimestamp = System.currentTimeMillis();
		sendMessages = 0;
		sendBytes = 0;
	}

	/**
	 * Add a new message to the measure.
	 * @param length
	 */
	public void addMessage(int length) {
		sendMessages++;
		sendBytes += length;
	}

	/**
	 * Adds a new flood warning to the measurement.
	 *
	 */
	public void warning() {
		floodWarnings++;
	}

	/**
	 * Return the amount of bytes per second the client sent.
	 * @return the amount of bytes per second the client sent.
	 */
	public int getBytesPerSecond() {
		int seconds=(int)((System.currentTimeMillis()-lasttimestamp)/1000);
		return sendBytes/seconds;
    }

	/**
	 * Return the amount of messages per second the client sent.
	 * @return the amount of messages per second the client sent.
	 */
	public int getMessagesPerSecond() {
		int seconds=(int)((System.currentTimeMillis()-lasttimestamp)/1000);
	    return sendMessages/seconds;
    }

	/**
	 * Return the amount of warnings done because of flood.
	 * @return the amount of warnings done because of flood.
	 */
	public int getWarnings() {
	    return floodWarnings;
    }

	/**
	 * Return the number of seconds since the last reset.
	 * @return the number of seconds since the last reset.
	 */
	public int sinceLastReset() {
		return (int)((System.currentTimeMillis()-lasttimestamp)/1000);
    }

	/**
	 * Reset the number of warnings because of flood.
	 *
	 */
	public void resetWarnings() {
	    floodWarnings=0;
	    
    }
}
