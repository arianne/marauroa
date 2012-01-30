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

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.game.Definition.DefinitionClass;

/**
 * This class represent a slot in an object
 */
public class RPSlot implements marauroa.common.net.Serializable, Iterable<RPObject>, Cloneable {
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPObject.class);

	/** Name of the slot */
	private String name;

	/** This slot is linked to an owner. */
	private SlotOwner owner;

	/** A List<RPObject> of objects */
	private LinkedRPObjectList objects;

	/** The maximum amount of objects that we can store at this slot */
	private int capacity;

	/** Stores added objects for delta^2 algorithm */
	private LinkedRPObjectList added;

	/** Stores deleted objects for delta^2 algorithm */
	private LinkedRPObjectList deleted;

	/**
	 * Constructor for deserialization. Please use {@link RPSlot#RPSlot(String)}. 
	 */
	public RPSlot() {
		name = null;
		owner = null;
		capacity = -1;

		objects = new LinkedRPObjectList();
		added = new LinkedRPObjectList();
		deleted = new LinkedRPObjectList();
	}

	/**
	 * Constructor
	 *
	 * @param name
	 *            name of the slot
	 */
	public RPSlot(String name) {
		this();
		this.name = name;
	}

	/**
	 * This method sets the owner of the slot. Owner is used for having access
	 * to RPClass.
	 *
	 * @param owner
	 *            sets owner of this slot.
	 */
	void setOwner(SlotOwner owner) {
		this.owner = owner;

		/*
		 * Compute now the capacity of the slot
		 */
		if (name != null) {
			capacity = owner.getRPClass().getDefinition(DefinitionClass.RPSLOT, name).getCapacity();
		}
	}

	/**
	 * This method returns the owner of the object
	 *
	 * @return the owner of the slot
	 */
	protected SlotOwner getOwner() {
		return owner;
	}

	/**
	 * Sets the name of the slot
	 *
	 * @param name
	 *            the name of the slot.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the slot
	 *
	 * @return the name of the object.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Add an object to the slot. It calls assignSlotID for the object to assign
	 * it a valid unique id for the object inside the container.
	 *
	 * @param object
	 *            the object to add to this slot.
	 * @return the id assigned to the object
	 * @throws SlotIsFullException
	 *             if there is no more room at the slot.
	 * @throws NullPointerException
	 *             if Owner is null
	 */
	public int add(RPObject object) {
		return add(object, true);
	}

	/**
	 * adds an object to a slot preserving its it.
	 * Note: In most cases you want to assign a new id.
	 *
	 * @param object RPObject to add
	 * @return the id of the object
	 */
	public int addPreservingId(RPObject object) {
		object.resetAddedAndDeleted();
		return add(object, false);
	}

	/**
	 * adds an object to a slot preserving its it.
	 * Note: In most cases you want to assign a new id.
	 *
	 * @param object RPObject to add
	 * @param assignId true to assign a new, conflict free ID.
	 * @return the id of the object
	 */
	protected int add(RPObject object, boolean assignId) {
		if (isFull()) {
			throw new SlotIsFullException(name);
		}

		if (assignId) {
			owner.assignSlotID(object);
		} else {
			// Ensure this ID doesn't get re-used
			if (object.has("id")) {
				owner.usedSlotID(object.getInt("id"));
			}
		}

		// Notify about the addition of the object
		added.add(object);
		// If the object is on deleted list, remove from there.
		deleted.remove(object);

		/*
		 * We set the container on object so that it can later do queries on the
		 * tree.
		 */
		object.setContainer(owner, this);
		objects.add(object);

		/*
		 * When object is added to slot we reset its added and delete
		 * attributes, slots and events.
		 * But only in the case it is an external addition, we don't want to break Delta^2 process.
		 */
		if (assignId) {
			object.resetAddedAndDeleted();
		}

		return object.getInt("id");
	}

	/**
	 * Gets the object from the slot
	 *
	 * @param id
	 *            the object id. Note that only object_id field is relevant.
	 * @return the object or null if it is not found.
	 */
	public RPObject get(RPObject.ID id) {
		return objects.getByIDIgnoringZone(id);
	}

	/**
	 * Gets the first object from the slot.
	 *
	 * @return the first object of the slot or null if it is empty.
	 */
	public RPObject getFirst() {
		if (objects.isEmpty()) {
			return null;
		}

		return objects.get(0);
	}

	/**
	 * This method removes the object from the slot. When an object is removed
	 * from the slot, its contained information is set to null.
	 *
	 * @param id
	 *            the object id. Note that only object_id field is relevant.
	 * @return the object or null if it is not found.
	 */
	public RPObject remove(RPObject.ID id) {
		if (id == null) return null;
		int oid = id.getObjectID();

		Iterator<RPObject> it = objects.iterator();
		while (it.hasNext()) {
			RPObject object = it.next();

			// We compare only the id, as the zone is really irrelevant
			if (object.getID().getObjectID() == oid) {
				prepareRemove(object);
				it.remove();
				return object;
			}
		}

		return null;

	}

	/**
	 * Preparse the removal of an RPObject without actually removing it from
	 * the objects list. This is within the responsibilities of the caller
	 * to enabling calling this method within an interation loop.
	 *
	 * @param object to remove.
	 */
	private void prepareRemove(RPObject object) {
		/*
		 * HACK: This is a hack to avoid a problem that happens when on
		 * the same turn an object is added and deleted, causing the
		 * client to confuse.
		 */
		RPObject fromAddedList = added.removeByIDIgnoringZone(object.getID());

		/*
		 * If it was added and it is now deleted on the same turn.
		 * Simply ignore the delta^2 information.
		 */
		if (fromAddedList == null) {
			/*
			 * Instead of adding the full object we are interested in
			 * adding only the id.
			 */
			RPObject del = new RPObject();
			del.setRPClass(object.getRPClass());
			del.put("id", object.get("id"));

			deleted.add(del);
		}
		object.setContainer(null, null);
	}

	/**
	 * This method empty the slot by removing all the objects inside.
	 */
	public void clear() {
		Iterator<RPObject> it = objects.iterator();
		while (it.hasNext()) {
			RPObject object = it.next();

			prepareRemove(object);
			it.remove();
		}

		// this should never happen
		if (!added.isEmpty()) {
			throw new IllegalStateException("added list not empty after cleaing rpslot: " + toString());
		}
	}

	/**
	 * This method returns true if the slot has the object whose id is id
	 *
	 * @param id
	 *            the object id. Note that only object_id field is relevant.
	 * @return true if it is found or false otherwise.
	 */
	public boolean has(RPObject.ID id) {
		return objects.hasByIDIgnoringZone(id);
	}

	
	/**
	 * Traverses up the container tree to see if the slot is owned by object
	 * or by one of its parents
	 *
	 * @param object
	 *            the object id. Note that only object_id field is relevant.
	 * @return true if this slot is owned (at any depth) by id or false
	 *         otherwise.
	 */
	// This signature with an RPObject as parameter is here for binary 
	// compatibilty. Changing the parameter to the parent type SlotOwner
	// is an incompatible change at binary level (although it is source
	// compatible)
	public boolean hasAsAncestor(RPObject object) {
		return hasAsAncestor((SlotOwner) object);
	}
	
	/**
	 * Traverses up the container tree to see if the slot is owned by object
	 * or by one of its parents
	 *
	 * @param object
	 *            the object id. Note that only object_id field is relevant.
	 * @return true if this slot is owned (at any depth) by id or false
	 *         otherwise.
	 */
	public boolean hasAsAncestor(SlotOwner object) {
		SlotOwner owner = getOwner();
		// traverse the owner tree
		while (owner != null) {
			// NOTE: We compare pointers.
			if (owner == object) {
				return true;
			}
			owner = owner.getContainerOwner();
		}
		return false;
	}

	/**
	 * Return the number of elements in the slot
	 *
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
	public int getCapacity() {
		return capacity;
	}
	
	/**
	 * Set the capacity of the slot. By default the value from the RPClass
	 * definition is used. It is the responsibility of the caller to ensure sane
	 * behavior if the capacity of a non-empty slot is modified. Also, since
	 * the slot size is not serialized, the client will always use the value
	 * from the RPClass. <em>Therefore, do not use this method to increase the
	 * capacity, because the client will not be able cope with slots that have
	 * more objects than the capacity defined in the RPClass.</em>
	 * 
	 * @param capacity new capacity
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Returns true if the slot is full.
	 *
	 * @return true if the slot is full.
	 */
	public boolean isFull() {
		return size() == capacity;
	}

	/**
	 * Iterate over the objects of the slot. We disallow removing objects from
	 * the iterator to avoid breaking delta^2 algorithm
	 *
	 * @return an unmodifiable iterator object the objects.
	 */
	public Iterator<RPObject> iterator() {
		return Collections.unmodifiableList(objects).iterator();
	}

	/**
	 * Returns true if both objects are equal
	 *
	 * @return true if both objects are equal
	 */
	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object instanceof RPSlot) {
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
		str.append(super.toString());
		str.append(" named(" + name + ") with capacity(" + capacity + ")");
		str.append(objects);
		return str.toString();
	}

	/**
	 * This method serialize the object with the default level of detail, that
	 * removes private and hidden attributes
	 *
	 * @param out
	 *            the output serializer
	 */
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	/**
	 * This method serialize the object with the given level of detail.
	 *
	 * @param out
	 *            the output serializer
	 * @param level
	 *            the level of Detail
	 * @throws IOException
	 *            in case of an IO error
	 */
	public void writeObject(marauroa.common.net.OutputSerializer out, DetailLevel level)
	        throws java.io.IOException {
		RPClass rpClass = owner.getRPClass();

		Definition def = rpClass.getDefinition(DefinitionClass.RPSLOT, name);
		short code = def.getCode();

		if (level == DetailLevel.FULL) {
			// We want to ensure that attribute text is stored.
			code = -1;
		}

		out.write(code);

		if (code == -1) {
			out.write(name);
		}

		int size = 0;
		/*
		 * Count the amount of non hidden objects.
		 */
		for (RPObject object : objects) {
			if (!object.isHidden()) {
				size++;
			}
		}

		if (level == DetailLevel.FULL) {
			size = objects.size();
		}

		out.write(size);

		for (RPObject object : objects) {
			if (level == DetailLevel.FULL || !object.isHidden()) {
				object.writeObject(out, level);
			}
		}
	}

	/**
	 * Fills this object with the data that has been serialized.
	 */
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException {
		short code = in.readShort();
		if (code == -1) {
			name = in.readString();
		} else {
			RPClass rpClass = owner.getRPClass();
			name = rpClass.getName(DefinitionClass.RPSLOT, code);
		}

		capacity = owner.getRPClass().getDefinition(DefinitionClass.RPSLOT, name).getCapacity();

		int size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + size + " size");
		}

		objects.clear();
		for (int i = 0; i < size; ++i) {
			final RPObject readObject = (RPObject) in.readObject(new RPObject());
			readObject.setContainer(owner, this);
			objects.add(readObject);
		}
	}

	/**
	 * This method create a copy of the slot
	 *
	 * @return a depth copy of the object.
	 */
	@Override
	public Object clone() {
		RPSlot slot = null;
		try {
			slot = (RPSlot) super.clone();
		} catch (CloneNotSupportedException e) {
			logger.error(e, e);
			return null;
		}

		slot.name = name;
		slot.owner = owner;
		slot.capacity = capacity;

		slot.objects = new LinkedRPObjectList();
		for (RPObject object : objects) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			slot.objects.addTrusted(copied);
		}

		slot.added = new LinkedRPObjectList();
		for (RPObject object : added) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			slot.added.addTrusted(copied);
		}

		slot.deleted = new LinkedRPObjectList();
		for (RPObject object : deleted) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			slot.deleted.addTrusted(copied);
		}

		return slot;
	}

	/**
	 * Clear stored delta^2 information.
	 *
	 */
	public void resetAddedAndDeletedRPObjects() {
		added.clear();
		deleted.clear();
	}

	/**
	 * Copy to given slot the objects added. It does a depth copy of the
	 * objects.
	 *
	 * @param slot
	 *            the slot to copy added objects.
	 * @return true if there is any object added.
	 */
	public boolean setAddedRPObject(RPSlot slot) {
		boolean changes = false;
		for (RPObject object : slot.added) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			objects.add(copied);
			changes = true;
		}
		return changes;
	}

	/**
	 * Copy to given slot the objects deleted. It does a depth copy of the
	 * objects.
	 *
	 * @param slot
	 *            the slot to copy added objects.
	 * @return true if there is any object added.
	 */
	public boolean setDeletedRPObject(RPSlot slot) {
		boolean changes = false;
		for (RPObject object : slot.deleted) {
			RPObject copied = (RPObject) object.clone();
			copied.setContainer(owner, slot);
			objects.add(copied);
			changes = true;
		}
		return changes;
	}

	/**
	 * Removes the visible objects from this slot. It iterates through the slots
	 * to remove the attributes too of the contained objects if they are empty.
	 * @param sync keep the structure intact, by not removing empty slots and links.
	 */
	public void clearVisible(boolean sync) {
		Definition def = owner.getRPClass().getDefinition(DefinitionClass.RPSLOT, name);

		if (def.isVisible()) {
			List<RPObject> idtoremove = new LinkedList<RPObject>();
			for (RPObject object : objects) {
				object.clearVisible(sync);

				/* If object is empty remove it. */
				if (object.size() == 1) {
					/*
					 * If object size is one means only id remains. Objects
					 * inside the slot should not contain any other special
					 * attribute. If only id remains, we can remove this object
					 * from the slot.
					 */

					idtoremove.add(object);
				}
			}

			for (RPObject obj : idtoremove) {
				/*
				 * Remove the object from objects, added and deleted lists.
				 */
				objects.remove(obj);
				added.remove(obj);
				deleted.removeByIDIgnoringZone(obj.getID());
			}
		}

	}
}
