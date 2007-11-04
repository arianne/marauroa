package marauroa.common.game;

/** This exception is thrown when an attribute is not found */
public class SyntaxException extends RuntimeException {

	private static final long serialVersionUID = 3724525088825394097L;

	/**
	 * creates a new SyntaxException
	 *
	 * @param offendingAttribute name of missing attribute
	 */
	public SyntaxException(String offendingAttribute) {
		super("attribute " + offendingAttribute + " isn't defined.");
	}

	/**
	 * creates a new SyntaxException
	 *
	 * @param offendingAttribute id of missing attribute
	 */
	public SyntaxException(short offendingAttribute) {
		super("attribute code " + offendingAttribute + " isn't defined.");
	}
}
