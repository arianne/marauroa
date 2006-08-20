package marauroa.server.game;

public class PlayerAlreadyAddedException extends Exception {
	private static final long serialVersionUID = 2269068381383587171L;

	PlayerAlreadyAddedException(String player) {
		super("Player [" + player + "] already added to the database.");
	}
}
