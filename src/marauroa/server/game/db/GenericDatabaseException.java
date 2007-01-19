package marauroa.server.game.db;

@Deprecated
public class GenericDatabaseException extends Exception {

	public GenericDatabaseException(Exception e) {
		super(e);
	}

	public GenericDatabaseException(String text, Exception sqle) {
		super(text,sqle);
	}

	public GenericDatabaseException(String text) {
		super(text);
	}

}
