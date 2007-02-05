/* $Id: MarauroaRPZone.java,v 1.3 2007/02/05 17:14:53 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import marauroa.common.Log4J;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.IRPZone;
import marauroa.common.game.Perception;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;

import org.apache.log4j.Logger;

/**
 * default implementation of <code>IRPZone</code> 
 * This class implements the Delta² algorithm to save bandwidth.
 * You should extends this class.
 * @author miguel
 *
 */
public abstract class MarauroaRPZone implements IRPZone {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(MarauroaRPZone.class);

	/** Name of the zone */
	protected ID zoneid;

	/** Objects contained by the zone indexed by its id. */
	protected Map<RPObject.ID, RPObject> objects;

	/** Objects that has been modified on zone since last turn.
	 *  This information is useful for DeltaÂ² algorithm. */
	private Map<RPObject.ID, RPObject> modified;

	/** This is the perception for the actual turn. */
	private Perception perception;

	/** This is a cache for the perception for this turn. */
	private Perception prebuildDeltaPerception = null;

	/** This is a sync perception cache */
	private Perception prebuildSyncPerception = null;

	/** This variable stores the last assigned id, that is unique per zone. */
	private static int lastNonPermanentIdAssigned = 0;

	private static Random rand = new Random();

	public MarauroaRPZone(String zoneid) {
		this.zoneid = new ID(zoneid);
		rand.setSeed(new Date().getTime());

		objects = new LinkedHashMap<RPObject.ID, RPObject>();
		modified = new LinkedHashMap<RPObject.ID, RPObject>();

		perception = new Perception(Perception.DELTA, getID());
	}

	/** Returns the zoneid */
	public ID getID() {
		return zoneid;
	}

	/** 
	 * This method adds an object to this zone.
	 */
	public void add(RPObject object) throws RPObjectInvalidException {
		try {
			RPObject.ID id = object.getID();

			object.resetAddedAndDeleted();
			objects.put(id, object);

			perception.added(object);
		} catch (AttributeNotFoundException e) {
			throw new RPObjectInvalidException(e.getMessage());
		}
	}

	/**
	 * This method notify zone that the object has been modified.
	 * You should call it only once per turn, even if inside the turn you modify
	 * it several times.
	 */
	public void modify(RPObject object) throws RPObjectInvalidException {
		try {
			RPObject.ID id = object.getID();

			if (!modified.containsKey(id) && has(id)) {
				modified.put(id, object);
			}
		} catch (Exception e) {
			throw new RPObjectInvalidException(e.getMessage());
		}
	}

	/**
	 * Removes the object from zone.
	 * @return the removed object
	 */
	public RPObject remove(RPObject.ID id) {
		RPObject object = objects.remove(id);

		if(object!=null) {
			// If objects has been removed, remove from modified
			modified.remove(object.getID());
			perception.removed((RPObject) object.clone());
		}

		return object;
	}

	/**
	 * Returns the object which id is id.
	 * @return the object
	 */
	public RPObject get(RPObject.ID id) {
		return objects.get(id);
	}

	/** 
	 * Returns true if the zone has that object.
	 * @return true if object exists.
	 */
	public boolean has(RPObject.ID id) {
		return objects.containsKey(id);
	}

	/**
	 * This method assigns a valid id to the object.
	 * @param object the object that is going to obtain a new id
	 */
	public void assignRPObjectID(RPObject object) {
		RPObject.ID id = new RPObject.ID(++lastNonPermanentIdAssigned, zoneid);
		while (has(id)) {
			id = new RPObject.ID(++lastNonPermanentIdAssigned, zoneid);
		}

		object.put("id", id.getObjectID());
		object.put("zoneid", zoneid.getID());
	}

	/** Iterates  over all the objects in the zone. */
	public Iterator<RPObject> iterator() {
		return objects.values().iterator();
	}

	/** 
	 * Returns the perception of given type for that object.
	 * @param id object whose perception we are going to build
	 * @param type the type of perception: 
	 * <ul>
	 * <li>SYNC
	 * <li>DELTA
	 * </ul>
	 */
	public Perception getPerception(RPObject.ID id, byte type) {
		if (type == Perception.DELTA) {
			if (prebuildDeltaPerception == null) {
				prebuildDeltaPerception = perception;

				for (RPObject modified_obj : modified.values()) {
					try {
						prebuildDeltaPerception.modified(modified_obj);
					} catch (Exception e) {
						logger.error("cannot add object to modified list (object is: ["	+ modified_obj + "])", e);
					}
				}
			}

			return prebuildDeltaPerception;
		} else /* type==Perception.SYNC */ {
			if (prebuildSyncPerception == null) {
				prebuildSyncPerception = new Perception(Perception.SYNC,getID());
				prebuildSyncPerception.addedList = new ArrayList<RPObject>(objects.values());
			}

			return prebuildSyncPerception;
		}
	}

	/**
	 * This methods resets the delta² information of objects.
	 */
	public void reset() {
		for (RPObject object : objects.values()) {
			object.resetAddedAndDeleted();
		}
	}

	/**
	 * This method returns the amount of objects in the zone.
	 * @return amount of objects.
	 */
	public long size() {
		return objects.size();
	}

	/**
	 * This method prints the whole zone.
	 * Handle it with care.
	 * @param out the PrintStream where zone is printed.
	 */
	public void print(PrintStream out) {
		for (RPObject object : objects.values()) {
			out.println(object);
		}
	}

	/**
	 * This method moves zone from this turn to the next turn.
	 * It is called by RPWorld.
	 */
	public void nextTurn() {
		reset();

		prebuildSyncPerception = null;
		prebuildDeltaPerception = null;
		
		modified.clear();
		perception.clear();
	}
}
