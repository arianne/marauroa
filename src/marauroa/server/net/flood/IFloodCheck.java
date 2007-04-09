package marauroa.server.net.flood;

/**
 * Implement this interface to assert if an entry is flooding or not.
 *
 * @author miguel
 *
 */
public interface IFloodCheck {

	/**
	 * Returns true if with the information stored an entry implementation
	 * determines that there is a flood attack.
	 *
	 * @param entry
	 *            Flood measures to help us take a decision
	 * @return true if there is a flood from this entry.
	 */
	public boolean isFlooding(FloodMeasure entry);

	/**
	 * Called by FloodValidator when the connection is found to be flooding.
	 *
	 * @param entry
	 *            the channel that is causing the flood.
	 */
	public void onFlood(FloodMeasure entry);
}
