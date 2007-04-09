package marauroa.server.net.nio;

import marauroa.common.Log4J;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.flood.FloodMeasure;
import marauroa.server.net.flood.IFloodCheck;

/**
 * A basic implementation of a flooding check. We check that client doesn't send
 * us more than 256 bytes per second or more than 6 messages per second. If this
 * happen, we warn client ( well, in fact we don't ), and at the third time it
 * happens we consider this a flooding.
 * 
 * @author miguel
 * 
 */
public class FloodCheck implements IFloodCheck {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(FloodCheck.class);

	private INetworkServerManager netMan;

	public FloodCheck(INetworkServerManager netMan) {
		this.netMan = netMan;
	}

	public boolean isFlooding(FloodMeasure entry) {
		if (entry.getBytesPerSecond() > 256 || entry.getMessagesPerSecond() > 6) {
			entry.warning();
		}

		/*
		 * We reset data each minute
		 */
		if (entry.sinceLastReset() > 60) {
			entry.resetPerSecondData();
		}

		return (entry.getWarnings() >= 3);
	}

	public void onFlood(FloodMeasure entry) {
		if (entry.getBytesPerSecond() > 1024 || entry.getMessagesPerSecond() > 12) {
			/*
			 * Ban for 10 minutes.
			 */
			logger.warn("Banning " + entry.channel + " for flooding server: " + entry);
			netMan.getValidator().addBan(entry.channel, 10 * 60);
		} else if (entry.getBytesPerSecond() > 512) {
			/*
			 * Just kick him
			 */
			logger.info("Disconnecting " + entry.channel + " for flooding server: " + entry);
			netMan.disconnectClient(entry.channel);
		} else {
			/*
			 * Give another chance.
			 */
			entry.resetWarnings();
		}
	}

}
