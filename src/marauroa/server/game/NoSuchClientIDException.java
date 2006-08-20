package marauroa.server.game;

public class NoSuchClientIDException extends Exception {
	private static final long serialVersionUID = -2364668338342421162L;

	public NoSuchClientIDException(int clientid) {
		super("Unable to find the requested client id [" + clientid + "]");
	}
}
