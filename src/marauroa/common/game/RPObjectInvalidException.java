package marauroa.common.game;

/**
 * invalid rpobject (required attribute is missing).
 */
public class RPObjectInvalidException extends RuntimeException {

	private static final long serialVersionUID = 2413566633754598291L;

	/**
	 * creates a new RPObjectInvalidException
	 *
	 * @param attribute name of missing attribute
	 */
	public RPObjectInvalidException(String attribute) {
		super("Object is invalid: It lacks of mandatory attribute [" + attribute + "]");
	}
}
