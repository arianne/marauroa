package marauroa.common.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The RPClass class implements a container of attributes with its code, name,
 * type and visibility. It adds support for strict type definition and class
 * hierarchy
 */
public class RPClass implements marauroa.common.net.Serializable {
	/* Visibility */
	/** The attribute is visible */
	final public static byte VISIBLE = 0;

	/** The attribute is ONLY visible for owner of the object */
	final public static byte PRIVATE = 1 << 0;

	/** The attribute is invisible and so only server related */
	final public static byte HIDDEN = 1 << 1;

	/** The attribute should be stored in the database */
	final public static byte STORABLE = 0;

	/** The attribute should not be stored in the database */
	final public static byte VOLATILE = 1 << 2;

	/* Type */
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

	static public class SyntaxException extends RuntimeException {
		private static final long serialVersionUID = 3724525088825394097L;

		public SyntaxException(String offendingAttribute) {
			super("attribute " + offendingAttribute + " isn't defined.");
		}

		public SyntaxException(short offendingAttribute) {
			super("attribute code " + offendingAttribute + " isn't defined.");
		}
	}

	private static Map<String, RPClass> rpClassList = new LinkedHashMap<String, RPClass>();

	private String name;

	private RPClass parent;

	private Map<String, AttributeDesc> attributes;

	private Map<String, RPSlotDesc> slots;

	public RPClass() {
		parent = null;
		attributes = new HashMap<String, AttributeDesc>();
		slots = new HashMap<String, RPSlotDesc>();
	}

	/** This constructor adds the rpclass to the global list of rpclasses. */
	public RPClass(String type) {
		parent = null;
		name = type;
		attributes = new HashMap<String, AttributeDesc>();
		slots = new HashMap<String, RPSlotDesc>();

		add("id", INT);
		add("clientid", INT, (byte) (HIDDEN | VOLATILE));
		add("zoneid", STRING, HIDDEN);
		add("#db_id", INT, HIDDEN);
		add("type", STRING);

		rpClassList.put(type, this);
	}

	/** This method sets the parent of this rpclass */
	public void isA(RPClass parent) {
		this.parent = parent;
	}

	/** This method sets the parent of this rpclass */
	public void isA(String parent) throws SyntaxException {
		this.parent = getRPClass(parent);
	}

	/**
	 * This method returns true if it is a subclass of parentClass or if it is
	 * the class itself.
	 */
	public boolean subclassOf(String parentClass) {
		if (!hasRPClass(parentClass)) {
			return false;
		}

		if (name.equals(parentClass)) {
			return true;
		} else if (parent != null) {
			return parent.subclassOf(parentClass);
		}

		return false;
	}

	static RPClass defaultRPClass;

	/**
	 * Returns a default rpclass for lazy developers. You won't get any
	 * advantages on the engine by using it.
	 */
	public static RPClass getBaseRPObjectDefault() {
		if (defaultRPClass == null) {
			defaultRPClass = new RPClass("") {
				public short getCode(String name) {
					return -1;
				}

				public byte getType(String name) {
					return RPClass.STRING;
				}

				public byte getFlags(String name) {
					if (name.charAt(0) == '!') {
						return RPClass.PRIVATE;
					} else {
						return RPClass.VISIBLE;
					}
				}

				public short getRPSlotCode(String name) {
					return -1;
				}

				public int getRPSlotCapacity(String name) {
					return -1;
				}

				public byte getRPSlotFlags(String name) {
					if (name.charAt(0) == '!') {
						return RPClass.PRIVATE;
					} else {
						return RPClass.VISIBLE;
					}
				}
			};
		}

		return defaultRPClass;
	}

	/**
	 * Returns a default rpclass for lazy developers. You won't get any
	 * advantages on the engine by using it.
	 */
	public static RPClass getBaseRPActionDefault() {
		if (defaultRPClass == null) {
			defaultRPClass = getBaseRPObjectDefault();
		}

		return defaultRPClass;
	}

	/** Adds a visible attribute to the rpclass */
	public boolean add(String name, byte type) {
		return add(name, type, VISIBLE);
	}

	/** Adds a attribute to the rpclass */
	public boolean add(String name, byte type, byte flags) {
		AttributeDesc desc = new AttributeDesc(name, type, flags);
		attributes.put(name, desc);

		return true;
	}

	public boolean addRPSlot(String name, int capacity) {
		RPSlotDesc desc = new RPSlotDesc(name, (byte) capacity, VISIBLE);
		slots.put(name, desc);

		return true;
	}

	public boolean addRPSlot(String name, int capacity, byte flags) {
		RPSlotDesc desc = new RPSlotDesc(name, (byte) capacity, flags);
		slots.put(name, desc);

		return true;
	}

	/** Returns the name of the rpclass */
	public String getName() {
		return name;
	}

	/** Returns the code of the attribute whose name is name for this rpclass */
	public short getCode(String name) throws SyntaxException {
		if (attributes.containsKey(name)) {
			AttributeDesc desc = attributes.get(name);
			return desc.code;
		}

		if (parent != null) {
			return parent.getCode(name);
		}

		throw new SyntaxException(name);
	}

	/** Returns the code of the slot whose name is name for this rpclass */
	public short getRPSlotCode(String name) throws SyntaxException {
		if (slots.containsKey(name)) {
			RPSlotDesc desc = slots.get(name);
			return desc.code;
		}

		if (parent != null) {
			return parent.getRPSlotCode(name);
		}

		throw new SyntaxException("RPSlot " + name);
	}

	/** Returns the name of the attribute whose code is code for this rpclass */
	public String getName(short code) throws SyntaxException {
		for (AttributeDesc desc : attributes.values()) {
			if (desc.code == code) {
				return desc.name;
			}
		}

		if (parent != null) {
			return parent.getName(code);
		}

		throw new SyntaxException(code);
	}

	/** Returns the name of the attribute whose code is code for this rpclass */
	public String getRPSlotName(short code) throws SyntaxException {
		for (RPSlotDesc desc : slots.values()) {
			if (desc.code == code) {
				return desc.name;
			}
		}

		if (parent != null) {
			return parent.getRPSlotName(code);
		}

		throw new SyntaxException("RPSlot " + code);
	}

	/** Returns the type of the attribute whose name is name for this rpclass */
	public byte getType(String name) throws SyntaxException {
		if (attributes.containsKey(name)) {
			AttributeDesc desc = attributes.get(name);
			return desc.type;
		}

		if (parent != null) {
			return parent.getType(name);
		}

		throw new SyntaxException(name);
	}

	public int getRPSlotCapacity(String name) throws SyntaxException {
		if (slots.containsKey(name)) {
			RPSlotDesc desc = slots.get(name);
			return desc.capacity;
		}

		if (parent != null) {
			return parent.getRPSlotCapacity(name);
		}

		throw new SyntaxException("RPSlot " + name);
	}

	/** Returns the flags of the attribute whose name is name for this rpclass */
	public byte getFlags(String name) throws SyntaxException {
		if (attributes.containsKey(name)) {
			AttributeDesc desc = attributes.get(name);
			return desc.flags;
		}

		if (parent != null) {
			return parent.getFlags(name);
		}

		throw new SyntaxException(name);
	}

	/** Returns the flags of the attribute whose name is name for this rpclass */
	public byte getRPSlotFlags(String name) throws SyntaxException {
		if (slots.containsKey(name)) {
			RPSlotDesc desc = slots.get(name);
			return desc.flags;
		}

		if (parent != null) {
			return parent.getRPSlotFlags(name);
		}

		throw new SyntaxException("RPSlot " + name);
	}

	/**
	 * Return the visibility of the attribute whose name is name for this
	 * rpclass
	 */
	public boolean isVisible(String name) {
		byte b = getFlags(name);
		return ((b & (RPClass.HIDDEN | RPClass.PRIVATE)) == 0);
	}

	public boolean isPrivate(String name) {
		byte b = getFlags(name);
		return ((b & RPClass.PRIVATE) == RPClass.PRIVATE);
	}

	public boolean isHidden(String name) {
		byte b = getFlags(name);
		return ((b & RPClass.HIDDEN) == RPClass.HIDDEN);
	}

	/**
	 * Return the storability of the attribute whose name is name for this
	 * rpclass
	 */
	public boolean isStorable(String name) {
		byte b = getFlags(name);
		return ((b & RPClass.VOLATILE) == 0);
	}

	public boolean isRPSlotVisible(String name) {
		byte b = getRPSlotFlags(name);
		return ((b & (RPClass.HIDDEN | RPClass.PRIVATE)) == 0);
	}

	public boolean isRPSlotPrivate(String name) {
		byte b = getRPSlotFlags(name);
		return ((b & RPClass.PRIVATE) == RPClass.PRIVATE);
	}

	public boolean isRPSlotHidden(String name) {
		byte b = getRPSlotFlags(name);
		return ((b & RPClass.HIDDEN) == RPClass.HIDDEN);
	}

	/**
	 * Return the storability of the attribute whose name is name for this
	 * rpclass
	 */
	public boolean isRPSlotStorable(String name) {
		byte b = getRPSlotFlags(name);
		return ((b & RPClass.VOLATILE) == 0);
	}

	/** Returns true if the attribute whose name is name exists for this rpclass */
	public boolean hasAttribute(String name) {
		if (attributes.containsKey(name)) {
			return true;
		}

		if (parent != null) {
			return parent.hasAttribute(name);
		}

		return false;
	}

	/** Returns true if the slot whose name is name exists for this rpclass */
	public boolean hasRPSlot(String name) {
		if (slots.containsKey(name)) {
			return true;
		}

		if (parent != null) {
			return parent.hasRPSlot(name);
		}

		return false;
	}

	/** Returns true if the global list contains the name rpclass */
	public static boolean hasRPClass(String name) {
		if (rpClassList.containsKey(name)) {
			return true;
		}

		return false;
	}

	/** Returns the name rpclass from the global list */
	public static RPClass getRPClass(String name) throws SyntaxException {
		if (rpClassList.containsKey(name)) {
			return rpClassList.get(name);
		}

		throw new SyntaxException(name);
	}

	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws java.io.IOException {
		out.write(name);

		if (parent == null) {
			out.write((byte) 0);
		} else {
			out.write((byte) 1);
			out.write(parent.name);
		}

		out.write(attributes.size());
		for (AttributeDesc desc : attributes.values()) {
			out.write(desc);
		}

		out.write(slots.size());
		for (RPSlotDesc desc : slots.values()) {
			out.write(desc);
		}
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws java.io.IOException, java.lang.ClassNotFoundException {
		name = in.readString();

		byte parentPresent = in.readByte();
		if (parentPresent == 1) {
			isA(in.readString());
		}

		int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			AttributeDesc desc = (AttributeDesc) in
					.readObject(new AttributeDesc());
			attributes.put(desc.name, desc);
		}

		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			RPSlotDesc desc = (RPSlotDesc) in.readObject(new RPSlotDesc());
			slots.put(desc.name, desc);
		}

		rpClassList.put(name, this);
	}

	/** Iterates over the global list of rpclasses */
	public static Iterator<RPClass> iterator() {
		return rpClassList.values().iterator();
	}

	/** Returns the size of the rpclass global list */
	public static int size() {
		return rpClassList.size();
	}
}
