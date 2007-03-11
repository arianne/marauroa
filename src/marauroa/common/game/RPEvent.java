/* $Id: RPEvent.java,v 1.13 2007/03/11 21:17:13 nhnb Exp $ */
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

import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * This class implements an event.
 * It is something that happens along the turn duration and it is hard to represent as an attribute
 * because it just happen and disappear after that or because it happens several times per turn.
 * <p>
 * One interesting example of this would be private chat.<br>
 * When you write private messages, it is added inside the target player, but you can't represent that
 * as an attribute because it two private chat messages happen at the same time, one of them would
 * be lost.
 * <p>
 * So solution to this problem is add each them as RPEvent.
 * <p>
 * A RPEvent is <b>always</b> linked to an RPObject.
 *
 * @author miguel
 *
 */
public class RPEvent implements marauroa.common.net.Serializable {
	/** Name of the event */
	private String name;

	/** Value of the event. */
	private String value;

	/** This event is linked to an object: its owner. */
	private RPObject owner;

	/**
	 * Constructor
	 *
	 */
	public RPEvent(RPObject object) {
		owner=object;
	}

	/**
	 * Constructor
	 * @param object
	 * @param name name of the event
	 * @param value value of the event
	 */
	public RPEvent(RPObject object, String name, String value) {
		this(object);
		put(name,value);
	}

	/** This method create a copy of the slot */
	@Override
	public Object clone() {
		RPEvent event = new RPEvent(owner);
		name=event.name;
		value=event.value;
		owner=event.owner;

		return event;
	}

	/**
	 * Sets the value of the event
	 * @param name name of the event
	 * @param value this event value.
	 */
	public void put(String name, String value) {
		this.name=name;
		this.value=value;
	}

	/**
	 * Return the name of the event
	 * @return name of the event
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the value of the event.
	 * @return value of the event as a String
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the value of the event as a integer
	 * @return the integer value
	 * @throws NumberFormatException  if the number is not convertable.
	 */
	public int getInt() throws NumberFormatException {
		return Integer.parseInt(value);
	}

	/**
	 * Returns the value of the event as a double
	 * @return the double value or an exception if the number is not convertable.
	 */
	public double getDouble() {
		return Double.parseDouble(value);
	}

	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	/**
	 * Serialize
	 * @param out the output serializer.
	 * @param level the detail level of the serialization
	 */
	public void writeObject(OutputSerializer out, DetailLevel level) throws IOException {
		RPClass rpClass = owner.getRPClass();

		Definition def=rpClass.getDefinition(DefinitionClass.RPEVENT, name);
		short code=def.getCode();

		if (level == DetailLevel.FULL) {
			// We want to ensure that event text is stored.
			code = -1;
		}

		out.write(code);

		if (code == -1) {
			out.write255LongString(name);
		}

		def.serialize(value, out);
	}

	/**
	 * Deserialize
	 * @param in the input serializer
	 */
	public void readObject(InputSerializer in) throws IOException, ClassNotFoundException {
		short code = in.readShort();

		if (code == -1) {
			name = in.read255LongString();
		} else {
			RPClass rpClass = owner.getRPClass();
			name=rpClass.getName(DefinitionClass.RPEVENT, code);
		}

		RPClass rpClass = owner.getRPClass();
		Definition def=rpClass.getDefinition(DefinitionClass.RPEVENT, name);
		value=def.deserialize(in);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Returns true if two objects are exactly equal
	 * @param obj the object to compare with this one.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RPEvent) {
			RPEvent comp=(RPEvent)obj;
			return name.equals(comp.name) && value.equals(value);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "["+name+"="+value+"]";
	}

}
