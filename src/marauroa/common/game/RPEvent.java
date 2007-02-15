/* $Id: RPEvent.java,v 1.7 2007/02/15 17:28:39 arianne_rpg Exp $ */
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
	public RPEvent() {		
	}

	/**
	 * Constructor 
	 * @param object 
	 * @param name name of the event
	 * @param value value of the event
	 */
	public RPEvent(RPObject object, String name, String value) {
		owner=object;
		put(name,value);
	}
	
	/** This method create a copy of the slot */
	@Override
	public Object clone() {
		RPEvent event = new RPEvent();
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
	 * @return the integer value or an exception if the number is not convertable.
	 */
	public int getInt() {
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
	 * @param the output serializer. 
	 */
	public void writeObject(OutputSerializer out, DetailLevel level) throws IOException {
		RPClass rpClass = owner.getRPClass();
		
		short code=-1;
		try {
			code=rpClass.getCode(DefinitionClass.RPEVENT, name);
		} catch(SyntaxException e) {
			code=-1;
		}

		if (level == DetailLevel.FULL) {
			// We want to ensure that event text is stored.
			code = -1;
		}

		out.write(code);

		if (code == -1) {
			out.write255LongString(name);
		}

		out.write255LongString(value);
	}

	/** 
	 * Deserialize
	 * @param in the input serializer
	 */
	public void readObject(InputSerializer in) throws IOException, ClassNotFoundException {
		short code = in.readShort();
		if (code == -1) {
			name = in.readString();
		} else {
			RPClass rpClass = owner.getRPClass();
			name=rpClass.getName(DefinitionClass.RPEVENT, code);
		}

		value=in.read255LongString();
	}

}
