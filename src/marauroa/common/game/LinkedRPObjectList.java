package marauroa.common.game;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A linked list which some helper functions for managing RPObjects
 *
 * @author hendrik
 */
// This class is not part of the Marauroa API but used internally.
class LinkedRPObjectList extends LinkedList<RPObject> {
	
	/**
	 * Gets the object from this list by its ID ignoring the zone.
	 * The zone is really irrelevant in a contained object.
	 *
	 * @param id the object id. Note that only the object_id field is relevant.
	 * @return the object or null if it is not found.
	 */
	public RPObject getByIDIgnoringZone(RPObject.ID id) {
		int oid = id.getObjectID();
		for (RPObject object : this) {
			if (object.getID().getObjectID() == oid) {
				return object;
			}
		}
		return null;
	}

	/**
	 * This method returns true if the slot has the object whose id is id
	 * ignoring the zone. The zone is really irrelevant in a contained object.
	 *
	 * @param id the object id. Note that only object_id field is relevant.
	 * @return true if it is found or false otherwise.
	 */
	public boolean hasByIDIgnoringZone(RPObject.ID id) {
		int oid = id.getObjectID();

		for (RPObject object : this) {
			if (oid == object.getID().getObjectID()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removed the object from this list by its ID ignoring the zone.
	 * The zone is really irrelevant in a contained object.
	 *
	 * @param id the object id. Note that only the object_id field is relevant.
	 * @return the object or null if it is not found.
	 */
	public RPObject removeByIDIgnoringZone(RPObject.ID id) {
		int oid = id.getObjectID();

		Iterator<RPObject> itr = this.iterator();
		while (itr.hasNext()) {
			RPObject rpobject = itr.next();
			if (rpobject.getID().getObjectID() == oid) {
				itr.remove();
				return rpobject;
			}
		}

		return null;
	}
}
