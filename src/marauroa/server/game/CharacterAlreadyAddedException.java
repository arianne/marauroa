package marauroa.server.game;

/**
 * This exception is thrown when a character is added to an account but the 
 * character already exists.
 * 
 * @author miguel
 *
 */
class CharacterAlreadyAddedException extends Exception {
	private static final long serialVersionUID = -3614527698941280035L;

	public CharacterAlreadyAddedException(String character) {
		super("Character [" + character + "] already added to the database");
	}
}
