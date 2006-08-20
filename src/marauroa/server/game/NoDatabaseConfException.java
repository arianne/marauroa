package marauroa.server.game;

public class NoDatabaseConfException extends IllegalStateException {
	private static final long serialVersionUID = -4145441757361358659L;

	public NoDatabaseConfException(Throwable cause) {
		super("Database configuration file not found.", cause);
	}

	public NoDatabaseConfException() {
		super("Database configuration file not found.");
	}
}
