/* $Id: IRPZone.java,v 1.10 2007/02/06 16:43:04 arianne_rpg Exp $ */
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

/** Interface for managing the objects in a RPZone. */
public interface IRPZone extends Iterable<RPObject> {
	/** An unique ID for this zone */
	public static class ID implements marauroa.common.net.Serializable {
		private String id;

		/**
		 * Constructor
		 * @param oid the object id
		 */
		public ID(String zid) {
			id = zid;
		}

		/**
		 * This method returns the object id
		 *
		 * @return the object id.
		 */
		public String getID() {
			return id;
		}

		/**
		 * This method returns true of both ids are equal.
		 *
		 * @param anotherid another id object
		 * @return true if they are equal, or false otherwise.
		 */
		@Override
		public boolean equals(Object anotherid) {
			return (anotherid != null) && (anotherid instanceof IRPZone.ID) && (id.equals(((IRPZone.ID) anotherid).id));
		}

		/** We need it for HashMap */
		@Override
		public int hashCode() {
			return id.hashCode();
		}

		/**
		 * This method returns a String that represent the object
		 *
		 * @return a string representing the object.
		 */
		@Override
		public String toString() {
			return "IRPZone.ID [id=" + id + "]";
		}

		/** Serialize the object into a stream of bytes. */
		public void writeObject(marauroa.common.net.OutputSerializer out)
				throws java.io.IOException {
			out.write(id);
		}

		/** Deserialize the object and fills this object with the data */
		public void readObject(marauroa.common.net.InputSerializer in)
				throws java.io.IOException, java.lang.ClassNotFoundException {
			id = in.readString();
		}
	}

	/** Returns the ID of the zone */
	public ID getID();

	/** This method is called when the zone is created to popullate it */
	public void onInit() throws Exception;

	/**
	 * This method is called when the server finish to save the content of the
	 * zone
	 */
	public void onFinish() throws Exception;

	/**
	 * This method adds an object to the Zone. Object can be modified after this
	 * methods and changes are expected to happen too in zone stored object.
	 */
	public void add(RPObject object) throws RPObjectInvalidException;

	/**
	 * This method tag an object of the Zone as modified. Object can be modified
	 * after this methods and changes are expected to happen too in zone stored
	 * object.
	 */
	public void modify(RPObject object) throws RPObjectInvalidException;

	/**
	 * This method removed an object of the Zone and return it. Object can be
	 * modified but it is not longer inside zone.
	 */
	public RPObject remove(RPObject.ID id) throws RPObjectNotFoundException;

	/**
	 * This method returns an object of the Zone. Object can be modified after
	 * this methods and changes are expected to happen too in zone stored
	 * object.
	 */
	public RPObject get(RPObject.ID id) throws RPObjectNotFoundException;

	/** This method returns true if the object exists in the Zone */
	public boolean has(RPObject.ID id);

	/** Assigns a valid RPObject.ID to the object given as parameter */
	public void assignRPObjectID(RPObject object);

	/** Iterates over the elements of the zone */
	public Iterator<RPObject> iterator();

	/** Returns the number of elements of the zone */
	public long size();

	/** This method return the perception of a zone for a player */
	public Perception getPerception(RPObject.ID id, byte type);

	/** This method is called to take zone to the next turn */
	public void nextTurn();
}
