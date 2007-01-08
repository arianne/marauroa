package marauroa.server.game;

/**
 * This exception is thrown when a character is tried to be loaded but it doesn't exist.
 * 
 * @author miguel
 *
 */
public class CharacterNotFoundException extends Exception {
	private static final long serialVersionUID = 4421144516681943172L;

	public CharacterNotFoundException(String character) {
		super("Character [" + character + "] not found on the database");
	}
}
