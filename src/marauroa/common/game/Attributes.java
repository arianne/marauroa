/* $Id: Attributes.java,v 1.85 2010/07/08 20:12:21 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.common.game;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.TimeoutConf;
import marauroa.common.game.Definition.DefinitionClass;

/**
 * This class hosts a list of Attributes stored as pairs String=String. There
 * are some important things to remark on Attributes.
 * 1) This class is more than a Map, as it stores information like its class.
 * 2) It has several special attributes that should be handle with care like:
 * - id It contains the unique per zone @Link {marauroa.common.game.IRPZone} identification of the object.
 * - zoneid It contains the zone identification. Both id and zoneid uniquely identify an object on server runtime.
 * - type It contains the name of the RPClass (@Link {marauroa.common.game.RPClass} that defines this attributes object.
 *
 * Attributes also features a part of the implementation of Delta^2 that try to
 * reduce data send to clients by just sending differences on the objects from a
 * previous state. This mainly consists on sending which attributes has been
 * added or modified and what attributes has been deleted.
 *
 * @author miguel
 */
public class Attributes implements marauroa.common.net.Serializable, Iterable<String>, Cloneable {
	private static Logger logger = Log4J.getLogger(Attributes.class);
	
	/** We are interested in clearing added and deleted only if they have changed. */
	private boolean modified;

	/** A Map<String,String> that contains the attributes */
	private Map<String, String> content;

	/** Every attributes has a class */
	private RPClass rpClass;

	/** This is for Delta algorithm: added attributes */
	private Map<String, String> added;

	/** This is for Delta algorithm: deleted attributes */
	private Map<String, String> deleted;

	/**
	 * This method fills this object with data from the attributes object passed
	 * as param
	 *
	 * @param attr
	 *			  the attribute object to use to fill this one.
	 * @return the object itself.
	 */
	public Object fill(Attributes attr) {
		setRPClass(attr.rpClass);
		modified = attr.modified;

		content.clear();
		content.putAll(attr.content);
		
		added.clear();
		added.putAll(attr.added);
		
		deleted.clear();
		deleted.putAll(attr.deleted);

		return this;
	}

	/**
	 * Constructor
	 *
	 * @param rpclass
	 *			  class that this attribute belongs too.
	 */
	public Attributes(RPClass rpclass) {
		rpClass = rpclass;

		content = Collections.synchronizedMap(new HashMap<String, String>());
		added = Collections.synchronizedMap(new HashMap<String, String>());
		deleted = Collections.synchronizedMap(new HashMap<String, String>());
		modified = false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Attributes clone = (Attributes) super.clone();
		clone.content = Collections.synchronizedMap(new HashMap<String, String>(content));
		clone.added = Collections.synchronizedMap(new HashMap<String, String>(added));
		clone.deleted = Collections.synchronizedMap(new HashMap<String, String>(deleted));
		return clone;
	}

	/**
	 * This method sets the RPClass of this attributes
	 *
	 * @param rpclass
	 *			  the rp class
	 */
	public void setRPClass(RPClass rpclass) {
		rpClass = rpclass;
	}

	/**
	 * This method sets the RPClass of this attributes
	 *
	 * @param rpclass
	 *			  the rp class
	 */
	public void setRPClass(String rpclass) {
		rpClass = RPClass.getRPClass(rpclass);
		
		if(rpClass==null) {
			throw new SyntaxException("Missing RPClass: "+rpclass);
		}
	}

	/**
	 * Returns the RPClass of the attributes
	 *
	 * @return the object RPClass
	 */
	public RPClass getRPClass() {
		return rpClass;
	}

	/**
	 * This method returns true if this attributes is an instance of RPClass or
	 * any of its subclasses
	 *
	 * @param baseclass
	 *			  the class we want to know if we are instance of.
	 * @return true if it is an instance of class
	 */
	public boolean instanceOf(RPClass baseclass) {
		return rpClass.subclassOf(baseclass.getName());
	}

	/**
	 * Returns true if the attributes contains nothing.
	 *
	 * @return true if is empty
	 */
	public boolean isEmpty() {
		return content.isEmpty();
	}

	/**
	 * Returns the number of attributes contained.
	 *
	 * @return amount of attributes
	 */
	public int size() {
		return content.size();
	}

	/**
	 * This method returns true if the attribute exists
	 *
	 * @param attribute
	 *			  the attribute name to check
	 * @return true if it exist or false otherwise
	 */
	public boolean has(String attribute) {
		if (!content.containsKey(attribute)) {
			if (rpClass == null || attribute==null)
				return false;

			Definition def = rpClass.getDefinition(DefinitionClass.STATIC, attribute);
			return (def != null && def.getValue() != null);
		}

		return true;
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute to be set.
	 * @param value
	 *			  the value we want to set.
	 */
	public void put(String attribute, String value) {
		validateValue(attribute, value);

		/* This is for Delta-delta feature */
		added.put(attribute, value);
		
		modified=true;
		
		if(value==null) {
			throw new IllegalArgumentException(attribute + " is null");
		}

		content.put(attribute, value);
	}

	/**
	 * validates the data type of the value
	 *
	 * @param attribute name of attribute
	 * @param value value to validate
	 */
	private void validateValue(String attribute, String value) {
		if (rpClass != null) {
			Definition def = rpClass.getDefinition(DefinitionClass.ATTRIBUTE, attribute);
			if (def != null) {
				def.validate(value);
			}
		}
	}

	/**
	 * Adds value to a previously existing attribute or just put it if it
	 * doesn't exist.
	 *
	 * @param attribute
	 *			  the attribute to be set.
	 * @param value
	 *			  the value we want to set.
	 */
	public void add(String attribute, int value) {
		if (!has(attribute)) {
			put(attribute, value);
		} else {
			put(attribute, getInt(attribute) + value);
		}
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute to be set.
	 * @param value
	 *			  the value we want to set.
	 */
	public void put(String attribute, int value) {
		put(attribute, Integer.toString(value));
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute to be set.
	 * @param value
	 *			  the value we want to set.
	 */
	public void put(String attribute, double value) {
		put(attribute, Double.toString(value));
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute to be set.
	 * @param value
	 *			  the value we want to set.
	 */
	public void put(String attribute, List<String> value) {
		put(attribute, Attributes.listToString(value));
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute we want to get
	 * @return the value of the attribute
	 */
	public String get(String attribute) {
		String value = content.get(attribute);

		if (value == null) {
			/*
			 * If instance doesn't have the attribute, check if RPClass has it
			 * as a static attribute.
			 */
			Definition def = rpClass.getDefinition(DefinitionClass.STATIC, attribute);
			if(def!=null) {
				/*
				 * It is possible that the attribute itself doesn't exist as static attribute, 
				 * so we should return null instead.
				 */
				return def.getValue();
			}
		}

		return value;
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute we want to get
	 * @return the value of the attribute
	 */
	public int getInt(String attribute) {
		String val = get(attribute);
		if (val == null) {
			throw new IllegalArgumentException("attribute '" + attribute + "' not found");
		}

		return Integer.parseInt(val);
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute we want to get
	 * @return the value of the attribute
	 */
	public boolean getBool(String attribute) {
		String val = get(attribute);
		if (val == null) {
			throw new IllegalArgumentException("attribute '" + attribute + "' not found");
		}

		return Boolean.parseBoolean(val);
	}
	
	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute we want to get
	 * @return the value of the attribute
	 */
	public double getDouble(String attribute) {
		String val = get(attribute);
		if (val == null) {
			throw new IllegalArgumentException("'" + attribute + "' not found");
		}

		return Double.parseDouble(val);
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute
	 *			  the attribute we want to get
	 * @return the value of the attribute
	 */
	public List<String> getList(String attribute) {
		String val = get(attribute);
		if (val == null) {
			throw new IllegalArgumentException("'" + attribute + "' not found");
		}

		return stringToList(val);
	}

	/**
	 * This methods remove the attribute from the container
	 *
	 * @param attribute
	 *			  the attribute we want to remove
	 * @return the value of the attribute
	 */
	public String remove(String attribute) {
		added.remove(attribute);
		/*
		 * This is for Delta^2 feature, as if it is empty it fails. It must
		 * be 0 because if attribute is a number it would fail on the
		 * serialization.
		 * 
		 * We can not ignore the change even if it had been added the same turn,
		 * because then if the attribute had a value before modifying it, the 
		 * client would get no notice about it being removed.
		 */
		deleted.put(attribute, "0");
		
		modified=true;

		return content.remove(attribute);
	}

	/**
	 * This method returns true of both object are equal.
	 *
	 * @param attr
	 *			  another Attributes object
	 * @return true if they are equal, or false otherwise.
	 */
	@Override
	public boolean equals(Object attr) {
		if (this == attr) {
			return true;
		}
		return (attr != null) && (attr instanceof Attributes)
				&& content.equals(((Attributes) attr).content);
	}

	@Override
	public int hashCode() {
		return content.hashCode();
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		return "Attributes of Class(" + rpClass.getName() + "): " + toAttributeString();
	}

	/**
	 * This method returns a String that represent the attributes
	 *
	 * @return a string representing the object.
	 */
	public String toAttributeString() {
		StringBuffer tmp = new StringBuffer();

		synchronized(content) {
			for (Map.Entry<String, String> entry : content.entrySet()) {
				tmp.append("[" + entry.getKey());
				tmp.append("=" + entry.getValue().replaceAll("\\\\", "\\\\\\\\").replaceAll("\\]", "\\\\]") + "]");
			}
		}

		return tmp.toString();
	}

	private static String listToString(List<String> list) {
		StringBuffer buffer = new StringBuffer("[");

		for (Iterator<?> it = list.iterator(); it.hasNext();) {
			String value = (String) it.next();

			buffer.append(value);
			if (it.hasNext()) {
				buffer.append("\t");
			}
		}

		buffer.append("]");
		return buffer.toString();
	}

	private static List<String> stringToList(String list) {
		String[] array = list.substring(1, list.length() - 1).split("\t");
		List<String> result = new LinkedList<String>();

		for (int i = 0; i < array.length; ++i) {
			result.add(array[i]);
		}

		return result;
	}

	/** 
	 * returns an iterator over the attribute names
	 *
	 * @return Iterator
	 */
	public Iterator<String> iterator() {
		Set<String> keySet = null;
		synchronized(content) {
			keySet = new HashSet<String>(content.keySet());
		}
		return keySet.iterator();
	}

	/**
	 * This method serialize the object with the default level of detail, that
	 * removes private and hidden attributes
	 *
	 * @param out
	 *			  the output serializer
	 */
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	/**
	 * This method serialize the object with the given level of detail.
	 *
	 * @param out
	 *			  the output serializer
	 * @param level
	 *			  the level of Detail
	 * @throws IOException
	 *			  in case of an IO error		
	 */
	public void writeObject(marauroa.common.net.OutputSerializer out, DetailLevel level)
			throws IOException {
		/*
		 * Obtains the number of attributes to serialize removing hidden and
		 * private attributes
		 */
		int size = 0;
		synchronized(content) {
			for (String key : content.keySet()) {
				try {
				if (shouldSerialize(DefinitionClass.ATTRIBUTE, key, level)) {
					size++;
				}
				} catch(NullPointerException e) {
					logger.warn("Not found key: "+key,e);
					logger.warn(this);
					throw e;
				}
			}
		}

		out.write(rpClass.getName());
		out.write(size);

		synchronized(content) {
			for (Map.Entry<String, String> entry : content.entrySet()) {
				String key = entry.getKey();
	
				Definition def = rpClass.getDefinition(DefinitionClass.ATTRIBUTE, key);
	
				if (shouldSerialize(def, level)) {
					boolean serializeKeyText = (level == DetailLevel.FULL) || (def.getCode() == -1);
	
					if (serializeKeyText) {
						out.write((short) -1);
						out.write(def.getName());
					} else {
						out.write(def.getCode());
					}
	
					def.serialize(entry.getValue(), out);
				}
			}
		}
	}

	/**
	 * Returns true if the element should be serialized.
	 *
	 * @param clazz
	 *			  Element definition type: ATTRIBUTE, RPSLOT or RPEVENT
	 * @param key
	 *			  the name of the element to test.
	 * @param level
	 *			  level of detail to serialize.
	 * @return true if it should be serialized.
	 */
	boolean shouldSerialize(DefinitionClass clazz, String key, DetailLevel level) {
		Definition def = rpClass.getDefinition(clazz, key);
		if (def == null) {
			logger.error("No definition " + clazz + " named " + key + " for class " + rpClass, new Throwable());
			return false;
		}
		return shouldSerialize(def, level);
	}

	/**
	 * Returns true if the attribute should be serialized.
	 *
	 * @param def
	 *			  Attribute definition
	 * @param level
	 *			  level of detail to serialize.
	 * @return true if it should be serialized.
	 */
	boolean shouldSerialize(Definition def, DetailLevel level) {
		if (level == DetailLevel.FULL && !def.isStorable()){
			return false;
		}
		
		return (level == DetailLevel.PRIVATE && !def.isHidden()) 
				|| (def.isVisible())
				|| (level == DetailLevel.FULL);
	}

	/**
	 * Fills this object with the data that has been serialized.
	 *
	 * @param in
	 *			  the input serializer
	 */
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException {
		modified = true;
		rpClass = RPClass.getRPClass(in.readString());
		int size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of " + String.valueOf(size) + " size");
		}

		content.clear();

		for (int i = 0; i < size; ++i) {
			short code = in.readShort();

			/* We obtain now the key name */
			String key;
			if (code == -1) {
				key = in.readString();
			} else {
				key = rpClass.getName(DefinitionClass.ATTRIBUTE, code);
			}

			Definition def = rpClass.getDefinition(DefinitionClass.ATTRIBUTE, key);

			if (def != null) {
				String value = def.deserialize(in);
				content.put(key, value);
			} else {
				throw new IOException("RPClass("+rpClass+") definition for attribute not found: " + key);
			}
		}
	}

	/**
	 * Removes all the visible attributes
	 * @param sync keep the structure intact, by not removing empty slots and links.
	 */
	public void clearVisible(boolean sync) {
		synchronized(content) {

			Iterator<Map.Entry<String, String>> it = content.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
	
				Definition def = rpClass.getDefinition(DefinitionClass.ATTRIBUTE, entry.getKey());
	
				// TODO handle Null Definition for attribute
				if(def==null) {
					logger.warn("Null Definition for attribute: "+entry.getKey()+" of RPClass: "+rpClass.getName());
					continue;
				}
				
				if (def.isVisible() && !entry.getKey().equals("id")) {
					it.remove();
	
					modified=true;
					deleted.remove(entry.getKey());
					added.remove(entry.getKey());
				}
			}
		}
	}

	/**
	 * Reset the Delta^2 information of the attribute.
	 */
	public void resetAddedAndDeletedAttributes() {
		/*
		 * We should clear added and deleted if they have been deleted. 
		 */
		if (modified) {
			added.clear();
			deleted.clear();
			modified = false;
		}
	}

	/**
	 * Fills this attribute with the added infomation of the Delta^2.
	 *
	 * @param attr
	 *			  the object whose added attributes we are going to copy.
	 */
	public void setAddedAttributes(Attributes attr) {
		rpClass = attr.rpClass;

		int i = 0;
		/* Copy each of the added attributes to this object. */
		synchronized(attr.added) {
			for (Map.Entry<String, String> entry : attr.added.entrySet()) {
				++i;
				content.put(entry.getKey(), entry.getValue());
			}
		}

		/* If we have added any attributes, we set the object id */
		if (i > 0) {
			content.put("id", attr.get("id"));
			/* Object stored at slots don't have now the zoneid attribute. */
			if (attr.has("zoneid")) {
				content.put("zoneid", attr.get("zoneid"));
			}
		}
	}

	/**
	 * Fills this attribute with the deleted infomation of the Delta^2.
	 *
	 * @param attr
	 *			  the object whose deleted attributes we are going to copy.
	 */
	public void setDeletedAttributes(Attributes attr) {
		rpClass = attr.rpClass;

		int i = 0;
		synchronized(attr.deleted) {
			/* Copy each of the deleted attributes to this object. */
			for (Map.Entry<String, String> entry : attr.deleted.entrySet()) {
				++i;
				content.put(entry.getKey(), entry.getValue());
			}
		}

		/* If we have added any attributes, we set the object id */
		if (i > 0) {
			content.put("id", attr.get("id"));
			/* Object stored at slots don't have now the zoneid attribute. */
			if (attr.has("zoneid")) {
				content.put("zoneid", attr.get("zoneid"));
			}
		}
	}
}
