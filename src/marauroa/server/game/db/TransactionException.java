package marauroa.server.game.db;

public class TransactionException extends Exception {
	private static final long serialVersionUID = -7935679275412534304L;

	TransactionException(String msg) {
		super(msg);
	}

	TransactionException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
