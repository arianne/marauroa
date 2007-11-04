package marauroa.common.game;

/**
 * thrown in case an exspected rpobject is not found
 */
public class RPObjectNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 4649823545471552977L;

	/**
	 * creates a new RPObjectNotFoundException
	 *
	 * @param id id of exspected rpobject
	 */
	public RPObjectNotFoundException(RPObject.ID id) {
		super("RP Object [" + id + "] not found");
	}
}
