/* $Id: RPObject.java,v 1.47 2007/02/28 14:06:32 arianne_rpg Exp $ */
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

/**
 * This class implements an Object.
 * <p>
 * An object is the basic abstraction at marauroa. Players are objects, creatures are objects,
 * the maze at pacman is an object, each gladiator is an object... everything is an object.<br>
 * But don't get confused with all the object keyword usage outthere. An object is anything
 * that can be though as an object ( physical or logical thing ).
 * <p>
 * Objects are stored at IRPZones.
 * <p>
 * Objects contains:<ul>
 * <li>RPSlots
 * <li>RPEvents
 * </ul>
 */

public class RPObject extends Attributes {
	/** a list of slots that this object contains */
	private List<RPSlot> slots;
	/** a list of events that this object contains */
	private List<RPEvent> events;

	/** Which object contains this one. */
	private RPObject container;

	/** In which slot are this object contained */
	private RPSlot containerSlot;

	/** added and modified slots, used at Delta^2 */
	private List<String> added;
	/** delete slots, used at Delta^2 */
	private List<String> deleted;

	/** Defines an invalid object id */
	public final static ID INVALID_ID = new ID(-1, "");

	/** If this variable is true the object is removed from the perception send to client. */
	private boolean hidden;

	/** Defines if this object should be stored at database. */
	private boolean storable;

	/**
	 * Constructor
	 */
	public RPObject() {
		super(RPClass.getBaseRPObjectDefault());

		slots = new LinkedList<RPSlot>();
		added = new LinkedList<String>();
		deleted = new LinkedList<String>();

		events= new LinkedList<RPEvent>();

		container = null;
		containerSlot=null;

		hidden=false;
		storable=false;
	}

	/**
	 * Copy constructor
	 * @param object the object that is going to be copied.
	 */
	public RPObject(RPObject object) {
		this();

		super.fill(object);

		hidden=object.hidden;
		storable=object.storable;

		container = object.container;
		containerSlot = object.containerSlot;

		for (RPSlot slot : object.slots) {
			slots.add((RPSlot) slot.clone());
		}

		for (RPEvent event : object.events) {
			events.add((RPEvent) event.clone());
		}
	}

	/**
	 * Constructor.
	 *
	 * @param id the id of the object
	 */
	RPObject(ID id) {
		this();
		setID(id);
	}

	/**
	 * Returns an ID object representing the id of this object
	 * @return the identificator of the object
	 */
	public RPObject.ID getID() {
		return new ID(this);
	}

	/**
	 * Set the attributes that define the ID of the object
	 * @param id the object id to set for this object
	 */
	public void setID(RPObject.ID id) {
		put("id", id.getObjectID());
		put("zoneid", id.getZoneID());
	}

	/**
	 * Makes this object invisible, so it is not added in any perception.
	 * This method is not callable directly from the object once it has been added to a zone.
	 * If it is already added, this method must be called from IRPZone.hide()
	 */
	public void hide() {
		hidden=true;

		//TODO: A hidden object should be removed from the perception.
	}

	/**
	 * Makes this object visible again.
	 * This method is not callable directly from the object once it has been added to a zone.
	 * If it is already added, this method must be called from IRPZone.unhide()
	 */
	public void unhide() {
		hidden=false;

		//TODO: An object that is now unhidden should be added to the perception.
	}

	/**
	 * Return true if this object is hidden.
	 * @return true if this object is hidden.
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Define this object as storable, but it doesn't in fact store the object.
	 * The object is stored on zone.finish
	 *
	 */
	public void store() {
		storable=true;
	}

	/**
	 * Return true if the object should be stored at database.
	 * @return
	 */
	public boolean isStorable() {
		return storable;
	}

	/**
	 * Returns true if this object is contained inside another one.
	 * @return true if this object is contained inside another one.
	 */
	public boolean isContained() {
		return container != null;
	}

	/**
	 * This make this object to be contained in the slot of container.
	 * @param object the object that is going to contain this object.
	 * @param slot the slot of the object that contains this object.
	 */
	public void setContainer(RPObject object, RPSlot slot)
	{
		container = object;
		containerSlot = slot;
	}

	/**
	 * Returns the container where this object is
	 * @return the container of this object.
	 */
	public RPObject getContainer() {
		return container;
	}

	/**
	 * Returns the base container where this object is
	 * @return the base container of this object.
	 */
	public RPObject getBaseContainer() {
		if(container!=null) {
			return container.getBaseContainer();
		} else {
			return this;
		}
	}

	/**
	 * Returns the slot where this object is contained
	 * @param the slot of the object that contains this object.
	 */
	public RPSlot getContainerSlot() {
		return containerSlot;
	}

	/**
	 * Keep track of the lastest assigned id for any object added to the slot of this
	 * object or any object that is contained by this object.
	 */
	private int lastassignedID;

	/**
	 * Assign a valid id for a object to be added to a slot.
	 * The id is assigned by the base object that contains all.
	 * @param object object to be added to a slot
	 */
	void assignSlotID(RPObject object) {
		if(container!=null) {
			container.assignSlotID(object);
		} else {
			object.put("id", lastassignedID++);

			// If object has zoneid we remove as it is useless inside a slot.
			if(object.has("zoneid")) {
				object.remove("zoneid");
			}
		}
	}

	/**
	 * This method returns true if the object has that slot
	 *
	 * @param name the name of the slot
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
	 * @param slot the RPSlot object
	 * @throws SlotAlreadyAddedException if the slot already exists
	 */
	public void addSlot(String name) throws SlotAlreadyAddedException {
		if (hasSlot(name)) {
			throw new SlotAlreadyAddedException(name);
		}

		RPSlot slot=new RPSlot(name);

		/** First we set the slot owner, so that slot can get access to RPClass */
		slot.setOwner(this);
		slots.add(slot);

		/** Notify delta^2 about the addition of this slot */
		added.add(name);
	}

	/**
	 * This method is used to remove an slot of the object
	 * @param name the name of the slot
	 * @return the removed slot if it is found or null if it is not found.
	 */
	public RPSlot removeSlot(String name) {
		for (Iterator<RPSlot> it = slots.iterator(); it.hasNext();) {
			RPSlot slot = it.next();
			if (name.equals(slot.getName())) {
				// TODO: if an slot is added and deleted on the same turn it shouldn't be mention on deleted.
				/** Notify delta^2 about the removal of this slot. */
				deleted.add(name);

				/* Remove and return it */
				it.remove();
				return slot;
			}
		}

		return null;
	}

	/**
	 * This method returns a slot whose name is name
	 *
	 * @param name the name of the slot
	 * @return the slot or null if the slot is not found
	 */
	public RPSlot getSlot(String name) {
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
	 * Add an event to this object
	 * @param name the name of the event
	 * @param value its value
	 */
	public void addEvent(String name, String value) {
		events.add(new RPEvent(this, name, value));
	}

	/**
	 * Empty the list of events.
	 * This method is called at the end of each turn.
	 */
	public void clearEvents() {
		events.clear();
	}

	/**
	 * Iterate over the events list
	 * @return an iterator over the events
	 */
	public Iterator<RPEvent> eventsIterator() {
		return events.iterator();
	}

	/**
	 * Returns an unmodifyable list of the events
	 *
	 * @return a list of the evetns
	 */
	public List<RPEvent> events() {
		return Collections.unmodifiableList(events);
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		StringBuffer tmp = new StringBuffer("RPObject with ");

		tmp.append(super.toString());
		tmp.append(" and RPSlots ");

		for (RPSlot slot : slots) {
			tmp.append("[" + slot.toString() + "]");
		}

		tmp.append(" and RPEvents ");
		for (RPEvent event : events) {
			tmp.append("[" + event.toString() + "]");
		}

		return tmp.toString();
	}

	/**
	 * This method serialize the object with the default level of detail, that removes
	 *  private and hidden attributes
	 *  @param out the output serializer
	 */
	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	/**
	 * This method serialize the object with the given level of detail.
	 *  @param out the output serializer
	 *  @param level the level of Detail
	 */
	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out, DetailLevel level) throws java.io.IOException {
		super.writeObject(out, level);

		if(level==DetailLevel.FULL) {
			out.write((byte)1);
			out.write((byte)(hidden?1:0));
			out.write((byte)(storable?1:0));
		} else {
			out.write((byte)0);
		}

		/*
		 * We compute the amount of slots to serialize first.
		 * We don't serialize hidden or private slots unless detail level is full.
		 */
		int size = 0;
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
			Definition def=getRPClass().getDefinition(DefinitionClass.RPSLOT, slot.getName());

			if (shouldSerialize(def, level)) {
				slot.writeObject(out, level);
			}
		}

		/*
		 * We compute the amount of events to serialize first.
		 * We don't serialize hidden or private slots unless detail level is full.
		 */
		size = 0;
		for (RPEvent event : events) {
			if (shouldSerialize(DefinitionClass.RPEVENT, event.getName(), level)) {
				size++;
			}
		}

		/*
		 * Now write it too.
		 */
		out.write(size);
		for (RPEvent event : events) {
			Definition def=getRPClass().getDefinition(DefinitionClass.RPEVENT, event.getName());

			if (shouldSerialize(def, level)) {
				event.writeObject(out, level);
			}
		}
	}

	/**
	 * Fills this object with the data that has been serialized.
	 * @param in the input serializer
	 */
	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException {
		super.readObject(in);

		if(in.readByte()==1) {
			hidden=in.readByte()==1;
			storable=in.readByte()==1;
		}

		/*
		 * First we load slots
		 */
		int size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of "+ String.valueOf(size) + " size");
		}

		slots = new LinkedList<RPSlot>();

		for (int i = 0; i < size; ++i) {
			RPSlot slot = new RPSlot();
			slot.setOwner(this);
			slot = (RPSlot) in.readObject(slot);
			slots.add(slot);
		}

		/*
		 * And now we load events
		 */
		size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of "+ String.valueOf(size) + " size");
		}

		events = new LinkedList<RPEvent>();

		for (int i = 0; i < size; ++i) {
			RPEvent event = new RPEvent(this);
			event = (RPEvent) in.readObject(event);
			events.add(event);
		}
	}

	/**
	 * Returns true if two objects are exactly equal
	 * @param obj the object to compare with this one.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RPObject) {
			RPObject object = (RPObject) obj;
			return super.equals(obj) &&
			  slots.equals(object.slots) &&
			  events.equals(object.events);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getInt("id");
	}

	/**
	 * Returns true if the object is empty
	 * @return true if the object lacks of any attribute, slots or events.
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && slots.isEmpty() && events.isEmpty();
	}

	/**
	 * Returns the number of attributes and events this object is made of.
	 */
	@Override
	public int size() {
		try {
			int total = super.size();

			total+= events.size();

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

	/**
	 * Removes the visible attributes and events from this object.
	 * It iterates through the slots to remove the attributes too of the contained objects if they are empty.
	 */
	@Override
	public void clearVisible() {
		super.clearVisible();

		Iterator<RPEvent> eventsit=events.iterator();
		while(eventsit.hasNext()) {
			/* Iterate over events and remove all of them that are visible */
			RPEvent event=eventsit.next();
			Definition def=getRPClass().getDefinition(DefinitionClass.RPSLOT, event.getName());
			if (def.isVisible()) {
				eventsit.remove();
			}
		}

		Iterator<RPSlot> slotit=slots.iterator();
		while(slotit.hasNext()) {
			RPSlot slot=slotit.next();
			Definition def=getRPClass().getDefinition(DefinitionClass.RPSLOT, slot.getName());

			if (def.isVisible()) {
				List<RPObject.ID> idtoremove=new LinkedList<RPObject.ID>();
				for (RPObject object : slot) {
					object.clearVisible();

					/* If object is empty remove it. */
					if(object.size()==1) {
						// If object size is one means only id remains.
						// Objects inside the slot should not contain any other special attribute.
						// If only id remains, we can remove this object from the slot.
						idtoremove.add(object.getID());
					}
				}

				for(RPObject.ID id: idtoremove) {
					// Slot.remove code takes care of removing it from added and deleted list.
					slot.remove(id);
				}
			}

			/* If slot is empty remove it. */
			if(slot.size()==0) {
				slotit.remove();
			}
		}
	}

	/**
	 * Create a depth copy of the object
	 * @return a copy of this object.
	 */
	@Override
	public Object clone() {
		RPObject object = new RPObject();

		object.fill((Attributes) this);

		object.container = container;
		object.containerSlot = containerSlot;

		object.hidden=hidden;
		object.storable=storable;

		for (RPEvent event : events) {
			object.addEvent(event.getName(), event.getValue());
		}

		for (RPSlot slot : slots) {
			RPSlot copied = (RPSlot) slot.clone();
			copied.setOwner(object);
			object.slots.add(copied);
		}

		for (String slot : added) {
			object.added.add(slot);
		}

		for (String slot : deleted) {

			object.deleted.add(slot);
		}

		return object;
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
		public ID(RPObject attr) {
			this.id = attr.getInt("id");
			this.zoneid = attr.get("zoneid");
		}

		/**
		 * Constructor
		 *
		 * @param attr
		 *            an RPAction containing sourceid attribute
		 */
		public ID(RPAction attr) {
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
		@Override
		public boolean equals(Object anotherid) {
			if (anotherid != null && anotherid instanceof RPObject.ID) {
				return (id == ((RPObject.ID) anotherid).id && zoneid
						.equals(((RPObject.ID) anotherid).zoneid));
			} else {
				return false;
			}
		}

		/** We need it for HashMap */
		@Override
		public int hashCode() {
			return id;
		}

		/**
		 * This method returns a String that represent the object
		 *
		 * @return a string representing the object.
		 */
		@Override
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



	/**
	 * Clean delta^2 information about added and deleted.
	 * It is called by Marauroa, don't use :)
	 */
	public void resetAddedAndDeleted() {
		resetAddedAndDeletedAttributes();
		resetAddedAndDeletedRPSlot();
	}

	/**
	 * Clean delta^2 data in the slots.
	 * It is called by Marauroa, don't use :)
	 */
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

	/**
	 * Set added objects in slots for this object and fill object passed as param.	 *
	 * It is called by Marauroa, don't use :)
	 * @param object the object to fill with added data.
	 */
	public void setAddedRPSlot(RPObject object) {
		for (String slot : object.added) {
			addSlot(slot);
		}
	}

	/**
	 * Set deleted objects in slots for this object and fill object passed as param.	 *
	 * It is called by Marauroa, don't use :)
	 * @param object the object to fill with deleted data.
	 */
	public void setDeletedRPSlot(RPObject object) {
		for (String slot : object.deleted) {
			addSlot(slot);
		}
	}

	/**
	 * Retrieve the differences stored in this object and add them to two new objects
	 * added changes and delete changes that will contains added and modified attributes,
	 * slots and events and on the other hand deleted changes that will contain the removes
	 * slots and attributes.
	 * We don't care about RP Events because they are removed on each turn.
	 * @param addedChanges an empty object
	 * @param deletedChanges an empty object
	 */
	public void getDifferences(RPObject addedChanges, RPObject deletedChanges) {
		addedChanges.setAddedAttributes(this);
		deletedChanges.setDeletedAttributes(this);

		addedChanges.setAddedRPSlot(this);
		deletedChanges.setDeletedRPSlot(this);
	}

	// TODO: Refactor this method. Looks like it claims for bugs!"
	/** This method get the changes on added and deleted things from this object */
	public void getDifferences_old(RPObject oadded, RPObject odeleted) throws Exception {
		/* First we get differences from attributes */
		oadded.setAddedAttributes(this);
		odeleted.setDeletedAttributes(this);

		/*
		 * We add to the oadded object the events that exists.
		 */
		for(RPEvent event: events) {
			oadded.events.add((RPEvent)event.clone());
		}

		/*
		 * Now we compute differences at slots of this object. First we get the
		 * deleted slots and add them to the deleted object.
		 */
		odeleted.setDeletedRPSlot(this);

		for (RPSlot slot : slots) {
			/* For each one of the existing slots, add the added objects */
			RPSlot added_slot = new RPSlot(slot.getName());
			added_slot.setAddedRPObject(slot);

			/* We only add the object if it is not there or it is not empty. */
			if (added_slot.size() > 0 && !oadded.hasSlot(added_slot.getName())) {
				added_slot.setOwner(oadded);
				oadded.slots.add(added_slot);
			}

			/* And add also the deleted objects */
			RPSlot deleted_slot = new RPSlot(slot.getName());
			deleted_slot.setDeletedRPObject(slot);

			/* Again, we are only interested in adding the deleted slot if it is not
			 * already there or it it is not empty.
			 */
			if (deleted_slot.size() > 0 && !odeleted.hasSlot(deleted_slot.getName())) {
				deleted_slot.setOwner(odeleted);
				odeleted.slots.add(deleted_slot);
			}

			/* Now apply recursively to the existing objects in the slot */
			for (RPObject object : slot) {
				RPObject object_added = new RPObject();
				RPObject object_deleted = new RPObject();

				/* So for each object in the slot, we get the differences */
				object.getDifferences(object_added, object_deleted);

				if (object_added.size() > 0) {
					/* If slot is no there, add it. */
					if (!oadded.hasSlot(slot.getName())) {
						RPSlot addedslot=new RPSlot(slot.getName());
						addedslot.setOwner(oadded);
						oadded.slots.add(addedslot);
					}

					// TODO: Is it possible for the object to previously exist?
					/* If it is not added, add it */
					if (!oadded.getSlot(slot.getName()).has(object.getID())) {
						// TODO: Do we need to set the id?
						object_added.put("id", object.get("id"));
						oadded.getSlot(slot.getName()).add(object_added);
					}
				}

				if (object_deleted.size() > 0) {
					if (!odeleted.hasSlot(slot.getName())) {
						RPSlot deletedslot=new RPSlot(slot.getName());
						deletedslot.setOwner(odeleted);
						odeleted.slots.add(deletedslot);
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

		if (odeleted.size() > 0) {
			odeleted.put("id", get("id"));
		}
	}


	/**
	 * With the diferences computed by getDifferences in added and deleted we build an update
	 * object by applying the changes.
	 * @param addedChanges the added and modified attributes, slots and events or null
	 * @param deletedChanges the deleted attributes and slots or null
	 * @throws Exception
	 */
	public void applyDifferences(RPObject addedChanges, RPObject deletedChanges) {
		if (deletedChanges != null) {
			for (String attrib : deletedChanges) {
				if (!attrib.equals("id") && !attrib.equals("zoneid")) {
					remove(attrib);
				}
			}

			for (RPSlot slot : deletedChanges.slots) {
				if (slot.size() == 0) {
					removeSlot(slot.getName());
				}
			}
		}

		if (addedChanges != null) {
			for (String attrib : addedChanges) {
				put(attrib, addedChanges.get(attrib));
			}

			for (RPSlot slot : addedChanges.slots) {
				addSlot(slot.getName());
			}
		}
	}

	// TODO: Refactor this method. Looks like it claims for bugs!"
	/**
	 * This method apply the changes retrieved from getDifferences and build the
	 * updated object
	 */
	public RPObject applyDifferences_old(RPObject added, RPObject deleted) throws Exception {
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
						if (object.size() == 1) {/** id attribute */
							getSlot(slot.getName()).remove(new ID(object));
						} else {
							RPObject actualObject = getSlot(slot.getName()).get(new ID(object));
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
					slots.add(new RPSlot(slot.getName()));
				}

				/** for each of the objects, add it */
				for (RPObject object : slot) {
					if (getSlot(slot.getName()).has(new ID(object))) {
						getSlot(slot.getName()).get(new ID(object)).applyDifferences(object, null);
					} else {
						String id=object.get("id");
						getSlot(slot.getName()).add(object);
						object.put("id", id);
					}
				}
			}
		}
		return this;
	}
}
