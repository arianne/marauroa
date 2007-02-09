package marauroa.common.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.game.Definition.Type;

public class RPClass implements marauroa.common.net.Serializable {
	/** This is a singleton-like Map that stores classes and their names */
	private static Map<String, RPClass> rpClassList = new LinkedHashMap<String, RPClass>();

	/** The class name */
	private String name;

	/** The superclass of this or null if it doesn't have it. */
	private RPClass parent;
	
	private Map<String, Definition> attributes;
	private Map<String, Definition> rpevents;
	private Map<String, Definition> rpslots;
	
	/** Constructor */
	public RPClass() {
		name=null;
		parent = null;
		attributes = new HashMap<String, Definition>();
		rpevents = new HashMap<String, Definition>();
		rpslots = new HashMap<String, Definition>();
	}

	public RPClass(String name) {
		this();
		
		this.name=name;

		/* Any class stores these attributes. */
		add(Type.ATTRIBUTE, "id", Definition.INT, Definition.STANDARD);
		add(Type.ATTRIBUTE, "clientid", Definition.INT, (byte)(Definition.HIDDEN |Definition.VOLATILE));
		add(Type.ATTRIBUTE, "zoneid", Definition.STRING, Definition.HIDDEN);
		add(Type.ATTRIBUTE, "#db_id", Definition.INT, Definition.HIDDEN);
		add(Type.ATTRIBUTE, "type", Definition.STRING, Definition.STANDARD);

		rpClassList.put(name, this);
	}

	/** Returns true if the global list contains the name rpclass */
	public static boolean hasRPClass(String name) {
		return rpClassList.containsKey(name);
	}

	/** Returns the name rpclass from the global list */
	public static RPClass getRPClass(String name) {
		return rpClassList.get(name);
	}

	/** This method sets the parent of this rpclass */
	public void isA(RPClass parent) {
		this.parent = parent;
	}

	/** This method sets the parent of this rpclass */
	public void isA(String parent) {
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
	
	/** Returns the name of the rpclass */
	public String getName() {
		return name;
	}
	

	private short lastCode = 0;
	private List<String> definitions = new LinkedList<String>();

	private short getValidCode(String name) {
		if (definitions.contains(name)) {
			throw new SyntaxException(name);
		}
		
		definitions.add(name);
		
		return ++lastCode;
	}
	
	public void add(Definition.Type clazz, String name, byte type, byte flags) {
		Definition def=null;
		switch(clazz) {
		case ATTRIBUTE:
			def=Definition.defineAttribute(name, type, flags);
			attributes.put(name,def);
			break;
		case RPEVENT:
			def=Definition.defineEvent(name, type, flags);
			rpevents.put(name,def);
			break;
		case RPSLOT:
			def=Definition.defineSlot(name,  type, flags);
			rpslots.put(name,def);
			break;
		}
		
		def.setCode(getValidCode(name));
	}

	public Definition getDefinition(Definition.Type clazz, String name) {
		Definition def=null;
		
		switch(clazz) {
		case ATTRIBUTE:
			def=attributes.get(name);
			break;
		case RPEVENT: 
			def=rpevents.get(name);
			break;
		case RPSLOT:
			def=rpslots.get(name);
			break;
		}

		if(def==null && parent !=null) {
			return parent.getDefinition(clazz, name);
		}

		return def;
	}

	/** Returns the code of the attribute/event/slot whose name is name for this rpclass */
	public short getCode(Definition.Type clazz, String name) {		
		Definition def = getDefinition(clazz, name);
		
		if(def!=null) {
			return def.getCode();
		}
		
		if(def==null && parent !=null) {
			return parent.getCode(clazz, name);
		}
		
		throw new SyntaxException(name);
	}

	/** Returns the name of the attribute whose code is code for this rpclass */
	public String getName(Definition.Type clazz, short code) {
		Map<String,Definition> list=null;

		switch(clazz) {
		case ATTRIBUTE:
			list=attributes;
			break;
		case RPEVENT: 
			list=rpevents;
			break;
		case RPSLOT:
			list=rpslots;
			break;
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


	/** Serialize the object into the output */
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		out.write(name);

		if (parent == null) {
			out.write((byte) 0);
		} else {
			out.write((byte) 1);
			out.write(parent.name);
		}

		List<Map<String,Definition>> list=new LinkedList<Map<String,Definition>>();
		list.add(attributes);
		list.add(rpslots);
		list.add(rpevents);
		
		for(Map<String,Definition> definitions: list) {
			out.write(definitions.size());
			for (Definition desc : definitions.values()) {
				out.write(desc);
			}
		}

	}

	/** Fill the object from data deserialized from the serializer */
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException {
		name = in.readString();

		byte parentPresent = in.readByte();
		if (parentPresent == 1) {
			isA(in.readString());
		}

		List<Map<String,Definition>> list=new LinkedList<Map<String,Definition>>();
		list.add(attributes);
		list.add(rpslots);
		list.add(rpevents);
		
		for(Map<String,Definition> definitions: list) {
			int size = in.readInt();
			for (int i = 0; i < size; ++i) {
				Definition desc = (Definition) in.readObject(new Definition());
				definitions.put(desc.getName(), desc);
			}
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
	}}
