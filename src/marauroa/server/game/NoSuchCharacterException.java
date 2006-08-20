package marauroa.server.game;

public class NoSuchCharacterException extends Exception {
	private static final long serialVersionUID = 9139284837464521292L;

	public NoSuchCharacterException(String character) {
		super("Unable to find the requested character [" + character + "]");
	}
}
