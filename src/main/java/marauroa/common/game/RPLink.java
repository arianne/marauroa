/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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

import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * An RPLink represent an object relation that is not as strong as RPSlot,
 * and although it could be modelled with a slot it would be better to manage
 * it with an RPLink.
 *
 * I.e:
 *   Buddies are handle now with a slot, when buddies are not an slot, so it
 *   just makes code harder and more prone to error.
 *   Buddies are not an slot in the sense that it is not something that a player
 *   contains, but an abstraction of something that a player is aware of.
 *   
 *   Bag is an slot
 *   Buddies should be an RPLink
 *
 * @author miguel
 *
 */
public class RPLink implements marauroa.common.net.Serializable, Cloneable {

	/** Name of the rplink */
	private String name;

	/** Target object */
	private RPObject object;

	/** This slot is linked to an object: its owner. */
	private RPObject owner;

	/**
	 * Constructor
	 *
	 * @param name name of link
	 * @param object target object
	 */
	public RPLink(String name, RPObject object) {
		this.name = name;
		this.object = object;
		this.owner = null;
	}

	/**
	 * This method sets the owner of the link. Owner is used for having access
	 * to RPClass.
	 *
	 * @param object
	 *            sets the object that owns this link.
	 */
	void setOwner(RPObject object) {
		owner = object;
	}

	/**
	 * This method returns the owner of the object
	 *
	 * @return the owner of the link
	 */
	RPObject getOwner() {
		return owner;
	}

	/**
	 * Return the name of the rplink
	 * @return the name of the rplink
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the object for this RPLink.
	 * @param object the object that will represent this RPLink.
	 */
	public void setObject(RPObject object) {
		this.object = object;
	}

	/**
	 * Returns the object that represent this rplink.
	 * @return the object that represent this rplink.
	 */
	public RPObject getObject() {
		return object;
	}

	@Override
	public Object clone() {
		RPLink link = null;
		try {
			link = (RPLink) super.clone();
		} catch (CloneNotSupportedException e) {
			// cloning is supported
			return null;
		}
		link.object = (RPObject) object.clone();
		return link;
	}

	@Override
	public String toString() {
		return "name: " + name + " --> " + object;
	}

	public void writeObject(OutputSerializer out) throws IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	/**
	 * Method to convert the object into a stream
	 *
	 * @param out OutputSerializer to write the object to
	 * @param level DetailLevel
	 * @throws IOException in case of an IO-error
	 */
	public void writeObject(OutputSerializer out, DetailLevel level) throws IOException {
		RPClass rpClass = owner.getRPClass();

		Definition def = rpClass.getDefinition(DefinitionClass.RPLINK, name);
		short code = def.getCode();

		if (level == DetailLevel.FULL) {
			// We want to ensure that attribute text is stored.
			code = -1;
		}

		out.write(code);

		if (code == -1) {
			out.write(name);
		}

		out.write(object);
	}

	public void readObject(InputSerializer in) throws IOException {
		short code = in.readShort();
		if (code == -1) {
			name = in.readString();
		} else {
			RPClass rpClass = owner.getRPClass();
			name = rpClass.getName(DefinitionClass.RPLINK, code);
		}

		object = (RPObject) in.readObject(new RPObject());
	}

	/**
	 * Returns true if two objects are exactly equal
	 *
	 * @param obj
	 *            the object to compare with this one.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj instanceof RPLink) {
			RPLink link = (RPLink) obj;
			return name.equals(link.name) && object.equals(link.object);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
