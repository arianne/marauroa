package marauroa.common.game;

import java.io.IOException;

import marauroa.common.Log4J;

import org.apache.log4j.Logger;

/**
 * This class stores the definition of an attributes, event or rpslot.
 * This definition contains:
 * - code used as index
 * - name 
 * - type of the attribute or event
 * - capacity of the slot
 * - flags to decide the visibility of the atttribute, event or slot.
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
	
	Type clazz;
	public short code;
	public String name;
	public byte capacity;
	public byte type;
	public byte flags;
	
	public Definition() {		
	}
	
	protected Definition(Type clazz) {
		this.clazz=clazz;
		code=-1;
	}
	
	public void setCode(short code) {
		this.code=code;
	}
	
	public static Definition defineAttribute(String name, byte type, byte flags) {
		Definition def=new Definition(Type.ATTRIBUTE);
		def.name=name;
		def.type=type;
		def.flags=flags;
		def.capacity=0;
		return def;
	}
	
	public static Definition defineEvent(String name, byte type, byte flags) {
		Definition def=new Definition(Type.RPEVENT);
		def.name=name;
		def.type=type;
		def.flags=flags;
		def.capacity=0;
		return def;
	}
	
	public static Definition defineSlot(String name, byte capacity, byte flags) {
		Definition def=new Definition(Type.RPSLOT);
		def.name=name;
		def.capacity=capacity;
		def.flags=flags;
		def.type=0;
		return def;
	}
	
	public short getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}

	public byte getType() {
		return type;
	}
	
	public byte getCapacity() {
		return capacity;
	}
	
	public byte getFlags() {
		return flags;
	}

	public boolean isVisible() {
		return ((flags & (HIDDEN | PRIVATE)) == 0);
	}

	/**
	 * Returns true if the attribute is private.
	 * @param name the name of the attribute.
	 * @return true if it is private
	 */
	public boolean isPrivate() {
		return ((flags & PRIVATE) == PRIVATE);
	}

	/**
	 * Returns true if the attribute is Hidden.
	 * @param name the name of the attribute.
	 * @return
	 */
	public boolean isHidden() {
		return ((flags & HIDDEN) == HIDDEN);
	}

	/**
	 * Return the storability of the attribute whose name is name for this rpclass
	 */
	public boolean isStorable() {
		return ((flags & VOLATILE) == 0);
	}
	
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
	
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		out.write((byte)clazz.get());
		out.write(code);
		out.write(name);
		out.write(type);
		out.write(flags);
	}

	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException {
		clazz = Type.getType(in.readByte());
		code = in.readShort();
		name = in.readString();
		type = in.readByte();
		flags = in.readByte();
	}
}
