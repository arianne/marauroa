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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.net.NetConst;

/**
 * This class implements an Object.
 * <p>
 * An object is the basic abstraction at marauroa. Players are objects,
 * creatures are objects, the maze at pacman is an object, each gladiator is an
 * object... everything is an object.<br>
 * But don't get confused with all the object keyword usage out there. An object
 * is anything that can be though as an object ( physical or logical thing ).
 * <p>
 * Objects are stored at IRPZones.
 * <p>
 * Objects contains:
 * <ul>
 * <li>RPSlots
 * <li>RPLinks
 * <li>RPEvents
 * </ul>
 */

public class RPObject extends SlotOwner {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPObject.class);

	/** We are interested in clearing added and deleted only if they have changed. */
	private boolean modified;

	/** a list of events that this object contains */
	private List<RPEvent> events;

	/** a list of links that this object contains. */
	private List<RPLink> links;

	/** a map to store maps with keys and values **/
	private Map<String, Attributes> maps;

	/** Which object contains this one. */
	private SlotOwner container;

	/** In which slot are this object contained */
	private RPSlot containerSlot;

	/** added slots, used at Delta^2 */
	private List<String> addedSlots;

	/** deleted slots, used at Delta^2 */
	private List<String> deletedSlots;

	/** added slots, used at Delta^2 */
	private List<String> addedLinks;

	/** deleted slots, used at Delta^2 */
	private List<String> deletedLinks;

	/** added maps, used at Delta^2 */
	private List<String> addedMaps;

	/** deleted maps, used at Delta^2 */
	private List<String> deletedMaps;

	/** Defines an invalid object id */
	public final static ID INVALID_ID = new ID(-1, "");

	/** an empty list */
	private static final List<RPEvent> EMPTY = Collections.unmodifiableList(new ArrayList<RPEvent>());

	/**
	 * If this variable is true the object is removed from the perception send
	 * to client.
	 */
	private boolean hidden;

	/** Defines if this object should be stored at database. */
	private boolean storable;

	/**
	 * Constructor
	 */
	public RPObject() {
		super(RPClass.getBaseRPObjectDefault());
		clear();
	}

	/**
	 * Constructor
	 *
	 * @param rpclass of this object
	 */
	public RPObject(RPClass rpclass) {
		super(rpclass);
		clear();
	}

	/**
	 * Constructor
	 *
	 * @param rpclass of this object
	 */
	public RPObject(String rpclass) {
		super(RPClass.getRPClass(rpclass));
		clear();
	}

	/**
	 * Constructor
	 *
	 * @param initialize initialize attributes
	 */
	RPObject(boolean initialize) {
		super(RPClass.getBaseRPObjectDefault());
		if (initialize) {
			clear();
		}
	}

	private void clear() {
		slots = null;
		addedSlots = null;
		deletedSlots = null;

		events = null;

		links = null;
		addedLinks = null;
		deletedLinks = null;

		maps = null;
		addedMaps = null;
		deletedMaps = null;

		modified = false;

		container = null;
		containerSlot = null;

		hidden = false;
		storable = false;
	}

	/**
	 * Copy constructor
	 *
	 * @param object
	 *            the object that is going to be copied.
	 */
	public RPObject(RPObject object) {
		this();

		fill(object);
	}

	/**
	 * Copy constructor
	 *
	 * @param object
	 *            the object that is going to be copied.
	 */
	public void fill(RPObject object) {
		super.fill(object);

		hidden = object.hidden;
		storable = object.storable;
		modified = object.modified;

		container = object.container;
		containerSlot = object.containerSlot;

		if (object.events != null) {
			if (events == null) {
				events = new LinkedList<RPEvent>();
			}
			for (RPEvent event : object.events) {
				RPEvent temp = (RPEvent) event.clone();
				temp.setOwner(this);
				events.add(temp);
			}
		}

		if (object.links != null) {
			if (links == null) {
				links = new LinkedList<RPLink>();
			}
			for (RPLink link : object.links) {
				RPLink temp = (RPLink) link.clone();
				temp.setOwner(this);
				links.add(temp);
			}
		}

		if (object.maps != null) {
			maps = new HashMap<String, Attributes>();
			try {
				for (Entry<String, Attributes> entry : object.maps.entrySet()) {
					Attributes toAdd = (Attributes) entry.getValue().clone();
					maps.put(entry.getKey(), toAdd);
				}
			} catch (CloneNotSupportedException e) {
				logger.error(e, e);
			}
		}

		/*
		 * Copy also the delta^2 info.
		 */
		if (object.addedSlots != null) {
			if (addedSlots == null) {
				addedSlots = new LinkedList<String>();
			}
			for (String slot : object.addedSlots) {
				addedSlots.add(slot);
			}
		}

		if (object.deletedSlots != null) {
			if (deletedSlots == null) {
				deletedSlots = new LinkedList<String>();
			}
			for (String slot : object.deletedSlots) {
				deletedSlots.add(slot);
			}
		}

		if (object.addedLinks != null) {
			if (addedLinks == null) {
				addedLinks = new LinkedList<String>();
			}
			for (String link : object.addedLinks) {
				addedLinks.add(link);
			}
		}

		if (object.deletedLinks != null) {
			if (deletedLinks == null) {
				deletedLinks = new LinkedList<String>();
			}
			for (String link : object.deletedLinks) {
				deletedLinks.add(link);
			}
		}

		if (object.addedMaps != null) {
			if (addedMaps == null) {
				addedMaps = new LinkedList<String>();
			}
			for (String entry : object.addedMaps) {
				addedMaps.add(entry);
			}
		}

		if (object.deletedMaps != null) {
			if (deletedMaps == null) {
				deletedMaps = new LinkedList<String>();
			}
			for (String entry : object.deletedMaps) {
				deletedMaps.add(entry);
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *            the id of the object
	 */
	RPObject(ID id) {
		this();
		setID(id);
	}

	/**
	 * Returns an ID object representing the id of this object.
	 *
	 * @return the identifier of the object
	 */
	public RPObject.ID getID() {
		return new ID(this);
	}

	/**
	 * Sets the attributes that define the ID of the object.
	 *
	 * @param id
	 *            the object id to set for this object
	 */
	public void setID(RPObject.ID id) {
		put("id", id.getObjectID());

		/*
		 * We don't use zoneid inside slots.
		 */
		if (id.getZoneID() != null) {
			put("zoneid", id.getZoneID());
		}
	}

	/**
	 * Makes this object invisible, so it is not added in any perception. This
	 * method is not callable directly from the object once it has been added to
	 * a zone. If it is already added, this method must be called from
	 * IRPZone.hide()
	 */
	public void hide() {
		hidden = true;

		/*
		 * NOTE: A hidden object should be removed from the perception. So
		 * either call hide before adding object to a zone or call
		 * IRPZone.hide() instead.
		 */
	}

	/**
	 * Makes this object visible again. This method is not callable directly
	 * from the object once it has been added to a zone. If it is already added,
	 * this method must be called from IRPZone.unhide()
	 */
	public void unhide() {
		hidden = false;

		/*
		 * NOTE: An object that is now unhidden should be added to the
		 * perception. So either call unhide before adding object to a zone or
		 * call IRPZone.unhide() instead.
		 */
	}

	/**
	 * Return true if this object is hidden.
	 *
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
		storable = true;
	}

	/**
	 * Declare that this object should not be stored at zones.
	 */
	public void unstore() {
		storable = false;
	}

	/**
	 * Return true if the object should be stored at database.
	 *
	 * @return true if the object should be stored at database.
	 */
	public boolean isStorable() {
		return storable;
	}

	/**
	 * Returns true if this object is contained inside another one.
	 *
	 * @return true if this object is contained inside another one.
	 */
	public boolean isContained() {
		return container != null;
	}

	/**
	 * This make this object to be contained in the slot of container.
	 *
	 * @param slotOwner
	 *            the object that is going to contain this object.
	 * @param slot
	 *            the slot of the object that contains this object.
	 */
	@Override
	public void setContainer(SlotOwner slotOwner, RPSlot slot) {
		container = slotOwner;
		containerSlot = slot;
	}

	/**
	 * Returns the container where this object is
	 *
	 * @return the container of this object.
	 */
	public RPObject getContainer() {
		return (RPObject) container;
	}

	/**
	 * Returns the base container where this object is
	 *
	 * @return the base container of this object.
	 */
	public RPObject getBaseContainer() {
		if (container != null) {
			return (RPObject) container.getContainerBaseOwner();
		} else {
			return this;
		}
	}

	/**
	 * Returns the container where this object is
	 *
	 * @return the container of this object.
	 */
	@Override
	public SlotOwner getContainerOwner() {
		return container;
	}

	/**
	 * Returns the base container where this object is
	 *
	 * @return the base container of this object.
	 */
	@Override
	public SlotOwner getContainerBaseOwner() {
		if (container != null) {
			return container.getContainerBaseOwner();
		} else {
			return this;
		}
	}

	/**
	 * Returns the slot where this object is contained
	 *
	 * @return the slot of the object that contains this object.
	 */
	public RPSlot getContainerSlot() {
		return containerSlot;
	}

	/**
	 * Gets and object from the tree of RPSlots using its id ( that it is unique ).
	 * Return null if it is not found.
	 *
	 * @param id the id of the object to look for.
	 * @return RPObject
	 */
	public RPObject getFromSlots(int id) {
		if (isContained()) {
			if (getInt("id") == id) {
				return this;
			}
		}

		RPObject found = null;
		if (slots != null) {
			for (RPSlot slot : slots) {
				for (RPObject object : slot) {
					found = object.getFromSlots(id);

					if (found != null) {
						return found;
					}
				}
			}
		}
		return found;
	}

	/**
	 * This method add the slot to the object
	 *
	 * @param name
	 *            the RPSlot name to be added
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	@Override
	public void addSlot(String name) throws SlotAlreadyAddedException {
		super.addSlot(name);

		/** Notify delta^2 about the addition of this slot */
		if (addedSlots == null) {
			addedSlots = new LinkedList<String>();
		}
		addedSlots.add(name);
		modified = true;
	}

	/**
	 * This method add the slot to the object
	 *
	 * @param slot
	 *            the RPSlot to be added
	 * @throws SlotAlreadyAddedException
	 *             if the slot already exists
	 */
	@Override
	public void addSlot(RPSlot slot) throws SlotAlreadyAddedException {
		super.addSlot(slot);

		/* Notify delta^2 about the addition of this slot */
		if (addedSlots == null) {
			addedSlots = new LinkedList<String>();
		}
		addedSlots.add(slot.getName());
		modified = true;
	}

	/**
	 * This method is used to remove an slot of the object
	 *
	 * @param name
	 *            the name of the slot
	 * @return the removed slot if it is found or null if it is not found.
	 */
	@Override
	public RPSlot removeSlot(String name) {
		for (Iterator<RPSlot> it = slotsIterator(); it.hasNext();) {
			RPSlot slot = it.next();
			if (name.equals(slot.getName())) {
				// BUG: if an slot is added and deleted on the same turn it
				// shouldn't be mention on deleted.
				/** Notify delta^2 about the removal of this slot. */
				if (deletedSlots == null) {
					deletedSlots = new LinkedList<String>();
				}
				deletedSlots.add(name);

				modified = true;

				/* Remove and return it */
				it.remove();
				return slot;
			}
		}

		return null;
	}

	/**
	 * Add an event to this object and set event's owner to this object.
	 *
	 * @param event
	 *            the event to add.
	 */
	public void addEvent(RPEvent event) {
		event.setOwner(this);
		if (events == null) {
			events = new LinkedList<RPEvent>();
		}
		events.add(event);
	}

	/**
	 * Empty the list of events. This method is called at the end of each turn.
	 */
	public void clearEvents() {
		if (events != null) {
			events.clear();
		}
	}

	/**
	 * Iterate over the events list
	 *
	 * @return an iterator over the events
	 */
	public Iterator<RPEvent> eventsIterator() {
		if (events == null) {
			return EMPTY.iterator();
		}
		return events.iterator();
	}

	/**
	 * Returns an unmodifiable list of the events
	 *
	 * @return a list of the events
	 */
	public List<RPEvent> events() {
		if (events == null) {
			return EMPTY;
		}
		return Collections.unmodifiableList(events);
	}

	/**
	 * Adds a new link to the object.
	 * @param name the name of the link
	 * @param object the object to link.
	 */
	public void addLink(String name, RPObject object) {
		if (hasLink(name)) {
			throw new SlotAlreadyAddedException(name);
		}

		RPLink link = new RPLink(name, object);
		link.setOwner(this);
		if (links == null) {
			links = new LinkedList<RPLink>();
		}
		links.add(link);

		if (addedLinks == null) {
			addedLinks = new LinkedList<String>();
		}
		addedLinks.add(name);
		modified = true;
	}

	/**
	 * Adds a new link to the object.
	 * @param link the link to add.
	 */
	public void addLink(RPLink link) {
		if (hasLink(link.getName())) {
			throw new SlotAlreadyAddedException(link.getName());
		}

		link.setOwner(this);
		if (links == null) {
			links = new LinkedList<RPLink>();
		}
		links.add(link);

		if (addedLinks == null) {
			addedLinks = new LinkedList<String>();
		}
		addedLinks.add(link.getName());
		modified = true;
	}

	/**
	 * Returns the link with given name or null if not found.
	 * @param name the name of the link to find.
	 * @return the link with given name or null if not found.
	 */
	public RPLink getLink(String name) {
		if (links == null) {
			return null;
		}
		for (RPLink link : links) {
			if (name.equals(link.getName())) {
				return link;
			}
		}

		return null;
	}

	/**
	 * Return the linked object by the given link or null if the link doesn't exist.
	 * @param name the name of the link.
	 * @return the object linked by the given link.
	 */
	public RPObject getLinkedObject(String name) {
		RPLink link = getLink(name);
		if (link != null) {
			return link.getObject();
		}

		return null;
	}

	/**
	 * Returns true if the object has that link.
	 * @param name the name of the link
	 * @return true if the link exists.
	 */
	public boolean hasLink(String name) {
		return getLink(name) != null;
	}

	/**
	 * Removes a link from this object and return it.
	 * @param name the name of the link to remove.
	 * @return the removed link or null if it was not found.
	 */
	public RPLink removeLink(String name) {
		if (links == null) {
			return null;
		}
		for (Iterator<RPLink> it = links.iterator(); it.hasNext();) {
			RPLink link = it.next();
			if (name.equals(link.getName())) {
				if (deletedLinks == null) {
					deletedLinks = new LinkedList<String>();
				}
				deletedLinks.add(name);

				modified = true;

				/* Remove and return it */
				it.remove();
				return link;
			}
		}

		return null;
	}

	/**
	 * Puts a value for a key in a given map
	 *
	 * @param map the name of the map to put in the value
	 * @param key the key to store for the value
	 * @param value the value
	 */
	public void put(String map, String key, String value) {
		if ((maps == null) || !this.maps.containsKey(map)) {
			this.addMap(map);
		}
		if ((key.equals("id") || key.equals("zoneid"))) {
			throw new IllegalArgumentException(
					"\"id\" and \"zoneid\" are reserved keys that may not be used.");
		}
		this.maps.get(map).put(key, value);
		if (addedMaps == null) {
			addedMaps = new LinkedList<String>();
		}
		if (!this.addedMaps.contains(map)) {
			this.addedMaps.add(map);
		}
	}

	/**
	 * Puts a value for a key in a given map
	 *
	 * @param map the name of the map to put in the value
	 * @param key the key to store for the value
	 * @param value the value
	 */
	public void put(String map, String key, int value) {
		this.put(map, key, Integer.toString(value));
	}

	/**
	 * Puts a value for a key in a given map
	 *
	 * @param map the name of the map to put in the value
	 * @param key the key to store for the value
	 * @param value the value
	 */
	public void put(String map, String key, double value) {
		this.put(map, key, Double.toString(value));
	}

	/**
	 * Puts a value for a key in a given map
	 *
	 * @param map the name of the map to put in the value
	 * @param key the key to store for the value
	 * @param value the value
	 */
	public void put(String map, String key, boolean value) {
		this.put(map, key, Boolean.toString(value));
	}

	/**
	 * Checks if a map has an entry
	 *
	 * @param map the name of the map to search in
	 * @param key the key to search for
	 * @return true, if the entry exists; false otherwise
	 */
	public boolean has(String map, String key) {
		if ((maps == null) || !this.maps.containsKey(map)) {
			return false;
		}
		return this.maps.get(map).has(key);
	}

	/**
	 * Retrieves a value
	 *
	 * @param map the name of the map to search in
	 * @param key the key to search for
	 * @return the value found
	 */
	public String get(String map, String key) {
		if ((maps == null) || !this.maps.containsKey(map)) {
			Definition def = getRPClass().getDefinition(DefinitionClass.STATIC, map);
			if (def != null) {
				/*
				 * It is possible that the attribute itself doesn't exist as static attribute,
				 * so we should return null instead.
				 */
				return def.getValue();
			}
			return null;
		}
		return this.maps.get(map).get(key);
	}

	/**
	 * Retrieves a value
	 *
	 * @param map the name of the map to search in
	 * @param key the key to search for
	 * @return the value found
	 */
	public int getInt(String map, String key) {
		if ((maps == null) || !this.maps.containsKey(map)) {
			throw new IllegalArgumentException("Map " + map + " not found");
		}
		return this.maps.get(map).getInt(key);
	}

	/**
	 * Retrieves a value
	 *
	 * @param map the name of the map to search in
	 * @param key the key to search for
	 * @return the value found
	 */
	public double getDouble(String map, String key) {
		if ((maps == null) || !this.maps.containsKey(map)) {
			throw new IllegalArgumentException("Map " + map + " not found");
		}
		return this.maps.get(map).getDouble(key);
	}

	/**
	 * Retrieves a value
	 *
	 * @param map the name of the map to search in
	 * @param key the key to search for
	 * @return the value found
	 */
	public boolean getBoolean(String map, String key) {
		if ((maps == null) || !this.maps.containsKey(map)) {
			throw new IllegalArgumentException("Map " + map + " not found");
		}
		return this.maps.get(map).getBool(key);
	}

	/**
	 * Retrieves a full map with the given name
	 *
	 * @param map the name of the map
	 * @return a copy of the map or null if no map with the given name is present
	 */
	public Map<String, String> getMap(String map) {
		if ((maps != null) && this.maps.containsKey(map)) {
			HashMap<String, String> newMap = new HashMap<String, String>();
			Attributes attr = this.maps.get(map);
			for (String key : attr) {
				if ((!key.equals("id") && !key.equals("zoneid"))) {
					newMap.put(key, attr.get(key));
				}
			}
			return newMap;
		}
		return null;
	}

	/**
	 * Remove a map from this RPObject
	 *
	 * @param map the name of the map to remove
	 * @return the RPObject representing the map or null if map not existing
	 */
	public Attributes removeMap(String map) {
		if ((maps != null) && maps.containsKey(map)) {
			Attributes attr = maps.get(map);
			if (deletedMaps == null) {
				deletedMaps = new LinkedList<String>();
			}
			this.deletedMaps.add(map);
			modified = true;
			return attr;
		}
		return null;
	}

	/**
	 * adds a Map to this RPObject
	 *
	 * @param map
	 */
	public void addMap(String map) {
		if (maps == null) {
			maps = new HashMap<String, Attributes>();
		}
		if (maps.containsKey(map)) {
			throw new SlotAlreadyAddedException(map);
		}
		if (getRPClass() != null) {
			if (getRPClass().getDefinition(DefinitionClass.ATTRIBUTE, map).getType() != Type.MAP) {
				throw new IllegalArgumentException("The type of the attribute " + map
						+ " is not MAP type.");
			}
		}
		RPObject newMap = new RPObject();
		newMap.setID(RPObject.INVALID_ID);
		maps.put(map, newMap);
		if (addedMaps == null) {
			addedMaps = new LinkedList<String>();
		}
		addedMaps.add(map);
		modified = true;
	}

	/**
	 * removes an entry from a map
	 *
	 * @param map the name of the map
	 * @param key the key of the entry to remove
	 * @return old value
	 */
	public String remove(String map, String key) {
		if ((key.equals("id") || key.equals("zoneid"))) {
			throw new IllegalArgumentException(
					"\"id\" and \"zoneid\" are reserved keys that may not be used");
		}
		if ((maps != null) && maps.containsKey(map) && maps.get(map).has(key)) {
			this.modified = true;
			if (deletedMaps == null) {
				deletedMaps = new LinkedList<String>();
			}
			if (!this.deletedMaps.contains(map)) {
				this.deletedMaps.add(map);
			}
			return this.maps.get(map).remove(key);
		}
		return null;
	}

	/**
	 * gets all maps and their names as a map
	 *
	 * @return a map with key name of map and value the map itself
	 */
	public Map<String, Map<String, String>> maps() {
		Map<String, Map<String, String>> res = new HashMap<String, Map<String, String>>();
		if (this.maps != null) {
			for (String map : this.maps.keySet()) {
				res.put(map, getMap(map));
			}
		}
		return res;
	}

	/**
	 * check if a map is present in this object
	 * @param map the name of the map
	 * @return true iff this objects has a map with that name
	 */
	public boolean hasMap(String map) {
		return (maps != null) && this.maps.containsKey(map);
	}

	/**
	 * check if a map of this object contains a key
	 * @param map the name of the map
	 * @param key the key to check for
	 * @return true iff map has a value stored for key
	 */
	public boolean containsKey(String map, String key) {
		if (hasMap(map)) {
			return getMap(map).containsKey(key);
		}
		return false;
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

		if ((maps != null) && !maps.isEmpty()) {
			tmp.append(" with maps");
			for (Map.Entry<String, Attributes> map : maps.entrySet()) {
				tmp.append(" " + map.getKey());
				tmp.append("=[" + map.getValue().toAttributeString() + "]");
			}
		}

		if ((links != null) && !links.isEmpty()) {
			tmp.append(" and RPLink ");
			for (RPLink link : links) {
				tmp.append("[" + link.toString() + "]");
			}
		}

		if ((events != null) && !events.isEmpty()) {
			tmp.append(" and RPEvents ");
			for (RPEvent event : events) {
				tmp.append("[" + event.toString() + "]");
			}
		}
		return tmp.toString();
	}

	/**
	 * This method serialize the object with the default level of detail, that
	 * removes private and hidden attributes
	 *
	 * @param out
	 *            the output serializer
	 */
	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		try {
			writeObject(out, DetailLevel.NORMAL);
		} catch (NullPointerException e) {
			logger.warn(this, e);
			throw e;
		}
	}

	/**
	 * This method serialize the object with the given level of detail.
	 *
	 * @param out
	 *            the output serializer
	 * @param level
	 *            the level of Detail
	 */
	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out, DetailLevel level)
			throws java.io.IOException {
		super.writeObject(out, level);

		if (level == DetailLevel.FULL) {
			/*
			 * Even if hidden and storable are server side only
			 * variables, we serialize then for database storage.
			 */
			out.write((byte) 1);
			out.write((byte) (hidden ? 1 : 0));
			out.write((byte) (storable ? 1 : 0));
		} else {
			out.write((byte) 0);
		}

		serializeRPSlots(out, level);

		/*
		 * We compute the amount of links to serialize first. We don't serialize
		 * hidden or private slots unless detail level is full.
		 */
		int size = 0;
		if (links != null) {
			for (RPLink link : links) {
				if (shouldSerialize(DefinitionClass.RPLINK, link.getName(), level)) {
					size++;
				}
			}

			/*
			 * Now write it.
			 */
			out.write(size);
			for (RPLink link : links) {
				Definition def = getRPClass().getDefinition(DefinitionClass.RPLINK, link.getName());

				if (shouldSerialize(def, level)) {
					link.writeObject(out, level);
				}
			}
		} else {
			out.write(0);
		}

		/*
		 * we compute the amount of maps before serializing
		 */
		if (out.getProtocolVersion() >= NetConst.FIRST_VERSION_WITH_MAP_SUPPORT) {
			size = 0;
			if (maps != null) {
				for (Entry<String, Attributes> entry : maps.entrySet()) {
					Definition def = getRPClass().getDefinition(DefinitionClass.ATTRIBUTE,
							entry.getKey());
					if (shouldSerialize(def, level)) {
						size++;
					}
				}
				out.write(size);
				/*
				 * now we write the maps in two steps
				 * 1. the names of the maps
				 * 2. the maps
				 */
				for (String map : maps.keySet()) {
					Definition def = getRPClass().getDefinition(DefinitionClass.ATTRIBUTE, map);
					if (shouldSerialize(def, level)) {
						out.write(map);
					}
				}
				for (String map : maps.keySet()) {
					Definition def = getRPClass().getDefinition(DefinitionClass.ATTRIBUTE, map);
					if (shouldSerialize(def, level)) {
						maps.get(map).writeObject(out, level);
					}
				}
			} else {
				out.write(0);
			}
		}
		/*
		 * We compute the amount of events to serialize first. We don't
		 * serialize hidden or private slots unless detail level is full.
		 */
		size = 0;
		if (events != null) {
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
				Definition def = getRPClass().getDefinition(DefinitionClass.RPEVENT, event.getName());

				if (shouldSerialize(def, level)) {
					event.writeObject(out, level);
				}
			}
		} else {
			out.write(0);
		}
	}

	/**
	 * Fills this object with the data that has been serialized.
	 *
	 * @param in
	 *            the input serializer
	 */
	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException {
		super.readObject(in);
		modified = true;

		if (in.readByte() == 1) {
			hidden = in.readByte() == 1;
			storable = in.readByte() == 1;
		}

		deserializeRPSlots(in);

		/*
		 * then we load links
		 */
		int size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + String.valueOf(size) + " size");
		}

		if (size > 0) {
			links = new LinkedList<RPLink>();

			for (int i = 0; i < size; ++i) {
				RPLink link = new RPLink(null, null);
				link.setOwner(this);
				link = (RPLink) in.readObject(link);
				links.add(link);
			}
		}

		if (in.getProtocolVersion() >= NetConst.FIRST_VERSION_WITH_MAP_SUPPORT) {
			// get the number of maps
			int numberOfMaps = in.readInt();
			// get the names of the maps and store them in a list
			List<String> mapNames = new ArrayList<String>();
			for (int j = 0; j < numberOfMaps; j++) {
				mapNames.add(in.readString());
			}
			// get the map objects and put them into the internal maps
			for (int k = 0; k < numberOfMaps; k++) {
				RPObject rpo = new RPObject();
				rpo = (RPObject) in.readObject(rpo);
				if (maps == null) {
					maps = new HashMap<String, Attributes>();
				}
				maps.put(mapNames.get(k), rpo);
			}
		}
		/*
		 * And now we load events
		 */
		size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + String.valueOf(size) + " size");
		}

		if (size > 0) {
			events = new LinkedList<RPEvent>();
			for (int i = 0; i < size; ++i) {
				RPEvent event = new RPEvent();
				event.setOwner(this);
				event = (RPEvent) in.readObject(event);
				events.add(event);
			}
		} else {
			events = null;
		}
	}

	/**
	 * Returns true if two objects are exactly equal
	 *
	 * @param obj
	 *            the object to compare with this one.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof RPObject) {
			RPObject object = (RPObject) obj;
			if (! (super.equals(obj)
					&& (collectionIsEqualTreaingNullAsEmpty(slots, object.slots))
					&& (collectionIsEqualTreaingNullAsEmpty(events, object.events))
					&& (collectionIsEqualTreaingNullAsEmpty(links, object.links)))) {
				return false;
			}
			// collectionIsEqualTreaingNullAsEmpty is not type compatible with maps,
			// so we do the map check here without calling that method
			if ((maps == null) || maps.isEmpty()) {
				return (object.maps == null) || object.maps.isEmpty();
			}
			return maps.equals(object.maps);
		} else {
			return false;
		}
	}

	/**
	 * compares two collections, empty collections and null collections are treated
	 * as equal to allow easy working with lazy initialisation.
	 *
	 * @param c1 first collection
	 * @param c2 second collection
	 * @return true, if they are equal; false otherwise.
	 */
	private static boolean collectionIsEqualTreaingNullAsEmpty(Collection<?> c1, Collection<?> c2) {
		if ((c1 == null) || c1.isEmpty()) {
			return (c2 == null) || c2.isEmpty();
		}
		return c1.equals(c2);
	}

	@Override
	public int hashCode() {
		String hash = get("id");
		if (hash != null) {
			return Integer.parseInt(hash);
		}
		return 31;
	}

	/**
	 * Returns true if the object is empty
	 *
	 * @return true if the object lacks of any attribute, slots, maps or events.
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty()
				&& (slots == null || slots.isEmpty())
				&& (events == null || events.isEmpty())
				&& (links == null || links.isEmpty())
				&& (maps == null || maps.isEmpty());
	}

	/**
	 * Returns the number of attributes and events this object is made of.
	 */
	@Override
	public int size() {
		try {
			int total = super.size();

			if (events != null) {
				total += events.size();
			}

			if (slots != null) {
				for (RPSlot slot : slots) {
					for (RPObject object : slot) {
						total += object.size();
					}
				}
			}

			if (links != null) {
				for (RPLink link : links) {
					total += link.getObject().size();
				}
			}

			return total;
		} catch (Exception e) {
			logger.error("Cannot determine size", e);
			return -1;
		}
	}

	/**
	 * Removes the visible attributes and events from this object. It iterates
	 * through the slots to remove the attributes too of the contained objects
	 * if they are empty.
	 * @param sync keep the structure intact, by not removing empty slots and links.
	 */
	@Override
	public void clearVisible(boolean sync) {
		super.clearVisible(sync);

		if (events != null) {
			Iterator<RPEvent> itrEvents = events.iterator();
			while (itrEvents.hasNext()) {
				/* Iterate over events and remove all of them that are visible */
				RPEvent event = itrEvents.next();
				Definition def = getRPClass().getDefinition(DefinitionClass.RPEVENT, event.getName());

				if (def == null) {
					logger.warn("Null Definition for event: " + event.getName() + " of RPClass: "
							+ getRPClass().getName());
					continue;
				}

				if (def.isVisible()) {
					itrEvents.remove();
				}
			}
		}

		Iterator<RPSlot> slotit = slotsIterator();
		while (slotit.hasNext()) {
			RPSlot slot = slotit.next();

			slot.clearVisible(sync);

			/*
			 * Even if slot is empty client may be interested in knowing the slot.
			 * So we don't remove the slot on sync type of clear visible.
			 */
			if (!sync && slot.size() == 0) {
				slotit.remove();
				if (addedSlots != null) {
					addedSlots.remove(slot.getName());
				}
				if (deletedSlots != null) {
					deletedSlots.remove(slot.getName());
				}
				modified = true;
			}
		}

		if (links != null) {
			Iterator<RPLink> linkit = links.iterator();
			while (linkit.hasNext()) {
				RPLink link = linkit.next();

				link.getObject().clearVisible(sync);

				/* If link is empty remove it. */
				if (link.getObject().isEmpty()) {
					linkit.remove();
					if (addedLinks != null) {
						addedLinks.remove(link.getName());
					}
					if (deletedLinks != null) {
						deletedLinks.remove(link.getName());
					}
					modified = true;
				}
			}
		}
	}

	/**
	 * Create a depth copy of the object
	 *
	 * @return a copy of this object.
	 */
	@Override
	public Object clone() {
		try {
			RPObject rpobject = (RPObject) super.clone();
			rpobject.clear();
			rpobject.fill(this);
			return rpobject;
		} catch (CloneNotSupportedException e) {
			logger.error(e, e);
			return null;
		}
	}

	/** This class stores the basic identification for a RPObject */
	public static class ID {

		private final int id;

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

		/**
		 * returns the zone id as string
		 *
		 * @return zone id
		 */
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
				ID otherId = (RPObject.ID) anotherid;
				if (id == otherId.id) {
					if (zoneid == null) {
						return otherId.zoneid == null;
					} else {
						return zoneid.equals(otherId.zoneid);
					}
				} else {
					return false;
				}
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
	}

	/**
	 * Clean delta^2 information about added and deleted.
	 * It also empty the event list.
	 *
	 * It is called by Marauroa, don't use :)
	 */
	public void resetAddedAndDeleted() {
		resetAddedAndDeletedAttributes();
		resetAddedAndDeletedRPSlot();
		resetAddedAndDeletedRPLink();
		resetAddedAndDeletedMaps();

		clearEvents();

		if (modified) {
			modified = false;
		}
	}

	/**
	 * Clean delta^2 data in the slots. It is called by Marauroa, don't use :)
	 */
	public void resetAddedAndDeletedRPSlot() {
		if (slots != null) {
			for (RPSlot slot : slots) {
				slot.resetAddedAndDeletedRPObjects();
				for (RPObject object : slot) {
					object.resetAddedAndDeleted();
				}
			}
		}

		if (modified) {
			if (addedSlots != null) {
				addedSlots.clear();
			}
			if (deletedSlots != null) {
				deletedSlots.clear();
			}
		}
	}

	/**
	 * Clean delta^2 data in the links. It is called by Marauroa, don't use :)
	 */
	public void resetAddedAndDeletedRPLink() {
		if (links !=null) {
			for (RPLink link : links) {
				link.getObject().resetAddedAndDeleted();
			}
		}

		if (modified) {
			if (addedLinks != null) {
				addedLinks.clear();
			}
			if (deletedLinks != null) {
				deletedLinks.clear();
			}
		}
	}

	/**
	 * Clean delta^2 data in the maps. It is called by Marauroa, don't use :)
	 */
	public void resetAddedAndDeletedMaps() {
		if (maps != null) {
			for (Attributes map : maps.values()) {
				map.resetAddedAndDeletedAttributes();
			}
		}

		if (addedMaps != null) {
			addedMaps.clear();
		}
		if (deletedMaps != null) {
			deletedMaps.clear();
		}
	}

	/**
	 * Set added objects in slots for this object and fill object passed as
	 * param. * It is called by Marauroa, don't use :)
	 *
	 * @param object
	 *            the object to fill with added data.
	 */
	public void setAddedRPSlot(RPObject object) {
		if (object.addedSlots != null) {
			for (String slot : object.addedSlots) {
				addSlot(slot);
			}
		}
	}

	/**
	 * Set deleted objects in slots for this object and fill object passed as
	 * param. * It is called by Marauroa, don't use :)
	 *
	 * @param object
	 *            the object to fill with deleted data.
	 */
	public void setDeletedRPSlot(RPObject object) {
		if (object.deletedSlots != null) {
			for (String slot : object.deletedSlots) {
				addSlot(slot);
			}
		}
	}

	/**
	 * adds the maps added in the specified object as maps to this object
	 *
	 * @param object RPObject to copy the added maps from
	 */
	public void setAddedMaps(RPObject object) {
		if (object.addedMaps != null) {
			for (String map : object.addedMaps) {
				addMap(map);
			}
		}
	}

	/**
	 * adds the maps deleted in the specified object as maps to this object
	 *
	 * @param object RPObject to copy the deleted maps from
	 */
	public void setDeletedMaps(RPObject object) {
		if (object.deletedMaps != null) {
			for (String map : object.deletedMaps) {
				addMap(map);
			}
		}
	}

	/**
	 * Retrieve the differences stored in this object and add them to two new
	 * objects added changes and delete changes that will contains added and
	 * modified attributes, slots and events and on the other hand deleted
	 * changes that will contain the removes slots and attributes. We don't care
	 * about RP Events because they are removed on each turn.
	 *
	 * @param addedChanges
	 *            an empty object
	 * @param deletedChanges
	 *            an empty object
	 */
	public void getDifferences(RPObject addedChanges, RPObject deletedChanges) {
		/*
		 * First we get the diff from attributes this object contains.
		 */
		addedChanges.setAddedAttributes(this);
		deletedChanges.setDeletedAttributes(this);

		/*
		 * We add to the added object the events that exists.
		 * Because events are cleared on each turn so they have no delta^2
		 */
		if (events != null) {
			if (addedChanges.events == null) {
				addedChanges.events = new LinkedList<RPEvent>();
			}
			for (RPEvent event : events) {
				addedChanges.events.add(event);
			}
		}

		/*
		 * We add the added links.
		 */
		if (addedLinks != null) {
			if (addedChanges.addedLinks == null) {
				addedChanges.addedLinks = new LinkedList<String>();
			}
			for (String addedLink : addedLinks) {
				addedChanges.addLink(addedLink, getLinkedObject(addedLink));
			}
		}

		/*
		 * We add the deleted links.
		 */
		if (deletedLinks != null) {
			if (deletedChanges.deletedLinks == null) {
				deletedChanges.deletedLinks = new LinkedList<String>();
			}
			for (String deletedLink : deletedLinks) {
				deletedChanges.addLink(deletedLink, new RPObject());
			}
		}

		/*
		 * We now get the diffs for the links
		 */
		if (links != null) {
			for (RPLink link : links) {
				RPObject linkadded = new RPObject();
				RPObject linkdeleted = new RPObject();

				link.getObject().getDifferences(linkadded, linkdeleted);

				if (!linkadded.isEmpty()) {
					addedChanges.addLink(link.getName(), linkadded);
				}

				if (!linkdeleted.isEmpty()) {
					deletedChanges.addLink(link.getName(), linkdeleted);
				}
			}
		}

		/*
		 * Now we get the diff from slots.
		 */
		addedChanges.setAddedRPSlot(this);
		deletedChanges.setDeletedRPSlot(this);

		// define tmpAddedChanges and tmpDeletedChanges so that they can be reused
		// because creating RPObjects is relativly expensive
		RPObject tmpAddedChanges;
		RPObject tmpDeletedChanges;
		if (slots != null && !slots.isEmpty()) {
			tmpAddedChanges = new RPObject();
			tmpDeletedChanges = new RPObject();

			for (RPSlot slot : slots) {

				// ignore all slots that are server only
				Definition def = this.getRPClass().getDefinition(DefinitionClass.RPSLOT,
						slot.getName());
				if (def.isHidden()) {
					continue;
				}

				/*
				 * First we process the added things to slot.
				 */
				RPSlot addedObjectsInSlot = new RPSlot(slot.getName());
				if (addedObjectsInSlot.setAddedRPObject(slot)) {
					/*
					 * There is added objects in the slot, so we need to add them to
					 * addedChanges.
					 */
					if (!addedChanges.hasSlot(slot.getName())) {
						addedChanges.addSlot(slot.getName());
					}

					RPSlot changes = addedChanges.getSlot(slot.getName());
					for (RPObject ad : addedObjectsInSlot) {
						changes.add(ad, false);
					}
				}

				/*
				 * Later we process the removed things from the slot.
				 */
				RPSlot deletedObjectsInSlot = new RPSlot(slot.getName());
				if (deletedObjectsInSlot.setDeletedRPObject(slot)) {
					/*
					 * There is deleted objects in the slot, so we need to add them
					 * to deletedChanges.
					 */
					if (!deletedChanges.hasSlot(slot.getName())) {
						deletedChanges.addSlot(slot.getName());
					}

					RPSlot changes = deletedChanges.getSlot(slot.getName());
					for (RPObject ad : deletedObjectsInSlot) {
						changes.add(ad, false);
					}
				}

				/*
				 * Finally we process the changes on the objects of the slot.
				 */
				for (RPObject rec : slot) {

					// ignore modified objects that has been added in the same turn
					if (!addedObjectsInSlot.has(rec.getID())) {

						rec.getDifferences(tmpAddedChanges, tmpDeletedChanges);

						/*
						 * If this object is not empty that means that there has been a
						 * change at it. So we add this object to the slot.
						 */
						if (!tmpAddedChanges.isEmpty()) {
							/*
							 * If slot was not created, create it now. For example if an
							 * object is modified ( that means not added nor deleted ),
							 * it won't have a slot already created on added.
							 */
							if (!addedChanges.hasSlot(slot.getName())) {
								addedChanges.addSlot(slot.getName());
							}

							RPSlot recAddedSlot = addedChanges.getSlot(slot.getName());
							/*
							 * We need to set the id of the object to be equals to the
							 * object from which the diff was generated.
							 */
							tmpAddedChanges.put("id", rec.get("id"));
							recAddedSlot.add(tmpAddedChanges, false);
							tmpAddedChanges = new RPObject();
						}

						/*
						 * Same operation with delete changes
						 */
						if (!tmpDeletedChanges.isEmpty()) {
							/*
							 * If slot was not created, create it now. For example if an
							 * object is modified ( that means not added nor deleted ),
							 * it won't have a slot already created on added.
							 */
							if (!deletedChanges.hasSlot(slot.getName())) {
								deletedChanges.addSlot(slot.getName());
							}

							RPSlot recDeletedSlot = deletedChanges.getSlot(slot.getName());
							/*
							 * We need to set the id of the object to be equals to the
							 * object from which the diff was generated.
							 */
							tmpDeletedChanges.put("id", rec.get("id"));
							recDeletedSlot.add(tmpDeletedChanges, false);
							tmpDeletedChanges = new RPObject();
						}
					}
				}
			}
		}

		/*
		 * now we deal with the diff of maps
		 */
		addedChanges.setAddedMaps(this);
		deletedChanges.setDeletedMaps(this);

		/*
		 * We now get the diffs for the maps
		 */
		if (maps != null) {
			for (Entry<String, Attributes> entry : maps.entrySet()) {
				synchronized (entry.getValue().added) {
					for (Map.Entry<String, String> ent : entry.getValue().added.entrySet()) {
						if (!ent.getKey().equals("id") && !ent.getKey().equals("zoneid")) {
							addedChanges.put(entry.getKey(), ent.getKey(), ent.getValue());
						}
					}
				}

				synchronized (entry.getValue().deleted) {
					for (Map.Entry<String, String> ent : entry.getValue().deleted.entrySet()) {
						if (!ent.getKey().equals("id") && !ent.getKey().equals("zoneid")) {
							deletedChanges.put(entry.getKey(), ent.getKey(), ent.getValue());
						}
					}
				}
			}
		}

		/*
		 * If the diff objects are not empty, we make sure they have the id.
		 */
		if (!addedChanges.isEmpty()) {
			addedChanges.put("id", get("id"));
			String zoneid = get("zoneid");
			if (zoneid != null) {
				addedChanges.put("zoneid", zoneid);
			}
		}

		if (!deletedChanges.isEmpty()) {
			deletedChanges.put("id", get("id"));
			String zoneid = get("zoneid");
			if (zoneid != null) {
				deletedChanges.put("zoneid", zoneid);
			}
		}
	}

	/**
	 * With the differences computed by getDifferences in added and deleted we
	 * build an update object by applying the changes.
	 *
	 * @param addedChanges
	 *            the added and modified attributes, slots and events or null
	 * @param deletedChanges
	 *            the deleted attributes and slots or null
	 */
	public void applyDifferences(RPObject addedChanges, RPObject deletedChanges) {
		super.applyDifferences(addedChanges, deletedChanges);

		if (deletedChanges != null) {

			/*
			 * We apply the deleted changes to the object of the link.
			 */
			if (deletedChanges.links != null) {
				for (RPLink link : deletedChanges.links) {
					if (link.getObject().isEmpty()) {
						removeLink(link.getName());
					} else {
						getLinkedObject(link.getName()).applyDifferences(null, link.getObject());
					}
				}
			}

			/*
			 * we apply the deleted changes to each map
			 */
			if (deletedChanges.maps != null && (maps != null)) {
				for (Entry<String, Attributes> entry : deletedChanges.maps.entrySet()) {
					if (entry.getValue().isEmpty()) {
						removeMap(entry.getKey());
					} else {
						maps.get(entry.getKey()).applyDifferences(null, entry.getValue());
					}
				}
			}

			/*
			 * Now we move to slots and remove the slot if it is empty on delete
			 * changes.
			 */
			if (deletedChanges.slots != null) {
				for (RPSlot slot : deletedChanges.slots) {
					if (slot.size() == 0) {
						removeSlot(slot.getName());
					} else {
						RPSlot changes = getSlot(slot.getName());

						/*
						 * For each of the deleted changes, check if they are
						 * already on the object so they an update and recursively
						 * apply differences to it. On the other hand if object is
						 * not present, it means it is a new object so we can add it
						 * directly.
						 */
						for (RPObject del : slot) {
							/*
							 * If object to remove has more than one attribute that
							 * means that we want to remove these attributes. On the
							 * other hand, if only one attribute is there, that
							 * means that we want to remove the full object from the
							 * slot.
							 */
							if (del.size() > 1) {
								RPObject recChanges = changes.get(del.getID());
								recChanges.applyDifferences(null, del);
							} else {
								changes.remove(del.getID());
							}
						}
					}
				}
			}
		}

		if (addedChanges != null) {

			/*
			 * We add also the events
			 */

			if (addedChanges.events != null) {
				if (events == null) {
					events = new LinkedList<RPEvent>();
				}
				for (RPEvent event : addedChanges.events) {
					events.add(event);
				}
			}

			/*
			 * We apply it for the links.
			 */
			if (addedChanges.links != null) {
				if (links == null) {
					links = new LinkedList<RPLink>();
				}
				for (RPLink link : addedChanges.links) {
					if (!hasLink(link.getName())) {
						links.add(link);
					} else {
						getLinkedObject(link.getName()).applyDifferences(link.getObject(), null);
					}
				}
			}

			/*
			 * we apply the added changes for the maps
			 */
			if (addedChanges.maps != null) {
				for (Entry<String, Attributes> entry : addedChanges.maps.entrySet()) {
					if (maps == null) {
						maps = new HashMap<String, Attributes>();
					}
					if (!maps.containsKey(entry.getKey())) {
						// entry.getValue is an RPObject for compatibility with the network
						// protocol, so copy it into an attributes object
						Attributes attr = new Attributes(RPClass.getBaseRPObjectDefault());
						attr.fill(entry.getValue());
						maps.put(entry.getKey(), attr);
					} else {
						maps.get(entry.getKey()).applyDifferences(entry.getValue(), null);
					}
				}
			}

			/*
			 * For each of the added slots we add it and any object that was
			 * inside.
			 */
			if (addedChanges.slots != null) {
				for (RPSlot slot : addedChanges.slots) {
					if (!hasSlot(slot.getName())) {
						addSlot(slot.getName());
					}

					RPSlot changes = getSlot(slot.getName());

					/*
					 * For each of the added changes, check if they are already on
					 * the object so they an update and recursively apply
					 * differences to it. On the other hand if object is not
					 * present, it means it is a new object so we can add it
					 * directly.
					 */
					for (RPObject ad : slot) {
						RPObject recChanges = changes.get(ad.getID());
						if (recChanges != null) {
							recChanges.applyDifferences(ad, null);
						} else {
							changes.add(ad, false);
						}
					}
				}
			}
		}
	}

	/**
	 * This method returns true if the object has that slot
	 *
	 * @param name
	 *            the name of the slot
	 * @return true if slot exists or false otherwise
	 */
	// this method is here for binary compatibility
	@Override
	public boolean hasSlot(String name) {
		return super.hasSlot(name);
	}

	/**
	 * This method returns a slot whose name is name
	 *
	 * @param name
	 *            the name of the slot
	 * @return the slot or null if the slot is not found
	 */
	// this method is here for binary compatibility
	@Override
	public RPSlot getSlot(String name) {
		return super.getSlot(name);
	}

	/**
	 * Returns a iterator over the slots
	 *
	 * @return an iterator over the slots
	 */
	// this method is here for binary compatibility
	@Override
	public Iterator<RPSlot> slotsIterator() {
		return super.slotsIterator();
	}

	/**
	 * Returns an unmodifiable list of the slots
	 *
	 * @return a list of the slots
	 */
	// this method is here for binary compatibility
	@Override
	public List<RPSlot> slots() {
		return super.slots();
	}

}
