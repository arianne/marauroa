package marauroa.server.net.nio;

import java.nio.channels.SocketChannel;

import marauroa.server.net.flood.FloodMeasure;
import marauroa.server.net.flood.IFloodCheck;

/**
 * A basic implementation of a flooding check.
 * We check that client doesn't send us more than 256 bytes per second or more than 3 messages per second.
 * If this happen, we warn client ( well, in fact we don't ), and at the third time it happens we consider
 * this a flooding.
 * @author miguel
 *
 */
public class FloodCheck implements IFloodCheck {
	public boolean isFlooding(FloodMeasure entry) {
		if(entry.getBytesPerSecond()>256 || entry.getMessagesPerSecond()>3) {
			entry.warning();
		}

		/* 
		 * We reset data each minute 
		 */
		if(entry.sinceLastReset()>60) {
			entry.resetPerSecondData();
		}			 
		
		return (entry.getWarnings()>=3);
	}

	public void onFlood(SocketChannel channel) {
	}

}
