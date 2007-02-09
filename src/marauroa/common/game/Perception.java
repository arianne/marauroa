/* $Id: Perception.java,v 1.9 2007/02/09 15:51:45 arianne_rpg Exp $ */
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
package marauroa.common.game;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** 
 * The Perception class provides a encapsultated way of managing perceptions.
 * It is the core implementation of the Delta^2.
 * 
 * Perception manages added, modified and removed perceptions.
 * 
 * @author miguel
 */
public class Perception {
	/** A Delta perception sends only changes */
	final public static byte DELTA = 0;

	/** A sync perception sends the whole world */
	final public static byte SYNC = 1;

	/** The type of the perception: Delta or Sync */
	public byte type;

	/** The zone.id to which this perception belong*/
	public IRPZone.ID zoneid;

	/** The added objects */
	public List<RPObject> addedList;

	/** The modified added objects */
	public List<RPObject> modifiedAddedAttribsList;

	/** The modified deleted objects */
	public List<RPObject> modifiedDeletedAttribsList;

	/** The deleted objects */
	public List<RPObject> deletedList;

	/** Constructor */
	public Perception(byte type, IRPZone.ID zoneid) {
		this.type = type;
		this.zoneid = zoneid;

		addedList = new LinkedList<RPObject>();
		modifiedAddedAttribsList = new LinkedList<RPObject>();
		modifiedDeletedAttribsList = new LinkedList<RPObject>();
		deletedList = new LinkedList<RPObject>();
	}

	/**
	 * This method adds an added object to the perception
	 * @param object the object added.
	 */
	public void added(RPObject object) {
		if (!addedHas(object)) {
			addedList.add(object);
		}
	}

	/** 
	 * This method adds an modified object of the world 
	 * @param modified the modified object
	 * @throws Exception if there is any problem computing the differences.
	 */
	public void modified(RPObject modified) throws Exception {
		if (!removedHas(modified) && !addedHas(modified)) {
			RPObject added = new RPObject();
			RPObject deleted = new RPObject();

			modified.getDifferences(added, deleted);
			if (added.size() > 0) {
				modifiedAddedAttribsList.add(added);
			}

			if (deleted.size() > 0) {
				modifiedDeletedAttribsList.add(deleted);
			}
		} else {
			modified.resetAddedAndDeleted();
		}
	}

	/** 
	 * This method adds a removed object of the world
	 * @param object the removed object
	 */
	public void removed(RPObject object) {
		if (addedHas(object)) {
			for (Iterator<RPObject> it = addedList.iterator(); it.hasNext();) {
				RPObject added = it.next();
				if (added.get("id").equals(object.get("id"))) {
					/*
					 * NOTE: If object was added and now remove we simply
					 * don't mention the object at all
					 */
					it.remove();
					return;
				}
			}
		}

		if (!removedHas(object)) {
			deletedList.add(object);
		}
	}

	/** 
	 * Returns the number of elements of the perception
	 * @return the amount of objects 
	 */
	public int size() {
		return (addedList.size() + modifiedAddedAttribsList.size() + modifiedDeletedAttribsList.size() + deletedList.size());
	}

	/** 
	 * Clear the perception 
	 */
	public void clear() {
		addedList.clear();
		modifiedAddedAttribsList.clear();
		modifiedDeletedAttribsList.clear();
		deletedList.clear();
	}

	private boolean removedHas(RPObject object) {
		for (RPObject deleted : deletedList) {
			if (deleted.get("id").equals(object.get("id"))) {
				return true;
			}
		}
		return false;
	}

	private boolean addedHas(RPObject object) {
		for (RPObject added : addedList) {
			if (added.get("id").equals(object.get("id"))) {
				return true;
			}
		}
		return false;
	}
}
