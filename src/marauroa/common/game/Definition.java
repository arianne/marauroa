/* $Id: Definition.java,v 1.35 2010/06/15 19:10:15 nhnb Exp $ */
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

/**
 * This class stores the definition of an attributes, event or rpslot. This
 * definition contains:
 * - code used as index
 * - name
 * - type of the attribute or event.
 *   It must be one of the following:
 *   - STRING It is a 255 characters long string.
 *   - LONG_STRING It is a 65536 characters long string.
 *   - VERY_LONG_STRING It is 2^32 characters string. Handle this one with care.
 *   - BYTE A 8 bits integer.
 *   - SHORT A 16 bits integer
 *   - INT A 32 bits integer
 *   - FLAG A value that is set or not set.
 * - capacity of the slot
 * - flags to decide the visibility of the atttribute, event or slot. It must be one of the
 * following:
 *   - STANDARD It is an attribute that it is storable and visible.
 *   - PRIVATE It is an attribute that only owner can know about.
 *   - HIDDEN It is an attribute that none knows about.
 *   - VOLATILE It is an attribute that it is not stored at persistence storage.
 *
 *
 * @author miguel
 */
public class Definition implements marauroa.common.net.Serializable {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(Definition.class);

	private static final Validator validator = new Validator();

	/**
	 * This enum determine to which entity the definition belogs: either
	 * attributes, event or slot
	 *
	 * @author miguel
	 */
	public enum DefinitionClass {
		/** This is a static attribute definition. */
		STATIC,
		/** This definition is for an attribute */
		ATTRIBUTE,
		/** This definition is for a RPSlot */
		RPSLOT,
		/** This definition is for a RPEvent */
		RPEVENT,
		/** This definition is for a RPLink */
		RPLINK
	}

	/** Define the possible types of an attribute or event */
	public enum Type {
		/** No type */
		NOTYPE,
		/** a string */
		VERY_LONG_STRING,
		/** a string of up to 65536 chars long */
		LONG_STRING,
		/** a string of up to 255 chars long */
		STRING,
		/** an float number of 32 bits */
		FLOAT,
		/** an integer of 32 bits */
		INT,
		/** an integer of 16 bits */
		SHORT,
		/** an integer of 8 bits */
		BYTE,
		/** an boolean attribute that either is present or not. */
		FLAG,
		/** a map attribute **/
		MAP
	}

	/* Visibility of a attribute/event/slot */
	/** The attribute is visible and stored in database */
	final public static byte STANDARD = 0;

	/** The attribute is ONLY visible for owner of the object */
	final public static byte PRIVATE = 1 << 0;

	/** The attribute is invisible and so only server related */
	final public static byte HIDDEN = 1 << 1;

	/** The attribute should not be stored in the database */
	final public static byte VOLATILE = 1 << 2;

	/** the type of definition we have: ATTRIBUTE, RPSLOT, RPLINK or RPEVENT */
	private DefinitionClass clazz;

	/** an unique code that is assigned at RPClass to identify this definition */
	private short code;

	/** the name of the object that is defined */
	private String name;

	/** In case of a static attribute, it stores the value of the attribute. */
	private String value;

	/**
	 * if it is a RPSLOT, this defines the amount of objects that can be placed
	 * inside otherwise it is 0.
	 */
	private int capacity;

	/**
	 * if it is an ATTRIBUTE, this define the type of the data
	 * associated with this definition.
	 */
	private Type type;

	/**
	 * the flags to show if it is visible, hidden, private, storable or
	 * volatile.
	 */
	private byte flags;

	/**
	 * Constructor used for serialisation
	 */
	public Definition() {
		// declaration needed because there is another constructor
	}

	/**
	 * creates a new definition object bases on the provided definition class
	 *
	 * @param clazz definition class
	 */
	protected Definition(DefinitionClass clazz) {
		this.clazz = clazz;
		code = -1;
	}
	
	@Override
	public String toString() {
		return clazz+": N="+name+" T="+type+" C="+code;
	}

	/**
	 * Method to set the code of a definition. It is called ONLY by RPClass
	 * internally.
	 *
	 * @param code
	 *            the code to assign to the definition.
	 */
	void setCode(short code) {
		this.code = code;
	}

	/**
	 * Creates an Attribute definition
	 *
	 * @param name
	 *            the name of the attribute
	 * @param type
	 *            the type of the attribute
	 * @param flags
	 *            flags options.
	 * @return an Attribute Definition
	 */
	public static Definition defineAttribute(String name, Type type, byte flags) {
		Definition def = new Definition(DefinitionClass.ATTRIBUTE);
		def.name = name;
		def.type = type;
		def.flags = flags;
		def.capacity = 0;
		return def;
	}

	/**
	 * Creates an static attribute definition and give it a value
	 *
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the type of the attribute
	 * @param flags
	 *            flags options.
	 * @return an Attribute Definition
	 */
	public static Definition defineStaticAttribute(String name, String value, byte flags) {
		Definition def = new Definition(DefinitionClass.STATIC);
		def.name = name;
		def.value = value;
		def.type = Type.NOTYPE;
		def.flags = flags;
		def.capacity = 0;
		return def;
	}

	/**
	 * Creates an Event definition
	 *
	 * @param name
	 *            the name of the event
	 * @param flags
	 *            flags options.
	 * @return an Event Definition
	 */
	public static Definition defineEvent(String name, byte flags) {
		Definition def = new Definition(DefinitionClass.RPEVENT);
		def.name = name;
		def.type = Type.NOTYPE;
		def.flags = flags;
		def.capacity = 0;
		return def;
	}

	/**
	 * Creates a RPSLot definition
	 *
	 * @param name
	 *            the name of the slot
	 * @param capacity
	 *            the capacity of the slot
	 * @param flags
	 *            flags options.
	 * @return an RPSlot Definition
	 */
	public static Definition defineSlot(String name, int capacity, byte flags) {
		Definition def = new Definition(DefinitionClass.RPSLOT);
		def.name = name;
		def.capacity = capacity;
		def.flags = flags;
		def.type = Type.NOTYPE;
		return def;
	}

	/**
	 * Creates a RPLink definition
	 *
	 * @param name
	 *            the name of the slot
	 * @param flags
	 *            flags options.
	 * @return an RPSlot Definition
	 */
	public static Definition defineLink(String name, byte flags) {
		Definition def = new Definition(DefinitionClass.RPLINK);
		def.name = name;
		def.capacity = 0;
		def.flags = flags;
		def.type = Type.NOTYPE;
		return def;
	}

	/**
	 * Returns the code of this definition
	 *
	 * @return definition's code
	 */
	public short getCode() {
		return code;
	}

	/**
	 * Returns the DefinitionClass
	 *
	 * @return DefinitionClass
	 */
	public DefinitionClass getDefinitionClass() {
		return clazz;
	}

	/**
	 * Returns the name of the definition
	 *
	 * @return definition's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of a static attribute.
	 *
	 * @return the value of a static attribute.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Returns the type of the definition
	 *
	 * @return definition's type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the capacity of the definition
	 *
	 * @return definition's capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * Returns the flags of the definition
	 *
	 * @return definition's flags
	 */
	public byte getFlags() {
		return flags;
	}

	/**
	 * Sets the value name
	 *
	 * @param name
	 *            definition name
	 */
	public void setName(String name) {
		this.name = name;

	}

	/**
	 * Sets the type of the definition: BYTE, INT, SHORT, STRING, ...
	 *
	 * @param type
	 *            the type of the definition
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Sets the slot capacity. Use 0 for non limited.
	 *
	 * @param capacity
	 *            its capacity.
	 */
	public void setCapacity(byte capacity) {
		this.capacity = capacity;
	}

	/**
	 * Set the definition flags: VOLATILE, HIDDEN, PRIVATE, ...
	 *
	 * @param flags
	 *            the flags to set.
	 */
	public void setFlags(byte flags) {
		this.flags = flags;
	}

	/**
	 * Returns if this definition is visible ( it is not hidden nor private )
	 *
	 * @return true if it is visible
	 */
	public boolean isVisible() {
		return ((flags & (HIDDEN | PRIVATE)) == 0);
	}

	/**
	 * Returns true if the attribute is private.
	 *
	 * @return true if it is private
	 */
	public boolean isPrivate() {
		return ((flags & PRIVATE) == PRIVATE);
	}

	/**
	 * Returns true if the attribute is Hidden.
	 *
	 * @return true if it is hidden
	 */
	public boolean isHidden() {
		return ((flags & HIDDEN) == HIDDEN);
	}

	/**
	 * Return the storability of the attribute whose name is name for this
	 * rpclass
	 *
	 * @return true if is it storable
	 */
	public boolean isStorable() {
		return ((flags & VOLATILE) == 0);
	}

	/**
	 * Deserializes an attribute or event from the input serializer
	 *
	 * @param in
	 *            the input serializer
	 * @return the value
	 * @throws java.io.IOException
	 *             if there is any problem deserializing the object
	 */
	public String deserialize(marauroa.common.net.InputSerializer in) throws java.io.IOException {
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
				return Short.toString(in.readShort());
			case BYTE:
				return Byte.toString(in.readByte());
			case FLAG:
				return "";
		}

		return null;
	}

	/**
	 * Serializes an attribute or even whose value is value into the output
	 * serializer
	 *
	 * @param value
	 *            the value of the event/attribute
	 * @param out
	 *            the output serializer
	 * @throws IOException
	 *             if there is any problem on the serialization
	 */
	public void serialize(String value, marauroa.common.net.OutputSerializer out)
	        throws IOException {		
		try {
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
	        		 * It is empty because it is a flag and so, it is already present.
	        		 */
	        		break;
	        	default:
	        		/* NOTE: Must never happen */
	        		logger.error("got unknown attribute(" + name + ") type:" + type);
	        		break;
	        }
        } catch (IOException e) {
        	logger.warn("Exception caused by name="+name+" type="+type+" code?"+code+" value="+value,e);
	        throw e;
        } catch (NullPointerException e) {
			logger.warn("Exception caused by name=" + name + " type=" + type + " code=" + code
			        + " value=" + value, e);
			throw e;
		}
	}

	/**
	 * validates 
	 *
	 * @param value
	 *            the value of the event/attribute
	 * @throws IllegalArgumentException
	 *             if there is any problem on the serialization
	 */
	public void validate(String value) throws IllegalArgumentException {
		try {
			switch (type) {
				case VERY_LONG_STRING:
					validator.validateVeryLongString(value);
					break;
				case LONG_STRING:
					validator.validate65536LongString(value);
					break;
				case STRING:
					validator.validate255LongString(value);
					break;
				case FLOAT:
					validator.validateFloat(value);
					break;
				case INT:
					validator.validateInteger(value);
					break;
				case SHORT:
					validator.validateShort(value);
					break;
				case BYTE:
					validator.validateByte(value);
					break;
				case FLAG:
					// values of flags are ignored
					break;
				case MAP:
					validator.validateMap(value);
					break;
				default:
					/* NOTE: Must never happen */
					logger.error("got unknown attribute(" + name + ") type:" + type);
					break;
				}
		} catch (RuntimeException e) {
			throw new IllegalArgumentException(e.getMessage() + " caused by name=" + name + " type=" + type + " code=" + code + " value=" + value,e);
		}
	}

	/** Serialize the object into the output */
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		out.write((byte) clazz.ordinal());
		out.write(code);
		out.write(name);
		out.write(capacity);

		/* Serialize value only if it is distinct of null */
		if (value == null) {
			out.write((byte) 0);
		} else {
			out.write((byte) 1);
			out.write255LongString(value);

		}
		out.write((byte) type.ordinal());
		out.write(flags);
	}

	/** Fill the object from data deserialized from the serializer */
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException {
		clazz = DefinitionClass.values()[in.readByte()];
		code = in.readShort();
		name = in.readString();
		capacity=in.readInt();

		/* If value is 0 that means that attribute is null */
		if (in.readByte() == 1) {
			value = in.read255LongString();
		}

		type = Type.values()[in.readByte()];
		flags = in.readByte();
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = PRIME * result + code;
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((type == null) ? 0 : type.hashCode());
		result = PRIME * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object ot) {
		if (!(ot instanceof Definition)) {
			return false;
		}

		Definition def = (Definition) ot;

		boolean result = clazz.equals(def.clazz) 
				&& code == def.code 
				&& capacity == def.capacity
		        && flags == def.flags 
		        && name.equals(def.name) 
		        && type == def.type;
		if (result) {
			if (value == null) {
				result = (def.value == null);
			} else {
				result = value.equals(def.value);
			}
		}
		return result;
	}

}
