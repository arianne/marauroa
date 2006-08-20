package marauroa.server.game;

class CharacterAlreadyAddedException extends Exception {
	private static final long serialVersionUID = -3614527698941280035L;

	public CharacterAlreadyAddedException(String character) {
		super("Character [" + character + "] already added to the database");
	}
}
