/* $Id: RPSlot.java,v 1.28 2007/01/28 20:22:14 arianne_rpg Exp $ */
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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;

import org.apache.log4j.Logger;

/** This class represent a slot in an object */
public class RPSlot implements marauroa.common.net.Serializable,
		Iterable<RPObject> {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RPSlot.class);

	private List<RPObject> added;

	private List<RPObject> deleted;

	private String name;

	/** The capacity of the slot */
	private byte capacity;

	/** This slot is linked to an object: its owner. */
	private RPObject owner;

	/** A List<RPObject> of objects */
	private List<RPObject> objects;

	public void resetAddedAndDeletedRPObjects() {
		added.clear();
		deleted.clear();
	}

	public void setAddedRPObject(RPSlot slot) {
		for (RPObject object : slot.added) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			objects.add(copied);
		}
	}

	public void setDeletedRPObject(RPSlot slot) {
		for (RPObject object : slot.deleted) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			objects.add(copied);
		}
	}

	void setOwner(RPObject object) {
		owner = object;
	}

	RPObject getOwner() {
		return owner;
	}

	public RPSlot() {
		name = "";
		capacity = -1;
		owner = null;

		objects = new LinkedList<RPObject>();
		added = new LinkedList<RPObject>();
		deleted = new LinkedList<RPObject>();
	}

	public RPSlot(String name) {
		this();
		this.name = name;
	}

	/** This method create a copy of the slot */
	@Override
	public Object clone() {
		RPSlot slot = new RPSlot();

		slot.name = name;
		slot.owner = owner;
		slot.capacity = capacity;

		for (RPObject object : objects) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			slot.objects.add(copied);
		}

		for (RPObject object : added) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			slot.added.add(copied);
		}

		for (RPObject object : deleted) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			slot.deleted.add(copied);
		}

		return slot;
	}

	/** Sets the name of the slot */
	public void setName(String name) {
		this.name = name;
	}

	/** Get the name of the slot */
	public String getName() {
		return name;
	}

	public void assignValidID(RPObject object) {
		int i = objects.size();

		boolean exists = false;

		do {
			exists = false;

			for (RPObject obj : objects) {
				if (obj.getInt("id") == i) {
					exists = true;
				}
			}

			if (exists) {
				i++;
			}
		} while (exists);

		object.put("id", i);
		object.put("zoneid", ""); // TODO: Remove this and allow zoneless id
									// in objects
	}

	/** Add an object to the slot */
	public void add(RPObject object) {
		if (isFull()) {
			throw new SlotIsFullException(name);
		}

		try {
			boolean found = false;

			Iterator<RPObject> it = objects.iterator();
			while (!found && it.hasNext()) {
				RPObject data = it.next();
				if (data.get("id").equals(object.get("id"))) {
					it.remove();
					found = true;
				}
			}

			if (!found) {
				added.add(object);
			}

			// If the object is on deleted list, remove from there.
			found = false;
			it = deleted.iterator();
			while (!found && it.hasNext()) {
				RPObject data = it.next();
				if (data.get("id").equals(object.get("id"))) {
					it.remove();
					found = true;
				}
			}

			object.setContainer(owner, this);
			objects.add(object);
		} catch (AttributeNotFoundException e) {
			logger.error("error adding object", e);
		}
	}

	/** Gets the object from the slot */
	public RPObject get(RPObject.ID id) throws RPObjectNotFoundException {
		try {
			for (RPObject object : objects) {
				// We compare only the id, as the zone is really irrelevant
				if (object.getID().getObjectID() == id.getObjectID()) {
					return object;
				}
			}
			throw new RPObjectNotFoundException(id);
		} catch (AttributeNotFoundException e) {
			logger.warn("error getting object", e);
			throw new RPObjectNotFoundException(id);
		}
	}

	/** Gets the object from the slot */
	public RPObject getFirst() throws RPObjectNotFoundException {
		if (objects.size() > 0) {
			return objects.get(0);
		} else {
			return null;
		}
	}

	/** This method removes the object from the slot */
	public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException {
		try {
			Iterator<RPObject> it = objects.iterator();

			while (it.hasNext()) {
				RPObject object = it.next();

				/** We compare only the id, as the zone is really irrelevant */
				if (object.getID().getObjectID() == id.getObjectID()) {
					/*
					 * HACK: This is a hack to avoid a problem that happens when
					 * on the same turn an object is added and deleted, causing
					 * the client to confuse.
					 */
					boolean found_in_added_list = false;
					Iterator<RPObject> added_it = added.iterator();
					while (!found_in_added_list && added_it.hasNext()) {
						RPObject added_object = added_it.next();
						if (added_object.getID().getObjectID() == id
								.getObjectID()) {
							added_it.remove();
							found_in_added_list = true;
						}
					}

					if (!found_in_added_list) {
						deleted.add(new RPObject(new RPObject.ID(object)));
					}

					it.remove();

					object.setContainer(null, null);

					return object;
				}
			}

			throw new RPObjectNotFoundException(id);
		} catch (AttributeNotFoundException e) {
			logger.warn("error removing object", e);
			throw new RPObjectNotFoundException(id);
		}
	}

	/** This method empty the slot */
	public void clear() {
		for (RPObject object : objects) {
			try {
				deleted.add(new RPObject(new RPObject.ID(object)));
				object.setContainer(null, null);
			} catch (AttributeNotFoundException e) {
			}
		}

		added.clear();
		objects.clear();
	}

	/** This method returns true if the slot has the object whose id is id */
	public boolean has(RPObject.ID id) {
		try {
			for (RPObject object : objects) {
				// compare only the id, as the zone is not used for slots
				if (id.getObjectID() == object.getID().getObjectID()) {
					return true;
				}
			}
			return false;
		} catch (AttributeNotFoundException e) {
			return false;
		}
	}

	/** traverses up the container tree to see if the item is one of the parents */
	public boolean hasAsParent(RPObject.ID id) {
		try {
			RPObject owner = getOwner();
			// traverse the owner tree
			while (owner != null) {
				// compare only the id, as the zone is not used for slots
				if (owner.getID().getObjectID() == id.getObjectID()) {
					return true;
				}
				owner = owner.getContainer();
			}
			return false;
		} catch (AttributeNotFoundException e) {
			return false;
		}
	}

	/** traverses up the container tree and counts all parent container */
	public int getContainedDepth() {
		int depth = 0;
		RPObject owner = getOwner();
		// traverse the owner tree
		while (owner != null) {
			depth++;
			owner = owner.getContainer();
		}
		return depth;
	}

	/**
	 * returns the number of items in this container and all subcontainers.
	 * <b>Warning:</b> This method may be very expensive and can lead to a
	 * stack overflow (if one item is contained in itself)
	 */
	public int getNumberOfContainedItems() {
		int numContainedItems = 0;

		// count all
		for (RPObject object : this) {
			// this is the item
			numContainedItems++;

			// all all items inside this one
			for (RPSlot slot : object.slots()) {
				numContainedItems += slot.getNumberOfContainedItems();
			}
		}

		return numContainedItems;
	}

	/** Return the number of elements in the slot */
	public int size() {
		return objects.size();
	}

	public void setCapacity(int capacity) {
		this.capacity = (byte) capacity;
	}

	public byte getCapacity() {
		return capacity;
	}

	public boolean isFull() {
		return size() == capacity;
	}

	/** Iterate over the objects of the slot */
	public Iterator<RPObject> iterator() {
		return Collections.unmodifiableList(objects).iterator();
	}

	/** Returns true if both objects are equal */
	@Override
	public boolean equals(Object object) {
		if(object instanceof RPSlot) {
		  RPSlot slot = (RPSlot) object;
		  return name.equals(slot.name) && objects.equals(slot.objects);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode() + objects.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append(super.toString()).append(
				" named(" + name + ") with capacity(" + capacity + ") [");

		for (RPObject object : objects) {
			str.append(object.toString());
		}
		str.append("]");
		return str.toString();
	}

	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	public void writeObject(marauroa.common.net.OutputSerializer out,
			DetailLevel level) throws java.io.IOException {
		short code = -1;

		try {
			RPClass rpClass = owner.getRPClass();
			code = rpClass.getRPSlotCode(name);
		} catch (RPClass.SyntaxException e) {
			logger.error("cannot writeObject, RPSlot [" + name + "] not found",
					e);
			code = -1;
		}

		if (level == DetailLevel.FULL) {
			// We want to ensure that attribute text is stored.
			code = -1;
		}

		out.write(code);

		if (code == -1) {
			out.write(name);
		}

		out.write(capacity);
		out.write(objects.size());
		for (RPObject object : objects) {
			object.writeObject(out, level);
		}
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws java.io.IOException, java.lang.ClassNotFoundException {
		short code = in.readShort();
		if (code == -1) {
			name = in.readString();
		} else {
			RPClass rpClass = owner.getRPClass();
			name = rpClass.getRPSlotName(code);
		}

		capacity = in.readByte();
		if (capacity > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + capacity
					+ " size");
		}

		int size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + size
					+ " size");
		}
		objects.clear();
		for (int i = 0; i < size; ++i) {
			objects.add((RPObject) in.readObject(new RPObject()));
		}
	}
}
