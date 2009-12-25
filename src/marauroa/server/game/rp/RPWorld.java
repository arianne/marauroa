/* $Id: RPWorld.java,v 1.27 2009/12/25 13:15:30 nhnb Exp $ */
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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import marauroa.common.Log4J;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;

/**
 * This class is a container of RPZones.
 * <p>
 * Worlds in Marauroa can be so big, so huge, that we need to split them in to
 * several pieces. Each of these pieces are what we call an IRPZone.
 * <p>
 * So our world is made of several IRPZones that are independent of each other.
 * <p>
 * RPWorld provides onInit and onFinish methods that are called on server
 * initialisation and server finalization to define what to do with the world on
 * these events.<br>
 * <b>There is no default behaviour and you need to extend this class to
 * implement the behaviour</b>.
 *
 * @author miguel
 */
public class RPWorld implements Iterable<IRPZone> {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPWorld.class);

	/** The Singleton instance */
	private static RPWorld instance;

	/** A map containing the zones. */
	Map<IRPZone.ID, IRPZone> zones;

	/** The all-mighty player container. */
	PlayerEntryContainer playerContainer;

	/**
	 * creates a new RPWorld. Note this class is designed as a singleton.
	 */
	protected RPWorld() {
		zones = new ConcurrentHashMap<IRPZone.ID, IRPZone>();
	}

	/**
	 * Initialize the player entry container so RPWorld knows about players.
	 *
	 */
	protected void initialize() {
		playerContainer = PlayerEntryContainer.getContainer();
	}

	/**
	 * Sets the instance of RPWorld we are going to use, so we are truly using a
	 * singleton pattern.
	 *
	 * @param implementation
	 *            the instance of RPWorld we are going to use.
	 */
	static void set(RPWorld implementation) {
		instance = implementation;
	}

	/**
	 * Returns an unique World method.
	 *
	 * @return an instance of RPWorld
	 */
	public static RPWorld get() {
		if (instance == null) {
			RPWorld instance = new RPWorld();
			instance.initialize();
			RPWorld.instance = instance;
		}
		return instance;
	}

	/** This method is called when RPWorld is created by RPServerManager */
	public void onInit() {
		// implement in subclasses
	}

	/** This method is called when server is going to shutdown. */
	public void onFinish() {
		/*
		 * Call onFinish for each of the zones.
		 */
		for (IRPZone zone : zones.values()) {
			try {
				zone.onFinish();
			} catch (Exception e) {
				logger.warn("Exception at onFinish", e);
			}
		}
	}

	/**
	 * Adds a new zone to World
	 *
	 * @param zone
	 *            a zone to add to world.
	 */
	public void addRPZone(IRPZone zone) {
		zones.put(zone.getID(), zone);
	}

	/**
	 * Returns true if world has such zone
	 *
	 * @param zoneid
	 *            the zone to query
	 * @return true of the zone exists
	 */
	public boolean hasRPZone(IRPZone.ID zoneid) {
		return zones.containsKey(zoneid);
	}

	/**
	 * Returns the zone or null if it doesn't exists
	 *
	 * @param zoneid
	 *            the zone to query
	 * @return the zone or null if it is not found.
	 */
	public IRPZone getRPZone(IRPZone.ID zoneid) {
		return zones.get(zoneid);
	}

	/**
	 * Returns the zone or null if it doesn't exists
	 *
	 * @param objectid
	 *            an id of an object that is in the zone to query
	 * @return the zone or null if it is not found.
	 */
	public IRPZone getRPZone(RPObject.ID objectid) {
		return zones.get(new IRPZone.ID(objectid.getZoneID()));
	}

	/**
	 * Removes a zone from world.
	 * It calls zone.onFinish method to free resources zone could have allocated.	 * 
	 * 
	 * @param zoneid
	 * @return the zone removed or null if not found
	 * @throws Exception caused by onFinish
	 */
	public IRPZone removeRPZone(IRPZone.ID zoneid) throws Exception {
		IRPZone zone=zones.remove(zoneid);
		
		if(zone!=null) {
		  zone.onFinish();
		}
		
		return zone;
	}

	/**
	 * Removes a zone from world.
	 * It calls zone.onFinish method to free resources zone could have allocated.
	 * 
	 * @param objectid
	 * @return the zone removed or null if not found
	 * @throws Exception caused by onFinish
	 */
	public IRPZone removeRPZone(RPObject.ID objectid) throws Exception {
		IRPZone.ID zoneid=new IRPZone.ID(objectid.getZoneID());
		IRPZone zone=zones.remove(zoneid);
		
		if(zone!=null) {
		  zone.onFinish();
		}
		
		return zone;
	}
	
	/**
	 * This method adds an object to the zone it points with its zoneid
	 * attribute. And if it is a player, it request also a sync perception.
	 *
	 * @param object
	 *            the object to add
	 */
	public void add(RPObject object) {
		if (object.has("zoneid")) {
			IRPZone zone = zones.get(new IRPZone.ID(object.get("zoneid")));
			if (zone == null) {
				logger.error("Unknown zone: " + object.get("zoneid"));
				return;
			}
			zone.assignRPObjectID(object);

			zone.add(object);
		}
	}

	/**
	 * When a player is added to a zone, it needs his status to be synced.
	 *
	 * @param object
	 *            the player object
	 */
	public void requestSync(RPObject object) {
		/* A player object will have always the clientid attribute. */
		if (object.has("#clientid")) {
			/*
			 * So if object has the attribute, we request a sync perception as
			 * we have entered a new zone.
			 */
			PlayerEntry entry = playerContainer.get(object);
			if (entry != null) {
				entry.requestSync();
			}
		}
	}

	/**
	 * This method returns an object from a zone using it ID<object, zone>
	 *
	 * @param id
	 *            the object's id
	 * @return the object
	 */
	public RPObject get(RPObject.ID id) {
		IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
		return zone.get(id);
	}

	/**
	 * This method returns true if an object exists in a zone using it ID<object,
	 * zone>
	 *
	 * @param id
	 *            the object's id
	 * @return true if the object exists
	 */
	public boolean has(RPObject.ID id) {
		IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
		return zone.has(id);
	}

	/**
	 * This method returns an object from a zone using it ID<object, zone> and
	 * remove it
	 *
	 * @param id
	 *            the object's id
	 * @return the object or null if it not found.
	 */
	public RPObject remove(RPObject.ID id) {
		IRPZone zone = zones.get(new IRPZone.ID(id.getZoneID()));
		if (zone != null) {
			return zone.remove(id);
		}

		logger.error("Cannot remove rpobject with id " + id + " from zone because there is no zone with that name in thw RPWorld");
		return null;
	}

	/**
	 * This method returns an iterator over all the zones contained.
	 *
	 * @return iterator over zones.
	 */
	public Iterator<IRPZone> iterator() {
		return zones.values().iterator();
	}

	/**
	 * This method notify zone that object has been modified. Used in Delta^2
	 *
	 * @param object
	 *            the object that has been modified.
	 */
	public void modify(RPObject object) {
		IRPZone zone = zones.get(new IRPZone.ID(object.get("zoneid")));
		if (zone != null) {
			zone.modify(object);
		} else {
			logger.warn("calling RPWorld.modify on a zoneless object: " + object + " parent: " + object.getContainerBaseOwner(), new Throwable());
		}
	}

	/**
	 * This methods make a player/object to change zone.
	 *
	 * @param newzoneid
	 *            the new zone id
	 * @param object
	 *            the object we are going to change zone to.
	 * @throws RPObjectInvalidException
	 */
	public void changeZone(IRPZone.ID newzoneid, RPObject object) {
		try {
			String targetZoneid = newzoneid.getID();
			if (targetZoneid.equals(object.get("zoneid"))) {
				return;
			}

			remove(object.getID());

			object.put("zoneid", targetZoneid);

			add(object);

			requestSync(object);

		} catch (Exception e) {
			logger.error("error changing Zone", e);
			throw new RPObjectInvalidException("zoneid");
		}
	}

	/**
	 * This methods make a player/object to change zone.
	 *
	 * @param newzone
	 *            the new zone id
	 * @param object
	 *            the object we are going to change zone to.
	 */
	public void changeZone(String newzone, RPObject object) {
		changeZone(new IRPZone.ID(newzone), object);
	}

	/**
	 * This method make world to move to the next turn, calling each zone
	 * nextTurn method. *
	 */
	public void nextTurn() {
		for (IRPZone zone : zones.values()) {
			zone.nextTurn();
		}
	}

	/**
	 * This methods return the amount of objects added to world.
	 *
	 * @return the amount of objects added to world.
	 */
	public int size() {
		int size = 0;

		for (IRPZone zone : zones.values()) {
			size += zone.size();
		}

		return size;
	}
}
