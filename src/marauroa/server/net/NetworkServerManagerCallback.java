package marauroa.server.net;

/**
 * Callback interface into NetworkManagerServer to prevent
 * dependency loop between NetworkManagerServer and ...Writer/Reader.
 *
 * @author hendrik
 */
public interface NetworkServerManagerCallback {

	/**
	 * Are we still running?
	 *
	 * @return keepRunning
	 */
	public boolean isStillRunning();
}
