/* $Id: Perception.java,v 1.6 2006/08/20 15:40:08 wikipedian Exp $ */
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

import marauroa.common.Log4J;

import org.apache.log4j.Logger;

/** The Perception class provides a encapsultated way of managing perceptions */
public class Perception {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Perception.class);

	/** A Delta perception sends only changes */
	final public static byte DELTA = 0;

	/** A sync perception sends the whole world */
	final public static byte SYNC = 1;

	public byte type;

	public IRPZone.ID zoneid;

	public List<RPObject> addedList;

	public List<RPObject> modifiedAddedAttribsList;

	public List<RPObject> modifiedDeletedAttribsList;

	public List<RPObject> deletedList;

	public Perception(byte type, IRPZone.ID zoneid) {
		this.type = type;
		this.zoneid = zoneid;
		addedList = new LinkedList<RPObject>();
		modifiedAddedAttribsList = new LinkedList<RPObject>();
		modifiedDeletedAttribsList = new LinkedList<RPObject>();
		deletedList = new LinkedList<RPObject>();
	}

	/** This method adds an added object to the world */
	public void added(RPObject object) {
		if (!addedHas(object)) {
			addedList.add(object);
		}
	}

	/** This method adds an modified object of the world */
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

	/** This method adds a removed object of the world */
	public void removed(RPObject object) {
		if (addedHas(object)) {
			try {
				for (Iterator<RPObject> it = addedList.iterator(); it.hasNext();) {
					RPObject added = it.next();
					if (added.get("id").equals(object.get("id"))) {
						it.remove();
						/*
						 * NOTE: If object was added and now remove we simply
						 * don't mention the object at all
						 */
						return;
					}
				}
			} catch (AttributeNotFoundException e) {
				logger.error("error removing an object ", e);
			}
		}

		if (!removedHas(object)) {
			deletedList.add(object);
		}
	}

	/** Returns the number of elements of the perception */
	public int size() {
		return (addedList.size() + modifiedAddedAttribsList.size()
				+ modifiedDeletedAttribsList.size() + deletedList.size());
	}

	/** Clear the perception */
	public void clear() {
		addedList.clear();
		modifiedAddedAttribsList.clear();
		modifiedDeletedAttribsList.clear();
		deletedList.clear();
	}

	private boolean removedHas(RPObject object) {
		try {
			for (RPObject deleted : deletedList) {
				if (deleted.get("id").equals(object.get("id"))) {
					return true;
				}
			}
		} catch (AttributeNotFoundException e) {
		}

		return false;
	}

	private boolean addedHas(RPObject object) {
		try {
			for (RPObject added : addedList) {
				if (added.get("id").equals(object.get("id"))) {
					return true;
				}
			}
		} catch (AttributeNotFoundException e) {
		}

		return false;
	}
}
