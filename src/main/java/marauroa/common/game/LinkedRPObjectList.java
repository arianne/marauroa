/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.game;

import java.util.Iterator;
import java.util.LinkedList;

import marauroa.common.Log4J;
import marauroa.common.Logger;

/**
 * A linked list which some helper functions for managing RPObjects
 *
 * @author hendrik
 */
// This class is not part of the Marauroa API but used internally.
class LinkedRPObjectList extends LinkedList<RPObject> {
	private static final long serialVersionUID = -7221795029536087812L;
	private static final Logger logger = Log4J.getLogger(LinkedRPObjectList.class);

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

	@Override
	public boolean add(RPObject object) {
		checkObjectNotAllreadyInList(object);
		return super.add(object);
	}

	/**
	 * adds an object without checking if it is already in the list.
	 * This is useful for cloning the list.
	 *
	 * @param object object to add
	 * @return true
	 */
	boolean addTrusted(RPObject object) {
		return super.add(object);
	}

	// TODO: Read RPSlot.java and decide whether assigning unique ids
	// is within the responsibilities of Marauroa or the Application.
	// In the first case fix the bug; in the second case throw a
	// specialized exception.
	private void checkObjectNotAllreadyInList(RPObject object) {
		RPObject.ID id = object.getID();
		RPObject oldObject = getByIDIgnoringZone(id);
		if (oldObject != null) {
			if (oldObject == object) {
				logger.error("Object cannot be added to list because it is already part of it: " + object, new Throwable());
			} else {
			    logger.error("Object cannot be added to list because another object with the same ID is part of it. objectToAdd: " + object + " objectAlreadyInList: " + oldObject, new Throwable());
			}
		}
	}
}
