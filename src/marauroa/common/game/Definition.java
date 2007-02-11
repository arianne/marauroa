/* $Id: Definition.java,v 1.4 2007/02/11 15:44:27 arianne_rpg Exp $ */
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

import marauroa.common.Log4J;

import org.apache.log4j.Logger;

/**
 * This class stores the definition of an attributes, event or rpslot.
 * This definition contains:
 * - code used as index
 * - name 
 * - type of the attribute or event.
 *   It must be one of the following:
 *   * STRING
 *     It is a 255 characters long string.
 *   * LONG_STRING
 *     It is a 65536 characters long string. 
 *   * VERY_LONG_STRING
 *     It is 2^32 characters string.
 *     Handle this one with care. *     
 *   * BYTE
 *     A 8 bits integer.
 *   * SHORT
 *     A 16 bits integer
 *   * INT
 *     A 32 bits integer
 *   * FLAG
 *     A value that is set or not set.
 * - capacity of the slot
 * - flags to decide the visibility of the atttribute, event or slot.
 *   It must be one of the following:
 *   * STANDARD
 *     It is an attribute that it is storable and visible.
 *   * PRIVATE
 *     It is an attribute that only owner can know about. 
 *   * HIDDEN
 *     It is an attribute that none knows about.
 *   * VOLATILE
 *     It is an attribute that it is not stored at persistence storage.
 * 
 * 
 * @author miguel
 */
public class Definition implements marauroa.common.net.Serializable {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Definition.class);

	/** 
	 * This enum determine to which entity the definition belogs: either attributes, event or slot 
	 * @author miguel
	 */
	public enum Type {
		/** This definition is for an attribute */
		ATTRIBUTE(0),
		/** This definition is for a RPSlot */
		RPSLOT(1),
		/** This definition is for a RPEvent */
		RPEVENT(2);
		
		private byte val;
		
		private Type(int val) {
			this.val=(byte)val;
		}
		
		public byte get() {
			return val;
		}
		
		public static Type getType(int val) {
			for(Type t: Type.values()) {
				if(t.val==val){
					return t;
				}
			}
		
			return null;
		}
	}

	/* Visibility of a attribute/event/slot*/
	/** The attribute is visible and stored in database */
	final public static byte STANDARD = 0;

	/** The attribute is ONLY visible for owner of the object */
	final public static byte PRIVATE = 1 << 0;

	/** The attribute is invisible and so only server related */
	final public static byte HIDDEN = 1 << 1;

	/** The attribute should not be stored in the database */
	final public static byte VOLATILE = 1 << 2;

	/* Type of a attribute/event */
	/** a string */
	final public static byte VERY_LONG_STRING = 1;

	/** a string of up to 255 chars long */
	final public static byte LONG_STRING = 2;

	/** a string of up to 255 chars long */
	final public static byte STRING = 3;

	/** an float number of 32 bits */
	final public static byte FLOAT = 4;

	/** an integer of 32 bits */
	final public static byte INT = 5;

	/** an integer of 16 bits */
	final public static byte SHORT = 6;

	/** an integer of 8 bits */
	final public static byte BYTE = 7;

	/** an boolean attribute that either is present or not. */
	final public static byte FLAG = 8;

	/** the type of definition we have: ATTRIBUTE, RPSLOT or RPEVENT */
	private Type clazz;
	/** an unique code that is assigned at RPClass to identify this definition */
	private short code;
	/** the name of the object that is defined */
	private String name;
	/** if it is a RPSLOT, this defines the amount of objects that can be placed inside 
	 * otherwise it is 0.
	 */
	private byte capacity;
	/** if it is a RPEVENT or an ATTRIBUTE, this define the type of the data associated with
	 * this definition.
	 */ 
	private byte type;
	/** the flags to show if it is visible, hidden, private, storable or volatile. */
	private byte flags;
	
	/** Constructor */
	public Definition() {		
	}
	
	protected Definition(Type clazz) {
		this.clazz=clazz;
		code=-1;
	}
	
	void setCode(short code) {
		this.code=code;
	}

	/**
	 *  Creates an Attribute definition
	 * @param name the name of the attribute
	 * @param type the type of the attribute
	 * @param flags flags options.
	 * @return an Attribute Definition
	 */
	public static Definition defineAttribute(String name, byte type, byte flags) {
		Definition def=new Definition(Type.ATTRIBUTE);
		def.name=name;
		def.type=type;
		def.flags=flags;
		def.capacity=0;
		return def;
	}
	
	/**
	 *  Creates an Event definition
	 * @param name the name of the event
	 * @param type the type of the event
	 * @param flags flags options.
	 * @return an Event Definition
	 */
	public static Definition defineEvent(String name, byte type, byte flags) {
		Definition def=new Definition(Type.RPEVENT);
		def.name=name;
		def.type=type;
		def.flags=flags;
		def.capacity=0;
		return def;
	}
	
	/**
	 *  Creates a RPSLot definition
	 * @param name the name of the slot
	 * @param capacity the capacity of the slot
	 * @param flags flags options.
	 * @return an RPSlot Definition
	 */
	public static Definition defineSlot(String name, byte capacity, byte flags) {
		Definition def=new Definition(Type.RPSLOT);
		def.name=name;
		def.capacity=capacity;
		def.flags=flags;
		def.type=0;
		return def;
	}
	
	/**
	 * Returns the code of this definition 
	 * @return definition's code
	 */
	public short getCode() {
		return code;
	}
	
	/**
	 * Returns the name of the definition
	 * @return definition's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type of the definition
	 * @return definition's type
	 */
	public byte getType() {
		return type;
	}
	
	/**
	 * Returns the capacity of the definition
	 * @return definition's capacity
	 */
	public byte getCapacity() {
		return capacity;
	}
	
	/**
	 * Returns the flags of the definition
	 * @return definition's flags
	 */
	public byte getFlags() {
		return flags;
	}

	/**
	 * Sets the value name
	 * @param name definition name
	 */
	public void setName(String name) {
		this.name=name;
		
	}

	/**
	 * Sets the type of the definition: BYTE, INT, SHORT, STRING, ...
	 * @param type the type of the definition
	 */
	public void setType(byte type) {
		this.type=type;		
	}

	/**
	 * Sets the slot capacity.
	 * Use 0 for non limited.
	 * 
	 * @param capacity its capacity.
	 */
	public void setCapacity(byte capacity) {
		this.capacity=capacity;		
	}

	/**
	 * Set the definition flags: VOLATILE, HIDDEN, PRIVATE, ...
	 * @param flags the flags to set.
	 */
	public void setFlags(byte flags) {
		this.flags=flags;		
	}

	/**
	 * Returns if this definition is visible ( it is not hidden nor private )
	 * @return true if it is visible 
	 */
	public boolean isVisible() {
		return ((flags & (HIDDEN | PRIVATE)) == 0);
	}

	/**
	 * Returns true if the attribute is private.
	 * @return true if it is private
	 */
	public boolean isPrivate() {
		return ((flags & PRIVATE) == PRIVATE);
	}

	/**
	 * Returns true if the attribute is Hidden.
	 * @return true if it is hidden
	 */
	public boolean isHidden() {
		return ((flags & HIDDEN) == HIDDEN);
	}

	/**
	 * Return the storability of the attribute whose name is name for this rpclass
	 * @return true if is it storable
	 */
	public boolean isStorable() {
		return ((flags & VOLATILE) == 0);
	}
	
	/**
	 * Deserializes an attribute or event from the input serializer
	 * @param in the input serializer
	 * @return the value
	 * @throws java.io.IOException if there is any problem deserializing the object
	 * @throws ClassNotFoundException
	 */
	public String deserialize(marauroa.common.net.InputSerializer in) throws java.io.IOException, ClassNotFoundException {
		switch (type) {
		case VERY_LONG_STRING:
			return in.readString();
		case LONG_STRING:
			return in.read65536LongString();
		case STRING:
			return in.read255LongString();
		case FLOAT:
			return Float.toString(in.readFloat());
		case INT:
			return Integer.toString(in.readInt());
		case SHORT:
			return Integer.toString(in.readShort());
		case BYTE:
			return Integer.toString(in.readByte());
		case FLAG:
			return "";
		}
	
		return null;
	}

	/**
	 * Serializes an attribute or even whose value is value into the output serializer 
	 * @param value the value of the event/attribute
	 * @param out the output serializer
	 * @throws IOException if there is any problem on the serialization
	 */
	public void serialize(String value, marauroa.common.net.OutputSerializer out) throws IOException {
		switch (type) {
		case VERY_LONG_STRING:
			out.write(value);
			break;
		case LONG_STRING:
			out.write65536LongString(value);
			break;
		case STRING:
			out.write255LongString(value);
			break;
		case FLOAT:
			out.write(Float.parseFloat(value));
			break;
		case INT:
			out.write(Integer.parseInt(value));
			break;
		case SHORT:
			out.write(Short.parseShort(value));
			break;
		case BYTE:
			out.write(Byte.parseByte(value));
			break;
		case FLAG:
			/*
			 * It is empty because it is a flag and so, it is
			 * already present.
			 */
			break;
		default:
			/* NOTE: Must never happen */
			logger.fatal("got unknown attribute("+name+") type:"+ code);
		break;
		}
	}
		
	/** Serialize the object into the output */
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		out.write((byte)clazz.get());
		out.write(code);
		out.write(name);
		out.write(type);
		out.write(flags);
	}

	/** Fill the object from data deserialized from the serializer */
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException {
		clazz = Type.getType(in.readByte());
		code = in.readShort();
		name = in.readString();
		type = in.readByte();
		flags = in.readByte();
	}

}
