/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;

/**
 * An RPClass is a entity that define the attributes, events and slots of an
 * Object.
 * <b>TODO:</b> Creating a new RPClass does in fact add it to a global list of RPClasses.
 *   Search for a way of making this in a different way so test added for equality works.
 *
 * <p>
 * The idea behind RPClass is not define members as a OOP language but to save
 * bandwidth usage by replacing these members text definitions with a short
 * integer.
 * <p>
 * Also RPClass define a set of properties over attributes, events and slots,
 * like being private, hidden or volatile.
 * <p>
 * It is very important that if you extend a class with isA, you completely
 * define the superclass before calling isA method.<br>
 * For example:
 * <pre>
 *   RPClass foo=new RPClass("foo");
 *   foo.add(....)
 *   foo.add(....)
 *   foo.add(....)
 *
 *   RPClass bar=new RPClass("bar");
 *   bar.isA(foo);
 *   bar.add(....)
 *   bar.add(....)
 * </pre>
 * </pre>
 *
 *
 * @see Definition
 *
 * @author miguel
 */
public class RPClass implements marauroa.common.net.Serializable {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPClass.class);

	/** This is a singleton-like Map that stores classes and their names */
	private static Map<String, RPClass> rpClassList = new LinkedHashMap<String, RPClass>();

	/** The class name */
	private String name;

	/** The superclass of this or null if it doesn't have it. */
	private RPClass parent;

	/** Stores static attributes definition */
	private Map<String, Definition> staticattributes;

	/**
	 * Stores attributes definition. The main difference between static and
	 * non-static attributes is that the first one are not settable once they
	 * are defined at RPClass.
	 */
	private Map<String, Definition> attributes;

	/** Stores RPEvents definitions */
	private Map<String, Definition> rpevents;

	/** Stores RPSlots definitions */
	private Map<String, Definition> rpslots;

	/** Stores RPLink definitions */
	private Map<String, Definition> rplinks;

	/** Is this class baked (parent definitions copied into this class)? */
	private boolean baked = false;

	/**
	 * Constructor Only used in serialization.
	 */
	public RPClass() {
		name = null;
		parent = null;

		staticattributes = new HashMap<String, Definition>();
		attributes = new HashMap<String, Definition>();
		rpevents = new HashMap<String, Definition>();
		rpslots = new HashMap<String, Definition>();
		rplinks = new HashMap<String, Definition>();
	}

	/**
	 * Constructor. It adds the RPClass to a global list of rp classes.
	 *
	 * @param name
	 *            the class name
	 */
	public RPClass(String name) {
		this();

		this.name = name;

		/* Any class stores these attributes. */
		add(DefinitionClass.ATTRIBUTE, "id", Type.INT, Definition.STANDARD);
		add(DefinitionClass.ATTRIBUTE, "zoneid", Type.STRING, Definition.HIDDEN);
		add(DefinitionClass.ATTRIBUTE, "#clientid", Type.INT,
		        (byte) (Definition.HIDDEN | Definition.VOLATILE));
		add(DefinitionClass.ATTRIBUTE, "#db_id", Type.INT, Definition.HIDDEN);

		/*
		 * We want to avoid shadow redefinitions of classes that are very hard to trace.
		 */
		if (rpClassList.containsKey(name)) {
			throw new IllegalArgumentException("Duplicated class name: "+name);
		}

		rpClassList.put(name, this);
	}

	@Override
	public String toString() {
		StringBuffer os = new StringBuffer();
		os.append("RPCLASS name: "+name+"\n");
		os.append("isa: "+(parent!=null?parent.getName():null)+"\n");

		for(Definition d: staticattributes.values()) {
			os.append(d+"\n");
		}
		for(Definition d: attributes.values()) {
			os.append(d+"\n");
		}
		for(Definition d: rpslots.values()) {
			os.append(d+"\n");
		}
		for(Definition d: rplinks.values()) {
			os.append(d+"\n");
		}
		for(Definition d: rpevents.values()) {
			os.append(d+"\n");
		}
		os.append("\n");

		return os.toString();
	}

	/**
	 * Returns true if the global list contains the name rpclass
	 *
	 * @param name
	 *            the class name to query
	 * @return true if the global list contains the name rpclass
	 */
	public static boolean hasRPClass(String name) {
		return rpClassList.containsKey(name);
	}

	/**
	 * Returns the name rpclass from the global list
	 *
	 * @param name
	 *            the name of the class
	 * @return the class or null if it doesn't exist.
	 */
	public static RPClass getRPClass(String name) {
		return rpClassList.get(name);
	}


	/**
	 * gets the RPClass object, if the class is defined and the default calss otherwise
	 *
	 * @param name name of rpclass
	 * @return RPClass of that name or default
	 */
	static RPClass getRPClassOrDefault(String name) {
		RPClass rpclassObject = rpClassList.get(name);
		if (rpclassObject == null) {
			rpclassObject = RPClass.getBaseRPObjectDefault();
		}
		return rpclassObject;
	}

	/**
	 * This method sets the parent of this rpclass
	 *
	 * @param parent
	 *            the super class of this class.
	 */
	public void isA(RPClass parent) {
		this.parent = parent;
		lastCode = parent.lastCode;
	}

	/**
	 * This method sets the parent of this rpclass
	 *
	 * @param parent
	 *            the super class of this class.
	 */
	public void isA(String parent) {
		this.parent = getRPClass(parent);
		lastCode = this.parent.lastCode;
	}

	/**
	 * This method returns true if it is a subclass of parentClass or if it is
	 * the class itself.
	 *
	 * @param parentClass
	 *            the super class of this class
	 * @return true if it is a subclass of parentClass or if it is the class
	 *         itself.
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

	/**
	 * Returns the name of the rpclass
	 *
	 * @return the name of the rpclass
	 */
	public String getName() {
		return name;
	}

	/**
	 * gets the parent class or <code>null</code> if this is a root class.
	 *
	 * @return RPClass or <code>null</code>.
	 */
	public RPClass getParent() {
		return parent;
	}

	private short lastCode = 0;

	private List<String> definitions = new LinkedList<String>();

	private short getValidCode(DefinitionClass clazz, String name) {
		if (definitions.contains(name)) {
			throw new SyntaxException(name);
		}

		definitions.add(name);

		return ++lastCode;
	}

	/**
	 * Adds a definition of an attribute with the given type and
	 * with the standard flags ( STORABLE and VISIBLE )
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param name
	 *            name of the definition
	 * @param type
	 *            type or capacity if it is an slot
	 */
	public void add(Definition.DefinitionClass clazz, String name, Type type) {
		add(clazz,name,type, Definition.STANDARD);
	}

	/**
	 * Adds a definition of an attribute with the given type and
	 * flags.
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param name
	 *            name of the definition
	 * @param type
	 *            type or capacity if it is an slot
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void add(Definition.DefinitionClass clazz, String name, Type type, byte flags) {
		if (clazz != DefinitionClass.ATTRIBUTE) {
			throw new SyntaxException(name);
		}

		Definition def = Definition.defineAttribute(name, type, flags);
		attributes.put(name, def);

		def.setCode(getValidCode(clazz, name));
	}

	/**
	 * Adds a definition of an event or rplink with the given flags.
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param name
	 *            name of the definition
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void add(Definition.DefinitionClass clazz, String name, byte flags) {
		Definition def = null;

		switch (clazz) {
			case RPLINK:
				def = Definition.defineEvent(name, flags);
				rplinks.put(name, def);
				break;

			case RPEVENT:
				def = Definition.defineEvent(name, flags);
				rpevents.put(name, def);
				break;

			default:
				throw new SyntaxException(name);
		}

		def.setCode(getValidCode(clazz, name));
	}

	/**
	 * Adds a definition of an slot with the given capacity and
	 * flags.
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param name
	 *            name of the definition
	 * @param capacity
	 *            capacity if it is an slot
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void add(Definition.DefinitionClass clazz, String name, int capacity, byte flags) {
		if (clazz != DefinitionClass.RPSLOT) {
			throw new SyntaxException(name);
		}

		Definition def = Definition.defineSlot(name, capacity, flags);
		rpslots.put(name, def);

		def.setCode(getValidCode(clazz, name));
	}

	/**
	 * Adds a static definition of an attribute that will be set for any object
	 * of the class. Its value must be set as a string, but it can be accessed
	 * later using Attributes.get method. NOTE: This type of attributes can't be
	 * set.
	 *
	 * @param clazz
	 *            It must be DefinitionClass.STATIC
	 * @param name
	 *            name of the static attribute
	 * @param value
	 *            value of the attribute
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void add(Definition.DefinitionClass clazz, String name, String value, byte flags) {
		if (clazz != DefinitionClass.STATIC) {
			throw new SyntaxException(name);
		}

		Definition def = Definition.defineStaticAttribute(name, value, flags);
		staticattributes.put(name, def);

		def.setCode(getValidCode(clazz, name));
	}

	/**
	 * Adds a definition of an attribute with the given type and
	 * flags.
	 *
	 * @param name
	 *            name of the definition
	 * @param type
	 *            type or capacity if it is an slot
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void addAttribute(String name, Type type, byte flags) {
		add(DefinitionClass.ATTRIBUTE, name, type, flags);
	}

	/**
	 * Adds a definition of an attribute with the given type and
	 * standard flags: VISIBLE and STORABLE.
	 *
	 * @param name
	 *            name of the definition
	 * @param type
	 *            type or capacity if it is an slot
	 */
	public void addAttribute(String name, Type type) {
		add(DefinitionClass.ATTRIBUTE, name, type, Definition.STANDARD);
	}

	/**
	 * Adds a static definition of an attribute that will be set for any object
	 * of the class. Its value must be set as a string, but it can be accessed
	 * later using Attributes.get method. NOTE: This type of attributes can't be
	 * set.
	 *
	 * @param name
	 *            name of the static attribute
	 * @param value
	 *            value of the attribute
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void addStaticAttribute(String name, String value, byte flags) {
		add(DefinitionClass.STATIC, name, value, flags);
	}

	/**
	 * Adds a definition of an slot with the given capacity and
	 * flags.
	 *
	 * @param name
	 *            name of the definition
	 * @param capacity
	 *            capacity if it is an slot
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void addRPSlot(String name, int capacity, byte flags) {
		add(DefinitionClass.RPSLOT, name, capacity, flags);
	}

	/**
	 * Adds a definition of an slot with the given capacity and
	 * standard flags: VISIBLE and STORABLE
	 *
	 * @param name
	 *            name of the definition
	 * @param capacity
	 *            capacity if it is an slot
	 */
	public void addRPSlot(String name, int capacity) {
		add(DefinitionClass.RPSLOT, name, capacity, Definition.STANDARD);
	}

	/**
	 * Adds a definition of a rplink with the given flags.
	 *
	 * @param name
	 *            name of the definition
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void addRPLink(String name, byte flags) {
		add(DefinitionClass.RPLINK, name, flags);
	}

	/**
	 * Adds a definition of an event with the given flags.
	 *
	 * @param name
	 *            name of the definition
	 * @param flags
	 *            like visibility, storability, etc...
	 */
	public void addRPEvent(String name, byte flags) {
		add(DefinitionClass.RPEVENT, name, flags);
	}

	/**
	 * Returns the definition object itself.
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param name
	 *            name of the definition
	 * @return this definition object or null if it is not found
	 */
	public Definition getDefinition(Definition.DefinitionClass clazz, String name) {
		Definition def = null;

		switch (clazz) {
			case STATIC:
				def = staticattributes.get(name);
				break;
			case ATTRIBUTE:
				def = attributes.get(name);
				break;
			case RPEVENT:
				def = rpevents.get(name);
				break;
			case RPSLOT:
				def = rpslots.get(name);
				break;
			case RPLINK:
				def = rplinks.get(name);
				break;
			default:
				throw new SyntaxException("Class not found: " + clazz);
		}

		if (def == null && parent != null && !baked) {
			return parent.getDefinition(clazz, name);
		}

		return def;
	}


	/**
	 * Returns a list of all definitions of this class (not including parent classes)
	 *
	 * @return list of definitions
	 */
	public List<Definition> getDefinitions() {
		List<Definition> res = new LinkedList<Definition>();
		res.addAll(staticattributes.values());
		res.addAll(attributes.values());
		res.addAll(rpevents.values());
		res.addAll(rpslots.values());
		res.addAll(rplinks.values());
		return res;
	}


	/**
	 * Bakes the RPClass by including copies of all the definitions of the 
	 * parent class to improve performance.
	 */
	public void bake() {
		if (!baked) {
			RPClass aParent = parent;
			// TODO: inverse order so that attributes defined lower in the hierarchy win
			while (aParent != null) {
				staticattributes.putAll(aParent.staticattributes);
				attributes.putAll(aParent.attributes);
				rpevents.putAll(aParent.rpevents);
				rpslots.putAll(aParent.rpslots);
				rplinks.putAll(aParent.rplinks);
				aParent = aParent.parent;
			}
		}
		baked = true;
	}

	/**
	 * Bales all RPClasses to optimize performance.
	 */
	public static void bakeAll() {
		for (RPClass rpClass : rpClassList.values()) {
			rpClass.bake();
		}
	}

	/**
	 * Checks whether there is at least one definition of the specified DefinitionClass.
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @return true, if there is a DefinitionClass, false otherwise
	 */
	boolean hasAnyDefinition(Definition.DefinitionClass clazz) {
		boolean res = false;

		switch (clazz) {
			case STATIC:
				res = !staticattributes.isEmpty();
				break;
			case ATTRIBUTE:
				res = !attributes.isEmpty();
				break;
			case RPEVENT:
				res = !rpevents.isEmpty();
				break;
			case RPSLOT:
				res = !rpslots.isEmpty();
				break;
			case RPLINK:
				res = !rplinks.isEmpty();
				break;
			default:
				throw new SyntaxException("Class not found: " + clazz);
		}

		if (!res && (parent != null)) {
			return parent.hasAnyDefinition(clazz);
		}

		return res;
	}

	/**
	 * Returns true if name attributes, slot, event or rplink exists in this RPClass or
	 * any of its ancestors.
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param name
	 *            name of the definition
	 * @return returns true if name attributes, slot, event or rplink exists in this RPClass or
	 * any of its ancestors.
	 */
	public boolean hasDefinition(Definition.DefinitionClass clazz, String name) {
		return getDefinition(clazz, name)!=null;
	}

	/**
	 * Returns the code of the attribute/event/slot whose name is name for this
	 * rpclass
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param name
	 *            name of the definition
	 * @return definition code
	 * @throws SyntaxException
	 *             if the definition is not found.
	 */
	public short getCode(Definition.DefinitionClass clazz, String name) {
		Definition def = getDefinition(clazz, name);

		if (def != null) {
			return def.getCode();
		}

		if (parent != null) {
			return parent.getCode(clazz, name);
		}

		throw new SyntaxException(name);
	}

	/**
	 * Returns the name of the attribute whose code is code for this rpclass
	 *
	 * @param clazz
	 *            type of definition ( attribute, event or slot )
	 * @param code
	 *            definition code
	 * @return name of the definition
	 * @throws SyntaxException
	 *             if the definition is not found.
	 */
	public String getName(Definition.DefinitionClass clazz, short code) {
		Map<String, Definition> list = null;

		switch (clazz) {
			case ATTRIBUTE:
				list = attributes;
				break;
			case RPEVENT:
				list = rpevents;
				break;
			case RPSLOT:
				list = rpslots;
				break;
			case RPLINK:
				list = rplinks;
				break;
			default:
				throw new SyntaxException("Class not found: " + clazz);
		}

		if (list == null) {
			logger.warn("Unexpected type of Definition: " + clazz);
			throw new SyntaxException(code);
		}

		for (Definition desc : list.values()) {
			if (desc.getCode() == code) {
				return desc.getName();
			}
		}

		if (parent != null) {
			return parent.getName(clazz, code);
		}

		throw new SyntaxException(code);
	}

	/**
	 * Serialize the object into the output
	 *
	 * @param out
	 *            the output serializer
	 * @throws IOException
	 *             if there is any problem serializing.
	 */
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		out.write(name);

		if (parent == null) {
			out.write((byte) 0);
		} else {
			out.write((byte) 1);
			out.write(parent.name);
		}

		List<Map<String, Definition>> list = new LinkedList<Map<String, Definition>>();
		list.add(attributes);
		list.add(rpslots);
		list.add(rpevents);
		list.add(rplinks);
		list.add(staticattributes);

		for (Map<String, Definition> definitions : list) {
			out.write(definitions.size());
			for (Definition desc : definitions.values()) {
				out.write(desc);
			}
		}
	}

	/**
	 * Fill the object from data deserialized from the serializer
	 *
	 * @param in
	 *            input serializer
	 * @throws IOException
	 *             if there is any problem in the serialization.
	 */
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException {
		name = in.readString();

		byte parentPresent = in.readByte();
		if (parentPresent == 1) {
			isA(in.readString());
		}

		List<Map<String, Definition>> list = new LinkedList<Map<String, Definition>>();
		list.add(attributes);
		list.add(rpslots);
		list.add(rpevents);
		list.add(rplinks);
		list.add(staticattributes);

		for (Map<String, Definition> definitions : list) {
			int size = in.readInt();
			for (int i = 0; i < size; ++i) {
				Definition desc = (Definition) in.readObject(new Definition());
				definitions.put(desc.getName(), desc);
			}
		}

		rpClassList.put(name, this);
	}

	/**
	 * Iterates over the global list of rpclasses
	 *
	 * @return an iterator
	 */
	public static Iterator<RPClass> iterator() {
		return rpClassList.values().iterator();
	}

	/**
	 * Returns the size of the rpclass global list
	 *
	 * @return number of defined classes.
	 */
	public static int size() {
		return rpClassList.size();
	}

	/**
	 * We need a default class for some cases where attributes used are not
	 * known at compile time.
	 */
	private final static RPClass defaultRPClass = new DefaultRPClass();

	/**
	 * Returns a default rpclass for lazy developers. You won't get any
	 * advantages on the engine by using it.
	 *
	 * @return RPClass
	 */
	public static RPClass getBaseRPObjectDefault() {
		return defaultRPClass;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = PRIME * result + ((name == null) ? 0 : name.hashCode());
		result = PRIME * result + ((parent == null) ? 0 : parent.hashCode());
		result = PRIME * result + ((rpevents == null) ? 0 : rpevents.hashCode());
		result = PRIME * result + ((rpslots == null) ? 0 : rpslots.hashCode());
		result = PRIME * result + ((staticattributes == null) ? 0 : staticattributes.hashCode());
		return result;
	}

	/**
	 * Returns true if two objects are exactly equal
	 *
	 * @param ot
	 *            the object to compare with this one.
	 */
	@Override
	public boolean equals(Object ot) {
		if ( this == ot)  {
			return true;
		}
		if (!(ot instanceof RPClass)) {
			return false;
		}

		RPClass other = (RPClass) ot;
		boolean isEqual;
		if (name==null){
			isEqual = other.name==null;
		} else {
			isEqual = name.equals(other.name);
		}
		if (parent==null){
			isEqual = isEqual && other.parent==null;
		} else {
			isEqual = isEqual && parent.equals(other.parent);
		}

		isEqual = isEqual
		        && staticattributes.equals(other.staticattributes)
		        && attributes.equals(other.attributes)
		        && rpevents.equals(other.rpevents)
		        && rpslots.equals(other.rpslots);

		return isEqual;
	}
}
