/* $Id: RPObject.java,v 1.21 2006/08/20 15:40:08 wikipedian Exp $ */
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

/** This class implements an Object. Please refer to "Objects Explained" document */
public class RPObject extends Attributes {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RPObject.class);

	private List<RPSlot> added;

	private List<RPSlot> deleted;

	/** a List<RPSlot> of slots */
	private List<RPSlot> slots;

	private RPObject container;

	private RPSlot containerSlot;

	public final static ID INVALID_ID = new ID(-1, "");

	/** Constructor */
	public RPObject() {
		super(RPClass.getBaseRPObjectDefault());

		slots = new LinkedList<RPSlot>();
		added = new LinkedList<RPSlot>();
		deleted = new LinkedList<RPSlot>();
		container = null;
	}

	public RPObject(RPObject object) {
		this();
		fill(object);
	}

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the id of the object
	 */
	public RPObject(ID id) {
		this();
		setID(id);
	}

	private void initialize() {
	}

	/** Returns an ID object representing the id of this object */
	public RPObject.ID getID() throws AttributeNotFoundException {
		return new ID(this);
	}

	public void setID(RPObject.ID id) {
		put("id", id.getObjectID());
		put("zoneid", id.getZoneID());
	}

	/** Returns true if the object is empty */
	public boolean isEmpty() {
		return super.isEmpty() && slots.isEmpty();
	}

	public boolean isContained() {
		return container != null;
	}

	public void setContainer(RPObject object, RPSlot slot) // Package only
															// access ... if
															// only Java would
															// have friendly
															// declarations...
	{
		container = object;
		containerSlot = slot;
	}

	public RPObject getContainer() {
		return container;
	}

	public RPSlot getContainerSlot() {
		return containerSlot;
	}

	public void resetAddedAndDeleted() {
		resetAddedAndDeletedAttributes();
		resetAddedAndDeletedRPSlot();
	}

	public void resetAddedAndDeletedRPSlot() {
		for (RPSlot slot : slots) {
			slot.resetAddedAndDeletedRPObjects();
			for (RPObject object : slot) {
				object.resetAddedAndDeleted();
			}
		}

		added.clear();
		deleted.clear();
	}

	public void setAddedRPSlot(RPObject object) {
		for (RPSlot slot : object.added) {
			RPSlot copied = (RPSlot) slot.clone();
			copied.setOwner(this);
			slots.add(copied);
		}
	}

	public void setDeletedRPSlot(RPObject object) {
		for (RPSlot slot : object.deleted) {
			RPSlot copied = new RPSlot(slot.getName());
			copied.setOwner(this);
			slots.add(copied);
		}
	}

	/**
	 * This method returns true if the object has that slot
	 * 
	 * @param name
	 *            the name of the slot
	 * @return true if slot exists or false otherwise
	 */
	public boolean hasSlot(String name) {
		for (RPSlot slot : slots) {
			if (name.equals(slot.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method add the slot to the object
	 * 
	 * @param slot
	 *            the RPSlot object
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	public void addSlot(RPSlot slot) throws SlotAlreadyAddedException {
		if (!hasSlot(slot.getName())) {
			slot.setOwner(this);

			slot.setCapacity(getRPClass().getRPSlotCapacity(slot.getName()));

			added.add(slot);
			slots.add(slot);
		} else {
			throw new SlotAlreadyAddedException(slot.getName());
		}
	}

	/** This method is used to remove an slot of the object */
	public void removeSlot(String name) throws NoSlotFoundException {
		if (hasSlot(name)) {
			for (Iterator<RPSlot> it = slots.iterator(); it.hasNext();) {
				RPSlot slot = it.next();
				if (name.equals(slot.getName())) {
					deleted.add(slot);
					it.remove();
					return;
				}
			}
		} else {
			throw new NoSlotFoundException(name);
		}
	}

	/**
	 * This method returns a slot whose name is name
	 * 
	 * @param name
	 *            the name of the slot
	 * @return the slot
	 * @throws NoSlotFoundException
	 *             if the slot is not found
	 */
	public RPSlot getSlot(String name) throws NoSlotFoundException {
		for (RPSlot slot : slots) {
			if (name.equals(slot.getName())) {
				return slot;
			}
		}
		throw new NoSlotFoundException(name);
	}

	/**
	 * Returns a iterator over the slots
	 * 
	 * @return an iterator over the slots
	 */
	public Iterator<RPSlot> slotsIterator() {
		return slots.iterator();
	}

	/**
	 * Returns an unmodifyable list of the slots
	 * 
	 * @return a list of the slots
	 */
	public List<RPSlot> slots() {
		return Collections.unmodifiableList(slots);
	}

	/**
	 * This method returns a String that represent the object
	 * 
	 * @return a string representing the object.
	 */
	public String toString() {
		StringBuffer tmp = new StringBuffer("RPObject with ");

		tmp.append(super.toString());
		tmp.append(" and RPSlots ");

		for (RPSlot slot : slots) {
			tmp.append("[" + slot.toString() + "]");
		}
		return tmp.toString();
	}

	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	public void writeObject(marauroa.common.net.OutputSerializer out,
			DetailLevel level) throws java.io.IOException {
		super.writeObject(out, level);

		RPClass rpClass = getRPClass();

		int size = slots.size();
		for (RPSlot slot : slots) {
			if (level == DetailLevel.NORMAL
					&& (rpClass.isRPSlotVisible(slot.getName()) == false)) {
				// If this attribute is Hidden or private and full data is false
				--size;
			} else if (level != DetailLevel.FULL
					&& rpClass.isRPSlotHidden(slot.getName())) {
				// If this attribute is Hidden and full data is true.
				// This way we hide some attribute to player.
				--size;
			}
		}

		out.write(size);
		for (RPSlot slot : slots) {
			if ((level == DetailLevel.PRIVATE && !rpClass.isRPSlotHidden(slot
					.getName()))
					|| (rpClass.isRPSlotVisible(slot.getName()))
					|| (level == DetailLevel.FULL)) {
				slot.writeObject(out, level);
			}
		}
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws java.io.IOException, java.lang.ClassNotFoundException {
		super.readObject(in);

		int size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of "
					+ String.valueOf(size) + " size");
		}

		slots = new LinkedList<RPSlot>();

		for (int i = 0; i < size; ++i) {
			RPSlot slot = new RPSlot();
			slot.setOwner(this);
			slot = (RPSlot) in.readObject(slot);
			slot.setCapacity(getRPClass().getRPSlotCapacity(slot.getName()));
			slots.add(slot);
		}
	}

	/** Returns the size of the object */
	public int size() {
		try {
			int total = super.size();

			for (RPSlot slot : slots) {
				for (RPObject object : slot) {
					total += object.size();
				}
			}

			return total;
		} catch (Exception e) {
			return -1;
		}
	}

	public int clearVisible() {
		int i = super.clearVisible();

		for (RPSlot slot : slots) {
			if (getRPClass().isRPSlotVisible(slot.getName())) {
				LinkedList<RPObject> objectToRemove = new LinkedList<RPObject>();

				for (RPObject object : slot) {
					i += object.clearVisible();
				}
			}
		}

		return i;
	}

	// TODO: Refactor this method. Looks like it claims for bugs!"
	/** This method get the changes on added and deleted things from this object */
	public void getDifferences(RPObject oadded, RPObject odeleted)
			throws Exception {
		/** First we get differences from attributes */
		oadded.setAddedAttributes(this);
		odeleted.setDeletedAttributes(this);

		/**
		 * Now we compute differences at slots of this object. First we get the
		 * deleted slots
		 */
		odeleted.setDeletedRPSlot(this);

		for (RPSlot slot : slots) {
			/** For each one of the existing slots, add the added objects */
			RPSlot added_slot = new RPSlot(slot.getName());
			added_slot.setAddedRPObject(slot);

			if (added_slot.size() > 0 && !oadded.hasSlot(added_slot.getName())) {
				oadded.addSlot(added_slot);
			}

			/** And add also the deleted objects */
			RPSlot deleted_slot = new RPSlot(slot.getName());
			deleted_slot.setDeletedRPObject(slot);

			if (deleted_slot.size() > 0
					&& !odeleted.hasSlot(deleted_slot.getName())) {
				odeleted.addSlot(deleted_slot);
			}

			/** Now apply recursively to the existing objects in the slot */
			for (RPObject object : slot) {
				RPObject object_added = new RPObject();
				RPObject object_deleted = new RPObject();

				object.getDifferences(object_added, object_deleted);

				if (object_added.size() > 0) {
					if (!oadded.hasSlot(slot.getName())) {
						oadded.addSlot(new RPSlot(slot.getName()));
					}

					if (!oadded.getSlot(slot.getName()).has(new ID(object))) {
						object_added.put("id", object.get("id"));
						oadded.getSlot(slot.getName()).add(object_added);
					}
				}

				if (object_deleted.size() > 0) {
					if (!odeleted.hasSlot(slot.getName())) {
						odeleted.addSlot(new RPSlot(slot.getName()));
					}

					if (!odeleted.getSlot(slot.getName()).has(new ID(object))) {
						object_deleted.put("id", object.get("id"));
						odeleted.getSlot(slot.getName()).add(object_deleted);
					}
				}
			}
		}

		if (oadded.size() > 0) {
			oadded.put("id", get("id"));
		}

		if (odeleted.size() > 0) // || odeleted.slots.size()>0)
		{
			odeleted.put("id", get("id"));
		}
	}

	// TODO: Refactor this method. Looks like it claims for bugs!"
	/**
	 * This method apply the changes retrieved from getDifferences and build the
	 * updated object
	 */
	public RPObject applyDifferences(RPObject added, RPObject deleted)
			throws Exception {
		if (deleted != null) {
			for (String attrib : deleted) {
				if (!attrib.equals("id") && !attrib.equals("zoneid")) {
					remove(attrib);
				}
			}

			for (RPSlot slot : deleted.slots) {
				if (slot.size() == 0) {
					removeSlot(slot.getName());
				} else {
					/** for each of the objects, delete it */
					for (RPObject object : slot) {
						if (object.size() == 2) /** id and zoneid */
						{
							getSlot(slot.getName()).remove(new ID(object));
						} else {
							RPObject actualObject = getSlot(slot.getName())
									.get(new ID(object));

							actualObject.applyDifferences(null, object);
						}
					}
				}
			}
		}

		if (added != null) {
			for (String attrib : added) {
				put(attrib, added.get(attrib));
			}

			for (RPSlot slot : added.slots) {
				if (!hasSlot(slot.getName())) {
					addSlot(new RPSlot(slot.getName()));
				}

				/** for each of the objects, add it */
				for (RPObject object : slot) {
					if (getSlot(slot.getName()).has(new ID(object))) {
						getSlot(slot.getName()).get(new ID(object))
								.applyDifferences(object, null);
					} else {
						getSlot(slot.getName()).add(object);
					}
				}
			}
		}
		return this;
	}

	/** Create a real copy of the object */
	public Object clone() {
		RPObject object = new RPObject();

		object.fill((Attributes) this);

		object.container = container;
		object.containerSlot = containerSlot;

		for (RPSlot slot : slots) {
			RPSlot copied = (RPSlot) slot.clone();
			copied.setOwner(object);
			object.slots.add(copied);
		}

		for (RPSlot slot : added) {
			RPSlot copied = (RPSlot) slot.clone();
			copied.setOwner(object);
			object.added.add(copied);
		}

		for (RPSlot slot : deleted) {
			RPSlot copied = (RPSlot) slot.clone();
			copied.setOwner(object);
			object.deleted.add(copied);
		}

		return object;
	}

	private void fill(RPObject object) {
		super.fill((Attributes) object);

		container = object.container;
		containerSlot = object.containerSlot;

		try {
			for (RPSlot slot : object.slots) {
				addSlot((RPSlot) slot.clone());
			}
		} catch (SlotAlreadyAddedException e) {
			// Should never happen
		}
	}

	/** Returns true if two objects are exactly equal */
	public boolean equals(Object obj) {
		RPObject object = (RPObject) obj;

		return super.equals(obj) && slots.equals(object.slots);
	}

	public int hashCode() {
		try {
			return getInt("id");
		} catch (AttributeNotFoundException e) {
			return -1;
		}
	}

	/** This class stores the basic identification for a RPObject */
	public static class ID implements marauroa.common.net.Serializable {
		private int id;

		private String zoneid;

		/**
		 * Constructor
		 * 
		 * @param objectid
		 *            the object id
		 * @param zone
		 *            the zone
		 */
		public ID(int objectid, String zone) {
			this.id = objectid;
			this.zoneid = zone;
		}

		/**
		 * Constructor
		 * 
		 * @param objectid
		 *            the object id
		 * @param zoneid
		 *            the zone-id
		 */
		public ID(int objectid, IRPZone.ID zoneid) {
			this.id = objectid;
			this.zoneid = zoneid.getID();
		}

		/**
		 * Constructor
		 * 
		 * @param attr
		 *            an RPObject containing object_id attribute
		 */
		public ID(RPObject attr) throws AttributeNotFoundException {
			this.id = attr.getInt("id");
			this.zoneid = attr.get("zoneid");
		}

		/**
		 * Constructor
		 * 
		 * @param attr
		 *            an RPAction containing sourceid attribute
		 */
		public ID(RPAction attr) throws AttributeNotFoundException {
			this.id = attr.getInt("sourceid");
			this.zoneid = attr.get("zoneid");
		}

		/**
		 * This method returns the object id
		 * 
		 * @return the object id.
		 */
		public int getObjectID() {
			return id;
		}

		public String getZoneID() {
			return zoneid;
		}

		/**
		 * This method returns true of both ids are equal.
		 * 
		 * @param anotherid
		 *            another id object
		 * @return true if they are equal, or false otherwise.
		 */
		public boolean equals(Object anotherid) {
			if (anotherid != null) {
				return (id == ((RPObject.ID) anotherid).id && zoneid
						.equals(((RPObject.ID) anotherid).zoneid));
			} else {
				return false;
			}
		}

		/** We need it for HashMap */
		public int hashCode() {
			return id * 1500 + zoneid.hashCode();
		}

		/**
		 * This method returns a String that represent the object
		 * 
		 * @return a string representing the object.
		 */
		public String toString() {
			return "RPObject.ID [id=" + id + " zoneid=" + zoneid + "]";
		}

		public void writeObject(marauroa.common.net.OutputSerializer out)
				throws java.io.IOException {
			out.write(id);
			out.write(zoneid);
		}

		public void readObject(marauroa.common.net.InputSerializer in)
				throws java.io.IOException, java.lang.ClassNotFoundException {
			id = in.readInt();
			zoneid = in.readString();
		}
	}
}
