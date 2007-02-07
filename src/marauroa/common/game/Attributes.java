/* $Id: Attributes.java,v 1.26 2007/02/07 22:36:30 arianne_rpg Exp $ */
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;

import org.apache.log4j.Logger;

/** 
 * This class hosts a list of Attributes stored as pairs String=String.
 * There are some important things to remark on Attributes.
 * 1) This class is more than a Map, as it stores information like its class.
 * 2) It has several special attributes that should be handle with care like:
 *   - id
 *   - zoneid
 *   - type
 *   
 * Attributes also features a part of the implementation of Delta^2 that try
 * to reduce data send to clients by just sending differences on the objects from a previous
 * state.
 * This mainly consists on sending which attributes has been added or modified and what 
 * attributes has been deleted.
 */
public class Attributes implements marauroa.common.net.Serializable, Iterable<String> {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Attributes.class);

	/** This is for Delta² algorithm: added attributes */
	private Map<String, String> added;

	/** This is for Delta² algorithm: deleted attributes */
	private Map<String, String> deleted;

	/** A Map<String,String> that contains the attributes */
	private Map<String, String> content;

	/** Every attributes has a class */
	private RPClass rpClass;

	/**
	 * This method fills this object with data from the attributes object passed as param
	 * @param attr the attribute object to use to fill this one.
	 * @return the object itself.
	 */
	public Object fill(Attributes attr) {
		setRPClass(attr.rpClass);

		content.clear();
		for (Map.Entry<String, String> entry : attr.content.entrySet()) {
			content.put(entry.getKey(), entry.getValue());
		}

		added.clear();
		for (Map.Entry<String, String> entry : attr.added.entrySet()) {
			added.put(entry.getKey(), entry.getValue());
		}

		deleted.clear();
		for (Map.Entry<String, String> entry : attr.deleted.entrySet()) {
			deleted.put(entry.getKey(), entry.getValue());
		}

		return this;
	}

	/** Constructor */
	public Attributes(RPClass rpclass) {
		rpClass = rpclass;

		content = new HashMap<String, String>();
		added = new HashMap<String, String>();
		deleted = new HashMap<String, String>();
	}

	/**
	 * This method sets the RPClass of this attributes
	 * @param rpclass the rp class
	 */
	public void setRPClass(RPClass rpclass) {
		rpClass = rpclass;
	}

	/** 
	 * Returns the RPClass of the attributes 
	 * @return the object RPClass 
	 */
	public RPClass getRPClass() {
		return rpClass;
	}

	/**
	 * This method returns true if this attributes is an instance of RPClass or any of its subclasses
	 * @param baseclass the class we want to know if we are instance of.
	 * @return true if it is an instance of class
	 */
	public boolean instanceOf(RPClass baseclass) {
		return rpClass.subclassOf(baseclass.getName());
	}

	/**
	 * Returns true if the attributes contains nothing.
	 * @return true if is empty
	 */
	public boolean isEmpty() {
		return content.isEmpty();
	}

	/**
	 * Returns the number of attributes contained.
	 * @return amount of attributes
	 */
	public int size() {
		return content.size();
	}

	/**
	 * This method returns true if the attribute exists
	 *
	 * @param attribute
	 *            the attribute name to check
	 * @return true if it exist or false otherwise
	 */
	public boolean has(String attribute) {
		return content.containsKey(attribute);
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *            the attribute to be set.
	 * @param value
	 *            the value we want to set.
	 */
	public void put(String attribute, String value) {
		/* This is for Delta-delta feature */
		added.put(attribute, value);

		/* If attribute to set is type we load class value from it */
		if (attribute.equals("type") && RPClass.hasRPClass(value)) {
			try {
				setRPClass(RPClass.getRPClass(value));
			} catch (RPClass.SyntaxException e) {
				/* NOTE: Can't ever happen */
				logger.error("cannot put attribute [" + attribute+ "] value: [" + value + "], Syntax error", e);
			}
		}

		content.put(attribute, value);
	}

	/**
	 * Adds value to a previously existing attribute or just put it if it doesn't exist.
	 * @param attribute the attribute to be set.
	 * @param value the value we want to set.
	 */
	public void add(String attribute, int value) {
		if (has(attribute)) {
			put(attribute, value);
		} else {
			put(attribute, getInt(attribute) + value);
		}
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *            the attribute to be set.
	 * @param value
	 *            the value we want to set.
	 */
	public void put(String attribute, int value) {
		put(attribute, Integer.toString(value));
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *            the attribute to be set.
	 * @param value
	 *            the value we want to set.
	 */
	public void put(String attribute, double value) {
		put(attribute, Double.toString(value));
	}

	/**
	 * This method set the value of an attribute
	 *
	 * @param attribute
	 *            the attribute to be set.
	 * @param value
	 *            the value we want to set.
	 */
	public void put(String attribute, List<String> value) {
		put(attribute, Attributes.ListToString(value));
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute the attribute we want to get
	 * @return the value of the attribute
	 */
	public String get(String attribute) {
		return content.get(attribute);
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute the attribute we want to get
	 * @return the value of the attribute
	 * @exception AttributesNotFoundException if the attributes doesn't exist.
	 */
	public int getInt(String attribute) {
		return Integer.parseInt(get(attribute));
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute the attribute we want to get
	 * @return the value of the attribute
	 * @exception AttributesNotFoundException if the attributes doesn't exist.
	 */
	public double getDouble(String attribute) {
		return Double.parseDouble(get(attribute));
	}

	/**
	 * This methods return the value of an attribute
	 *
	 * @param attribute the attribute we want to get
	 * @return the value of the attribute
	 * @exception AttributesNotFoundException if the attributes doesn't exist.
	 */
	public List<String> getList(String attribute) {
		return StringToList(get(attribute));
	}

	/**
	 * This methods remove the attribute from the container
	 *
	 * @param attribute the attribute we want to remove
	 * @return the value of the attribute
	 * @exception AttributesNotFoundException if the attributes doesn't exist.
	 */
	public String remove(String attribute) {
		String has=added.remove(attribute);
		if(has==null) {
			/* This is for Delta^2 feature, as if it is empty it fails.
			 * It must be 0 because if attribute is a number it would fail on the serialization */
			deleted.put(attribute, "0");
		}

		return content.remove(attribute);
	}

	/**
	 * This method returns true of both object are equal.
	 *
	 * @param attr another Attributes object
	 * @return true if they are equal, or false otherwise.
	 */
	@Override
	public boolean equals(Object attr) {
		return (attr !=null) && (attr instanceof Attributes) && content.equals(((Attributes) attr).content);
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
		StringBuffer tmp = new StringBuffer("Attributes of Class("+ rpClass.getName() + "): ");

		for (Map.Entry<String, String> entry : content.entrySet()) {
			tmp.append("[" + entry.getKey());
			tmp.append("=" + entry.getValue() + "]");
		}

		return tmp.toString();
	}

	private static String ListToString(List<String> list) {
		StringBuffer buffer = new StringBuffer("[");

		for (Iterator it = list.iterator(); it.hasNext();) {
			String value = (String) it.next();

			buffer.append(value);
			if (it.hasNext()) {
				buffer.append("\t");
			}
		}

		buffer.append("]");
		return buffer.toString();
	}

	private static List<String> StringToList(String list) {
		String[] array = list.substring(1, list.length() - 1).split("\t");
		List<String> result = new LinkedList<String>();

		for (int i = 0; i < array.length; ++i) {
			result.add(array[i]);
		}

		return result;
	}

	/** returns an iterator over the attribute names */
	public Iterator<String> iterator() {
		return content.keySet().iterator();
	}

	/**
	 * This method serialize the object with the default level of detail, that removes
	 *  private and hidden attributes
	 *  @param out the output serializer
	 */
	public void writeObject(marauroa.common.net.OutputSerializer out) throws java.io.IOException {
		writeObject(out, DetailLevel.NORMAL);
	}

	/**
	 * This method serialize the object with the default level of detail, that removes
	 *  private and hidden attributes
	 *  @param out the output serializer
	 *  @param level the level of Detail
	 */
	public void writeObject(marauroa.common.net.OutputSerializer out, DetailLevel level) throws java.io.IOException {
		int size = content.size();

		/* We need to remove hidden and private attributes to players */
		for (String key : content.keySet()) {
			if (level == DetailLevel.NORMAL	&& (rpClass.isVisible(key) == false)) {
				// If this attribute is Hidden or private and full data is false
				--size;
			} else if (level != DetailLevel.FULL && rpClass.isHidden(key)) {
				// If this attribute is Hidden and full data is true.
				// This way we hide some attribute to player.
				--size;
			}
		}

		out.write(rpClass.getName());
		out.write(size);
		for (Map.Entry<String, String> entry : content.entrySet()) {
			String key = entry.getKey();

			if ((level == DetailLevel.PRIVATE && !rpClass.isHidden(key))
					|| (rpClass.isVisible(key)) || (level == DetailLevel.FULL)) {
				short code = -1;

				try {
					code = rpClass.getCode(key);
				} catch (RPClass.SyntaxException e) {
					logger.error("cannot writeObject, Attribute [" + key+ "] not found", e);
					code = -1;
				}

				if (level == DetailLevel.FULL) {
					// We want to ensure that attribute text is stored.
					// This is helpful for database storage.
					code = -1;
				}

				out.write(code);

				if (code == -1) {
					out.write(key);
				}

				try {
					switch (rpClass.getType(key)) {
					case RPClass.VERY_LONG_STRING:
						out.write(entry.getValue());
						break;
					case RPClass.LONG_STRING:
						out.write65536LongString(entry.getValue());
						break;
					case RPClass.STRING:
						out.write255LongString(entry.getValue());
						break;
					case RPClass.FLOAT:
						out.write(Float.parseFloat(entry.getValue()));
						break;
					case RPClass.INT:
						out.write(Integer.parseInt(entry.getValue()));
						break;
					case RPClass.SHORT:
						out.write(Short.parseShort(entry.getValue()));
						break;
					case RPClass.BYTE:
						out.write(Byte.parseByte(entry.getValue()));
						break;
					case RPClass.FLAG:
						/*
						 * It is empty because it is a flag and so, it is
						 * already present.
						 */
						break;
					default:
						/* NOTE: Must never happen */
						logger.fatal("got unknown attribute type "+ rpClass.getType(key));
						break;
					}
				} catch (Exception e) {
					String className = (rpClass != null ? rpClass.getName(): null);
					logger.error("Attribute " + key + " [" + className+ "] caused an exception", e);
					throw new java.io.IOException(e.getMessage());
				}
			}
		}
	}

	/**
	 * Fills this object with the data that has been serialized.
	 */
	public void readObject(marauroa.common.net.InputSerializer in) throws java.io.IOException, java.lang.ClassNotFoundException {
		rpClass = RPClass.getRPClass(in.readString());
		int size = in.readInt();

		if (size > TimeoutConf.MAX_ARRAY_ELEMENTS) {
			throw new IOException("Illegal request of an list of "
					+ String.valueOf(size) + " size");
		}

		content.clear();

		for (int i = 0; i < size; ++i) {
			short code = in.readShort();

			/* We obtaing now the key name */
			String key;
			if (code == -1) {
				key = in.readString();
			} else {
				key = rpClass.getName(code);
			}

			if (rpClass.getType(key) == RPClass.VERY_LONG_STRING) {
				content.put(key, in.readString());
			} else if (rpClass.getType(key) == RPClass.LONG_STRING) {
				content.put(key, in.read65536LongString());
			} else if (rpClass.getType(key) == RPClass.STRING) {
				content.put(key, in.read255LongString());
			} else if (rpClass.getType(key) == RPClass.FLOAT) {
				content.put(key, Float.toString(in.readFloat()));
			} else if (rpClass.getType(key) == RPClass.INT) {
				content.put(key, Integer.toString(in.readInt()));
			} else if (rpClass.getType(key) == RPClass.SHORT) {
				content.put(key, Integer.toString(in.readShort()));
			} else if (rpClass.getType(key) == RPClass.BYTE) {
				content.put(key, Integer.toString(in.readByte()));
			} else if (rpClass.getType(key) == RPClass.FLAG) {
				content.put(key, "");
			}
		}
	}

	/**
	 * Removes all the visible attributes
	 * @return amount of attributes removed.
	 */
	public int clearVisible() {
		int i = 0;

		Iterator<Map.Entry<String, String>> it = content.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> entry = it.next();

			if (rpClass.isVisible(entry.getKey())
					&& !entry.getKey().equals("id")
					&& !entry.getKey().equals("zoneid")) {
				i++;
				it.remove();

				deleted.remove(entry.getKey());
				added.remove(entry.getKey());
			}
		}

		return i;
	}

	/**
	 * Reset the DeltaÂ² information of the attribute.
	 */
	public void resetAddedAndDeletedAttributes() {
		added.clear();
		deleted.clear();
	}

	/**
	 * Fills this attribute with the added infomation of the DeltaÂ².
	 */
	public void setAddedAttributes(Attributes attr) throws RPClass.SyntaxException {
		rpClass = attr.rpClass;
		int i = 0;
		for (Map.Entry<String, String> entry : attr.added.entrySet()) {
			++i;
			content.put(entry.getKey(), entry.getValue());
		}

		if (i > 0) {
			content.put("id", attr.get("id"));
			content.put("zoneid", attr.get("zoneid"));
		}
	}

	/**
	 * Fills this attribute with the deleted infomation of the DeltaÂ².
	 */	public void setDeletedAttributes(Attributes attr) throws RPClass.SyntaxException {
		rpClass = attr.rpClass;

		int i = 0;
		for (Map.Entry<String, String> entry : attr.deleted.entrySet()) {
			++i;
			content.put(entry.getKey(), entry.getValue());
		}

		if (i > 0) {
			content.put("id", attr.get("id"));
			content.put("zoneid", attr.get("zoneid"));
		}
	}
}
