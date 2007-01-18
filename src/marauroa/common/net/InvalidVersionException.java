package marauroa.common.net;

/** This exception is thrown when a invalid version message is recieved. */
public class InvalidVersionException extends Exception {
	private static final long serialVersionUID = 7892075553859015832L;

	private int version;

	public InvalidVersionException(int version) {
		super();
		this.version = version;
	}

	public int getVersion() {
		return version;
	}
}
