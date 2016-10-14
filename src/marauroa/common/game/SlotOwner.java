/***************************************************************************
 *                   (C) Copyright 2003-2016 - Marauroa                    *
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * interface for RP-classes that own slots
 *
 * @author hendrik
 */
public abstract class SlotOwner extends Attributes {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(SlotOwner.class);

	/** an empty list */
	private static final List<RPSlot> EMPTY = Collections.unmodifiableList(new ArrayList<RPSlot>());


	/** a list of slots that this object contains */
	protected List<RPSlot> slots;

	/**
	 * Keep track of the lastest assigned id for any object added to the slot of
	 * this object or any object that is contained by this object.
	 */
	private int lastAssignedID;


	/**
	 * creates a new SlowOwner
	 *
	 * @param rpclass RPClass definition
	 */
	public SlotOwner(RPClass rpclass) {
		super(rpclass);
	}


	@Override
	public Object fill(Attributes attr) {
		Object res = super.fill(attr);
		slots = null;
		if (attr instanceof SlotOwner) {
			SlotOwner slotOwner = (SlotOwner) attr;
			lastAssignedID = slotOwner.lastAssignedID;
			if (slotOwner.slots != null) {
				slots = new LinkedList<RPSlot>();
				for (RPSlot slot : slotOwner.slots) {
					RPSlot added = (RPSlot) slot.clone();
					added.setOwner(this);
					slots.add(added);
				}
			}
		}
		return res;
	}



	/**
	 * This method returns true if the object has that slot
	 *
	 * @param name
	 *            the name of the slot
	 * @return true if slot exists or false otherwise
	 */
	public boolean hasSlot(String name) {
		if (slots == null) {
			return false;
		}
		for (RPSlot slot : slots) {
			if (slot.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method add the slot to the object
	 *
	 * @param name
	 *            the RPSlot name to be added
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	/**
	 * This method add the slot to the object
	 *
	 * @param name
	 *            the RPSlot name to be added
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	public void addSlot(String name) throws SlotAlreadyAddedException {
		if (hasSlot(name)) {
			throw new SlotAlreadyAddedException(name);
		}

		RPSlot slot = new RPSlot(name);

		/** First we set the slot owner, so that slot can get access to RPClass */
		slot.setOwner(this);
		if (slots == null) {
			slots = new LinkedList<RPSlot>();
		}
		slots.add(slot);
	}

	/**
	 * This method add the slot to the object
	 *
	 * @param slot
	 *            the RPSlot to be added
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	public void addSlot(RPSlot slot) throws SlotAlreadyAddedException {
		if (hasSlot(slot.getName())) {
			throw new SlotAlreadyAddedException(slot.getName());
		}

		/* First we set the slot owner, so that slot can get access to RPClass */
		slot.setOwner(this);
		if (slots == null) {
			slots = new LinkedList<RPSlot>();
		}
		slots.add(slot);

		/* Now we make sure everyRPObject inside the added slot gets a proper id */
		for(RPObject object: slot) {
			assignSlotID(object);
			object.setContainer(this, slot);
		}
	}

	/**
	 * This method is used to remove an slot of the object
	 *
	 * @param name
	 *            the name of the slot
	 * @return the removed slot if it is found or null if it is not found.
	 */

	/**
	 * This method is used to remove an slot of the object
	 *
	 * @param name
	 *            the name of the slot
	 * @return the removed slot if it is found or null if it is not found.
	 */
	public RPSlot removeSlot(String name) {
		if (slots == null) {
			return null;
		}
		for (Iterator<RPSlot> it = slots.iterator(); it.hasNext();) {
			RPSlot slot = it.next();
			if (name.equals(slot.getName())) {
				it.remove();
				return slot;
			}
		}
		return null;
	}

	/**
	 * This method returns a slot whose name is name
	 *
	 * @param name
	 *            the name of the slot
	 * @return the slot or null if the slot is not found
	 */
	public RPSlot getSlot(String name) {
		if (slots == null) {
			return null;
		}
		for (RPSlot slot : slots) {
			if (name.equals(slot.getName())) {
				return slot;
			}
		}

		return null;
	}

	/**
	 * Returns a iterator over the slots
	 *
	 * @return an iterator over the slots
	 */
	public Iterator<RPSlot> slotsIterator() {
		if (slots == null) {
			return EMPTY.iterator();
		}
		return slots.iterator();
	}

	/**
	 * Returns an unmodifiable list of the slots
	 *
	 * @return a list of the slots
	 */
	public List<RPSlot> slots() {
		if (slots == null) {
			return EMPTY;
		}
		return Collections.unmodifiableList(slots);
	}


	/**
	 * Assign a valid id for a object to be added to a slot. The id is assigned
	 * by the base object that contains all.
	 *
	 * @param object
	 *            object to be added to a slot
	 */
	void assignSlotID(RPObject object) {
		if (getContainerOwner() != null) {
			getContainerOwner().assignSlotID(object);
		} else {
			object.put("id", lastAssignedID++);

			// If object has zoneid we remove as it is useless inside a slot.
			if (object.has("zoneid")) {
				object.remove("zoneid");
			}
		}
	}

	/**
	 * Mark an ID as used for slot assignments so that it won't be used again.
	 * @param id
	 *	An ID.
	 */
	void usedSlotID(int id) {
		if (getContainerOwner() != null) {
			getContainerOwner().usedSlotID(id);
		} else {
			if(id >= lastAssignedID) {
				logger.debug("Reseting slot ID: " + lastAssignedID + " -> " + (id + 1));
				lastAssignedID = id + 1;
			}
		}
	}

	abstract void setContainer(SlotOwner owner, RPSlot slot);

	abstract SlotOwner getContainerOwner();

	abstract SlotOwner getContainerBaseOwner();

	protected void serializeRPSlots(OutputSerializer out,
			DetailLevel level) throws IOException {
		/*
		 * We compute the amount of slots to serialize first. We don't serialize
		 * hidden or private slots unless detail level is full.
		 */
		int size = 0;
		if (slots != null) {
			for (RPSlot slot : slots) {
				if (shouldSerialize(DefinitionClass.RPSLOT, slot.getName(), level)) {
					size++;
				}
			}

			/*
			 * Now write it.
			 */
			out.write(size);
			for (RPSlot slot : slots) {
				Definition def = getRPClass().getDefinition(DefinitionClass.RPSLOT, slot.getName());

				if (shouldSerialize(def, level)) {
					slot.writeObject(out, level);
				}
			}
		} else {
			out.write(0);
		}
	}


	/**
	 * This method serialize the object with the given level of detail.
	 *
	 * @param out
	 *            the output buffer
	 * @param level
	 *            the level of Detail
	 */
	@Override
	public void writeToJson(StringBuilder out, DetailLevel level) {
		super.writeToJson(out, level);
		if (slots == null || slots.isEmpty()) {
			return;
		}

		boolean first = true;
		out.append(",\"s\":{");
		for (RPSlot slot : slots) {
			if (slot.isEmpty()) {
				continue;
			}
			Definition def = getRPClass().getDefinition(DefinitionClass.RPSLOT, slot.getName());
			if (!shouldSerialize(def, level)) {
				continue;
			}

			if (first) {
				first = false;
			} else {
				out.append(",");
			}
			slot.writeToJson(out, level);
		}
		out.append("}");
	}

	protected void deserializeRPSlots(InputSerializer in)
			throws IOException {
		/*
		 * First we load slots
		 */
		int size = in.readInt();
		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + String.valueOf(size) + " size");
		}

		if (size > 0) {
			slots = new LinkedList<RPSlot>();

			for (int i = 0; i < size; ++i) {
				RPSlot slot = new RPSlot();
				slot.setOwner(this);
				slot = (RPSlot) in.readObject(slot);
				slots.add(slot);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder tmp = new StringBuilder();
		tmp.append(super.toString());

		if (slots != null) {
			tmp.append(" and RPSlots ");
			for (RPSlot slot : slots) {
				tmp.append("[" + slot.toString() + "]");
			}
		}
		return tmp.toString();
	}

}
