package marauroa.common.game;

/** This exception is thrown when an attribute is not found */
public class SyntaxException extends RuntimeException {

	private static final long serialVersionUID = 3724525088825394097L;

	public SyntaxException(String offendingAttribute) {
		super("attribute " + offendingAttribute + " isn't defined.");
	}

	public SyntaxException(short offendingAttribute) {
		super("attribute code " + offendingAttribute + " isn't defined.");
	}
}
