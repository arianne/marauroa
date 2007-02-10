package marauroa.common.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.game.Definition.Type;

/**
 * An RPClass is a entity that define the attributes, events and slots of an Object.
 * <p>
 * The idea behind RPClass is not define members as a OOP language but to save 
 * bandwidth usage by replacing these members text definitions with a short integer.
 * <p>
 * Also RPClass define a set of propierties over attributes, events and slots, like being 
 * private, hidden or volatile. 
 * 
 * @see Definition
 * 
 * @author miguel
 */
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
	
	/** 
	 * Constructor
	 * Only used in serialization. 
	 */
	public RPClass() {
		name=null;
		parent = null;
		attributes = new HashMap<String, Definition>();
		rpevents = new HashMap<String, Definition>();
		rpslots = new HashMap<String, Definition>();
	}

	/**
	 * Constructor.
	 * It adds the RPClass to a global list of rp classes.
	 * 
	 * @param name the class name
	 */
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

	/** 
	 * Returns true if the global list contains the name rpclass
	 * @param name the class name to query
	 * @return true if the global list contains the name rpclass
	 */
	public static boolean hasRPClass(String name) {
		return rpClassList.containsKey(name);
	}

	/**
	 * Returns the name rpclass from the global list 
	 * @param name the name of the class
	 * @return the class or null if it doesn't exist.
	 */
	public static RPClass getRPClass(String name) {
		return rpClassList.get(name);
	}

	/** 
	 * This method sets the parent of this rpclass 
	 * @param parent the super class of this class. 
	 */
	public void isA(RPClass parent) {
		this.parent = parent;
	}

	/** 
	 * This method sets the parent of this rpclass
 	 * @param parent the super class of this class. 
	 */
	public void isA(String parent) {
		this.parent = getRPClass(parent);
	}

	/**
	 * This method returns true if it is a subclass of parentClass or if it is
	 * the class itself.
	 * @param parentClass the super class of this class
	 * @return true if it is a subclass of parentClass or if it is the class itself.
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
	 * @return the name of the rpclass
	 */
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
	
	/**
	 * Adds a definition of an attribute, event or slot with the given type and flags.
	 * @param clazz type of definition ( attribute, event or slot ) 
	 * @param name name of the definition
	 * @param type type or capacity if it is an slot
	 * @param flags like visibility, storability, etc...
	 */
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

	/**
	 * Returns the definition object itself.
	 * 
	 * @param clazz type of definition ( attribute, event or slot ) 
	 * @param name name of the definition
	 * @return this definition object or null if it is not found
	 */
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

	/** 
	 * Returns the code of the attribute/event/slot whose name is name for this rpclass

	 * @param clazz type of definition ( attribute, event or slot ) 
	 * @param name name of the definition
	 * @return definition code
	 * @throws SyntaxException if the definition is not found. 
	 */
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

	/** 
	 * Returns the name of the attribute whose code is code for this rpclass 
	 * @param clazz type of definition ( attribute, event or slot ) 
	 * @param code definition code
	 * @return name of the definition
	 * @throws SyntaxException if the definition is not found.
	 */ 
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


	/** 
	 * Serialize the object into the output
	 * @param out the output serializer
	 * @throws IOException if there is any problem serializing.
	 */
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

	/** 
	 * Fill the object from data deserialized from the serializer
	 * @param in input serializer
	 * @throws IOException if there is any problem in the serialization. 
	 */
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

	/** 
	 * Iterates over the global list of rpclasses
	 * @return an iterator 
	 */
	public static Iterator<RPClass> iterator() {
		return rpClassList.values().iterator();
	}

	/** 
	 * Returns the size of the rpclass global list
	 * @return number of defined classes.  
	 */
	public static int size() {
		return rpClassList.size();
	}
}
