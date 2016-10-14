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
package marauroa.server.game.rp;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import marauroa.common.Log4J;
import marauroa.common.game.IRPZone;
import marauroa.common.game.Perception;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.RPZoneDAO;
import marauroa.server.game.dbcommand.StoreZoneCommand;

/**
 * Default implementation of <code>IRPZone</code>. This class implements the
 * Delta^2 algorithm to save bandwidth.
 * <p>
 * The idea behind the DPA is to avoid sending ALL the objects to a client each
 * time, but only those that have been modified. Imagine that we have 1000
 * objects, and only Object 1 and Object 505 are active objects that are
 * modified each turn.
 *
 * <pre>
 *   The Traditional method:
 *
 *   - Get objects that our player should see ( 1000 objects )
 *   - Send them to player ( 1000 objects )
 *   - Next turn
 *   - Get objects that our player should see ( 1000 objects )
 *   - Send them to player
 *   - Next turn
 *   ...
 * </pre>
 *
 * I hope you see the problem... we are sending objects that haven't changed
 * each turn.
 *
 * <pre>
 *   The delta perception algorithm:
 *
 *   - Get objects that our player should see ( 1000 objects )
 *   - Reduce the list to the modified ones ( 1000 objects )
 *   - Store also the objects that are not longer visible ( 0 objects )
 *   - Send them to player ( 1000 objects )
 *   - Next turn
 *   - Get objects that our player should see ( 1000 objects )
 *   - Reduce the list to the modified ones ( 2 objects )
 *   - Store also the objects that are not longer visible ( 0 objects )
 *   - Send them to player ( 2 objects )
 *   - Next turn
 *   ...
 * </pre>
 *
 * The next step of the delta perception algorithm is pretty clear: delta2<br>
 * The idea is to send only what changes of the objects that changed. This way
 * we save even more bandwidth, making perceptions around 20% of the original
 * delta perception size.
 * <p>
 * The delta2 algorithm is based on four containers:
 * <ul>
 * <li>List of added objects
 * <li>List of modified added attributes of objects
 * <li>List of modified deleted attributes of objects
 * <li>List of deleted objects
 * </ul>
 * To make perceptions work, it is important to call the modify method in
 * RPZone, so this way objects modified are stored in the modified list.
 *
 * @author miguel
 */
public class MarauroaRPZone implements IRPZone {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(MarauroaRPZone.class);

	/** Name of the zone */
	protected ID zoneid;

	/** Objects contained by the zone indexed by its id. */
	protected Map<RPObject.ID, RPObject> objects;

	/**
	 * Objects that has been modified on zone since last turn. This information
	 * is useful for Delta^2 algorithm.
	 */
	private Set<RPObject> modified;

	/** This is the perception for the current turn. */
	private Perception perception;

	/** This is a cache for the perception for this turn. */
	private Perception prebuildDeltaPerception = null;

	/** This is a sync perception cache */
	private Perception prebuildSyncPerception = null;

	/** This variable stores the last assigned id, that is unique per zone. */
	private int lastNonPermanentIdAssigned = 0;

	private static Random rand = new Random();

	/**
	 * Creates a new MarauroaRPZone
	 *
	 * @param zoneid name of zone
	 */
	public MarauroaRPZone(String zoneid) {
		this.zoneid = new ID(zoneid);
		rand.setSeed(new Date().getTime());

		objects = new LinkedHashMap<RPObject.ID, RPObject>();
		modified = new HashSet<RPObject>();

		perception = new Perception(Perception.DELTA, this.zoneid);
	}

	/** Returns the zoneid */
	public ID getID() {
		return zoneid;
	}

	public void onFinish() throws Exception {
		storeToDatabase();
	}

	/**
	 * Store objects that has been tagged as storable to database asynchronously.
	 * Note: This methods returns before the saving is completed.
	 */
	public void storeToDatabase() {
		List<RPObject> list = new LinkedList<RPObject>();
		for (RPObject object : objects.values()) {
			list.add((RPObject) object.clone());
		}
		DBCommandQueue.get().enqueue(new StoreZoneCommand(this, list));
	}


	/**
	 * Load objects in database for this zone that were stored 
	 * and waits for the database operation to complete.
	 */
	public void onInit() throws Exception {
		DAORegister.get().get(RPZoneDAO.class).loadRPZone(this);
	}

	/**
	 * This method adds an object to this zone.
	 *
	 * @param object
	 *            object to add.
	 * @throws RPObjectInvalidException
	 *             if it lacks of mandatory attributes.
	 */
	public void add(RPObject object) throws RPObjectInvalidException {
		try {
			RPObject.ID id = object.getID();

			object.resetAddedAndDeleted();
			objects.put(id, object);

			if (!object.isHidden()) {
				perception.added(object);
			}
		} catch (Exception e) {
			throw new RPObjectInvalidException(e);
		}
	}

	/**
	 * This method notify zone that the object has been modified. You should
	 * call it only once per turn, even if inside the turn you modify it several
	 * times.
	 *
	 * @param object
	 *            object to modify.
	 * @throws RPObjectInvalidException
	 *             if it lacks of mandatory attributes.
	 */
	public void modify(RPObject object) throws RPObjectInvalidException {
		try {
			modified.add(object);
		} catch (Exception e) {
			throw new RPObjectInvalidException(e.getMessage());
		}
	}

	/**
	 * Removes the object from zone.
	 *
	 * @param id
	 *            identified of the removed object
	 * @return the removed object
	 */
	public RPObject remove(RPObject.ID id) {
		RPObject object = objects.remove(id);

		if (object != null) {
			/* We create an empty copy of the object */
			RPObject deleted = new RPObject();
			deleted.setID(object.getID());
			deleted.setRPClass(object.getRPClass());

			perception.removed(deleted);
		}

		return object;
	}

	/**
	 * Hide an object from the perceptions, but it doesn't remove it from world.
	 * Any further calls to modify will be ignored.
	 *
	 * @param object
	 *            the object to hide.
	 */
	public void hide(RPObject object) {
		object.hide();

		/* We create an empty copy of the object */
		RPObject deleted = new RPObject();
		deleted.setID(object.getID());
		deleted.setRPClass(object.getRPClass());

		perception.removed(deleted);
	}

	/**
	 * Makes a hidden object to be visible again. It will appear on the
	 * perception as an added object.
	 *
	 * @param object
	 *            the object to unhide.
	 */
	public void unhide(RPObject object) {
		object.unhide();

		object.resetAddedAndDeleted();
		perception.added(object);
	}

	/**
	 * Returns the object which id is id.
	 *
	 * @param id
	 *            identified of the removed object
	 * @return the object
	 */
	public RPObject get(RPObject.ID id) {
		return objects.get(id);
	}

	/**
	 * Returns true if the zone has that object.
	 *
	 * @param id
	 *            identified of the removed object
	 * @return true if object exists.
	 */
	public boolean has(RPObject.ID id) {
		return objects.containsKey(id);
	}

	/**
	 * This method assigns a valid id to the object.
	 *
	 * @param object
	 *            the object that is going to obtain a new id
	 */
	public void assignRPObjectID(RPObject object) {
		RPObject.ID id = new RPObject.ID(++lastNonPermanentIdAssigned, zoneid);
		while (has(id)) {
			id = new RPObject.ID(++lastNonPermanentIdAssigned, zoneid);
		}

		object.put("id", id.getObjectID());
		object.put("zoneid", zoneid.getID());
	}

	/**
	 * Iterates over all the objects in the zone.
	 *
	 * @return an iterator
	 */
	public Iterator<RPObject> iterator() {
		return objects.values().iterator();
	}

	/**
	 * Returns the perception of given type for that object.
	 *
	 * @param player
	 *            object whose perception we are going to build
	 * @param type
	 *            the type of perception:
	 *            <ul>
	 *            <li>SYNC
	 *            <li>DELTA
	 *            </ul>
	 */
	public Perception getPerception(RPObject player, byte type) {
		if (type == Perception.DELTA) {
			if (prebuildDeltaPerception == null) {
				prebuildDeltaPerception = perception;

				for (RPObject modified_obj : modified) {
					if (modified_obj.isHidden()) {
						continue;
					}
					try {
						if (logger.isDebugEnabled()) {
							if(!has(modified_obj.getID())) {
								logger.debug("Modifying a non existing object: "+modified_obj);
							}
						}
						
						prebuildDeltaPerception.modified(modified_obj);
					} catch (Exception e) {
						logger.error("cannot add object to modified list (object is: ["
						        + modified_obj + "])", e);
					}
				}
			}

			return prebuildDeltaPerception;
		} else /* type==Perception.SYNC */{
			if (prebuildSyncPerception == null) {
				prebuildSyncPerception = new Perception(Perception.SYNC, getID());
				prebuildSyncPerception.addedList = new ArrayList<RPObject>(objects.size());
				for (RPObject obj : objects.values()) {
					if (!obj.isHidden()) {
						prebuildSyncPerception.addedList.add(obj);
					}
				}
			}

			return prebuildSyncPerception;
		}
	}

	/**
	 * This methods resets the delta^2 information of objects.
	 */
	protected void reset() {
		/*
		 * We only reset the objects that have been modified because the rest should have been modified.
		 */
		for (RPObject object : modified) {
			object.resetAddedAndDeleted();
		}
	}

	/**
	 * This method returns the amount of objects in the zone.
	 *
	 * @return amount of objects.
	 */
	public long size() {
		return objects.size();
	}

	/**
	 * This method prints the whole zone. Handle it with care.
	 *
	 * @param out
	 *            the PrintStream where zone is printed.
	 */
	public void print(PrintStream out) {
		for (RPObject object : objects.values()) {
			out.println(object);
		}
	}

	/**
	 * This method moves zone from this turn to the next turn. It is called by
	 * RPWorld.
	 */
	public void nextTurn() {
		reset();

		prebuildSyncPerception = null;
		prebuildDeltaPerception = null;

		modified.clear();
		perception.clear();
	}
}
