package marauroa.server.game;

/**
 * This exception is thrown when an action lacks of an important attribute like:
 * <ul>
 * <li>sourceid
 * <li>zoneid
 * </ul>
 *
 * @author miguel
 *
 */
public class ActionInvalidException extends Exception {

	private static final long serialVersionUID = -2287105367089095987L;

	/** Constructor */
	public ActionInvalidException(String attribute) {
		super("Action is invalid: It lacks of mandatory attribute [" + attribute + "]");
	}
}
