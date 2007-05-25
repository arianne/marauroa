package marauroa.server.game.rp;

import marauroa.common.game.RPObject;


/**
 * This class is a factory to convert RPObjects into real objects for your game.
 * This class is invoked by whatever that load an object into the server:
 * - JDBCDatabase.
 * 
 * @author miguel
 *
 */
public class RPObjectFactory {
	/**
	 * This method is called when object is serialized back from database to
	 * zone, so you can define which subclass of RPObject we are going to use.
	 * This implements a factory pattern.
	 *
	 * If you are not interested in this feature, just return the object
	 *
	 * @param object
	 *            the original object
	 * @return the new instance of the object
	 */
	public RPObject factory(RPObject object) {
		return object;
	}
}
