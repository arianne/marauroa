/* $Id: RPSlot.java,v 1.35 2007/02/15 21:29:59 arianne_rpg Exp $ */
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

import marauroa.common.TimeoutConf;
import marauroa.common.game.Definition.DefinitionClass;

/** This class represent a slot in an object */
public class RPSlot implements marauroa.common.net.Serializable, Iterable<RPObject> {
	/** Name of the slot */
	private String name;

	/** This slot is linked to an object: its owner. */
	private RPObject owner;

	/** A List<RPObject> of objects */
	private List<RPObject> objects;

	/** The maximum amount of objects that we can store at this slot */
	private byte capacity;

	/** Stores added objects for delta^2 algorithm */
	private List<RPObject> added;

	/** Stores deleted objects for delta^2 algorithm */
	private List<RPObject> deleted;

	/** 
	 * Constructor.
	 *
	 */
	public RPSlot() {
		name = "";
		owner = null;
		capacity=-1;

		objects = new LinkedList<RPObject>();
		added = new LinkedList<RPObject>();
		deleted = new LinkedList<RPObject>();
	}

	/**
	 * Constructor 
	 * @param name name of the slot
	 */
	public RPSlot(String name) {
		this();
		this.name = name;
	}

	/** 
	 * This method sets the owner of the slot.
	 * Owner is used for having access to RPClass.
	 * 
	 * @param object sets the object that owns this slot. 
	 */
	void setOwner(RPObject object) {
		owner = object;
	}

	/** 
	 * This method returns the owner of the object
	 * @return the owner of the slot
	 */
	RPObject getOwner() {
		return owner;
	}

	/** 
	 * Sets the name of the slot
	 * @param name the name of the slot. 
	 */
	public void setName(String name) {
		this.name = name;
	}

	/** 
	 * Get the name of the slot 
	 * @return the name of the object. 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Assigns a valid unique id for this object inside the contained object.
	 * This assigned id will lack of zoneid attribute.
	 * @param object object to assign a valid unique id.
	 */
	public void assignValidID(RPObject object) {
		//TODO: Code it.
	}

	/** 
	 * Add an object to the slot, but object previously should have a valid id by calling assignValidID
	 * @param object the object to add to this slot.
	 * @throws SlotIsFullException if there is no more room at the slot. 
	 */
	public void add(RPObject object) {
		if (isFull()) {
			throw new SlotIsFullException(name);
		}

		// Notify about the addition of the object 
		added.add(object);
		// If the object is on deleted list, remove from there.
		deleted.remove(object);

		/*
		 * We set the container on object so that it can later do queries on the tree.
		 */
		object.setContainer(owner, this);
		objects.add(object);
	}

	/** 
	 * Gets the object from the slot
	 * @param id the object id. Note that only object_id field is relevant.
	 * @return the object or null if it is not found. 
	 */
	public RPObject get(RPObject.ID id) {
		int oid=id.getObjectID();
		
		for (RPObject object : objects) {
			/* We compare only the id, as the zone is really irrelevant
			 * in a contained object */
			if (object.getID().getObjectID() == oid) {
				return object;
			}
		}

		return null;
	}

	/** 
	 * Gets the first object from the slot.
	 * @return the first object of the slot or null if it is empty.  
	 */
	public RPObject getFirst() {
		if(objects.isEmpty()) {
			return null;
		}

		return objects.get(0);
	}

	/** 
	 * This method removes the object from the slot.
	 * When an object is removed from the slot, its contained information is set to null.
	 * @param id the object id. Note that only object_id field is relevant.
	 * @return the object or null if it is not found. 
	 */
	public RPObject remove(RPObject.ID id) {
		Iterator<RPObject> it = objects.iterator();
		int oid=id.getObjectID();

		while (it.hasNext()) {
			RPObject object = it.next();

			/** We compare only the id, as the zone is really irrelevant */
			if (object.getID().getObjectID() == oid) {
				/*
				 * HACK: This is a hack to avoid a problem that happens when
				 * on the same turn an object is added and deleted, causing
				 * the client to confuse.
				 */
				boolean found_in_added_list = false;
				Iterator<RPObject> added_it = added.iterator();
				while (!found_in_added_list && added_it.hasNext()) {
					RPObject added_object = added_it.next();
					if (added_object.getID().getObjectID() == oid) {
						added_it.remove();
						found_in_added_list = true;
					}
				}

				/*
				 * If it was added and it is now deleted on the same turn.
				 * Simply ignore the delta^2 information.
				 */
				if (!found_in_added_list) {
					deleted.add(new RPObject(new RPObject.ID(object)));
				}

				it.remove();

				object.setContainer(null, null);

				return object;
			}
		}

		return null;

	}

	/** 
	 * This method empty the slot by removing all the objects inside.
	 */
	public void clear() {
		for (RPObject object : objects) {
			deleted.add(new RPObject(new RPObject.ID(object)));
			object.setContainer(null, null);
		}

		added.clear();
		objects.clear();
	}

	/** 
	 * This method returns true if the slot has the object whose id is id
	 * @param id the object id. Note that only object_id field is relevant.
	 * @return true if it is found or false otherwise.
	 */
	public boolean has(RPObject.ID id) {
		int oid=id.getObjectID();
		
		for (RPObject object : objects) {
			// compare only the id, as the zone is not used for slots
			if (oid == object.getID().getObjectID()) {
				return true;
			}
		}
		return false;
	}

	/** 
	 * Traverses up the container tree to see if the slot is owned by id object or by one of its parents
	 * @param id the object id. Note that only object_id field is relevant.
	 * @return true if this slot is owned (at any depth) by id or false otherwise. 
	 */
	public boolean hasAsParent(RPObject.ID id) {
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
	}

	/** 
	 * Return the number of elements in the slot
	 * @return the number of elements in the slot
	 */
	public int size() {
		return objects.size();
	}

	/**
	 * Returns the maximum amount of objects that can be stored at the slot.
	 * When there is no limit we use the -1 value.
	 * 
	 * @return the maximum amount of objects that can be stored at the slot.
	 */
	public byte getCapacity() {
		if(capacity==-1) {
			capacity=owner.getRPClass().getDefinition(DefinitionClass.RPSLOT, name).getCapacity();
		}
		
		return capacity;
	}

	/**
	 * Returns true if the slot is full.
	 * @return true if the slot is full.
	 */
	public boolean isFull() {
		return size() == capacity;
	}

	/** 
	 * Iterate over the objects of the slot.
	 * We disallow removing objects from the iterator to avoid breaking delta^2 algorithm
	 * @return an unmodifiable iterator object the objects. 
	 */
	public Iterator<RPObject> iterator() {
		return Collections.unmodifiableList(objects).iterator();
	}

	/** 
	 * Returns true if both objects are equal
	 * @return true if both objects are equal
	 */
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
		return name.hashCode() * objects.hashCode();
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

		RPClass rpClass = owner.getRPClass();
		try {
			code=rpClass.getCode(DefinitionClass.RPSLOT, name);
		} catch(SyntaxException e) {
			code=-1;
		}

		if (level == DetailLevel.FULL) {
			// We want to ensure that attribute text is stored.
			code = -1;
		}

		out.write(code);

		if (code == -1) {
			out.write(name);
		}
		
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
			name=rpClass.getName(DefinitionClass.RPSLOT, code);
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


	/** 
	 * This method create a copy of the slot
	 * @return a depth copy of the object.  
	 */
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

}
