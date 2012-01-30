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

import marauroa.common.Log4J;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * This class implements an event. It is something that happens along the turn
 * duration and it is hard to represent as an attribute because it just happen
 * and disappear after that or because it happens several times per turn.
 * <p>
 * One interesting example of this would be private chat.<br>
 * When you write private messages, it is added inside the target player, but
 * you can't represent that as an attribute because it two private chat messages
 * happen at the same time, one of them would be lost.
 * <p>
 * So solution to this problem is add each them as RPEvent.
 * <p>
 * A RPEvent is <b>always</b> linked to an RPObject.
 *
 * @author miguel, hendrik
 *
 */
public class RPEvent extends SlotOwner {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPEvent.class);

	/** Name of the event */
	private String name;

	/** This event is linked to an object: its owner. */
	private RPObject owner;

	/**
	 * Constructor
	 *
	 * @param name name of this RPEvent
	 */
	public RPEvent(String name) {
		super(RPClass.getRPClass(name));
		this.name = name;
	}

	/**
	 * Constructor
	 *
	 */
	public RPEvent() {
		super(RPClass.getBaseRPObjectDefault());
		// Only used by serialization.
	}

	/** This method create a copy of the slot */
	@Override
	public Object clone() {
		try {
			RPEvent rpevent = (RPEvent) super.clone();
			rpevent.fill(this);
			return rpevent;
		} catch (CloneNotSupportedException e) {
			logger.error(e, e);
			return null;
		}
	}

	/**
	 * Copy constructor
	 *
	 * @param event
	 *            the object that is going to be copied.
	 */
	public void fill(RPEvent event) {
		super.fill(event);
		this.name = event.name;
		this.owner = event.owner;
	}

	/**
	 * Set the owner of this RPEvent.
	 *
	 * @param owner
	 */
	public void setOwner(RPObject owner) {
		this.owner = owner;
	}

	/**
	 * Return the name of the event
	 *
	 * @return name of the event
	 */
	public String getName() {
		return name;
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	/**
	 * Serialize
	 *
	 * @param out
	 *            the output serializer.
	 * @param level
	 *            the detail level of the serialization
	 */
	@Override
	public void writeObject(OutputSerializer out, DetailLevel level) throws IOException {
		RPClass rpClass = owner.getRPClass();

		Definition def = rpClass.getDefinition(DefinitionClass.RPEVENT, name);
		short code = def.getCode();

		if (level == DetailLevel.FULL) {
			// We want to ensure that event text is stored.
			code = -1;
		}

		out.write(code);

		if (code == -1) {
			out.write255LongString(name);
		}

		super.writeObject(out, level);

		RPClass rpclass = this.getRPClass();
		if (rpclass.hasAnyDefinition(DefinitionClass.RPSLOT)) {
			serializeRPSlots(out, level);
		}

	}

	/**
	 * Deserialize
	 *
	 * @param in
	 *            the input serializer
	 */
	@Override
	public void readObject(InputSerializer in) throws IOException {
		short code = in.readShort();

		if (code == -1) {
			name = in.read255LongString();
		} else {
			RPClass rpClass = owner.getRPClass();
			name = rpClass.getName(DefinitionClass.RPEVENT, code);
		}

		super.readObject(in);

		RPClass rpclass = this.getRPClass();
		if (rpclass.hasAnyDefinition(DefinitionClass.RPSLOT)) {
			deserializeRPSlots(in);
		}
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + super.hashCode();
		return result;
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
		if (obj instanceof RPEvent) {
			RPEvent comp = (RPEvent) obj;
			return name.equals(comp.name) && super.equals(this);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuffer tmp = new StringBuffer("RPEvent " + name + " with ");
		tmp.append(super.toString());
		return tmp.toString();
	}

	@Override
	SlotOwner getContainerBaseOwner() {
		return null;
	}

	@Override
	SlotOwner getContainerOwner() {
		return null;
	}

	@Override
	void setContainer(SlotOwner owner, RPSlot slot) {
		// ignore, RPEvent is always top level
	}

}
