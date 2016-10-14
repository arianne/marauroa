/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.Utility;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.IRPZone;
import marauroa.common.game.Perception;
import marauroa.common.game.RPObject;
import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.apache.log4j.NDC;

/**
 * This message indicate the client the objects that the server has determined
 * that this client is able to see.
 *
 * @see marauroa.common.net.message.Message
 * @see marauroa.common.game.IRPZone
 */
public class MessageS2CPerception extends Message {

	/** the logger instance. */
	static final marauroa.common.Logger logger = Log4J.getLogger(MessageS2CPerception.class);

	byte typePerception;

	private int timestampPerception;

	IRPZone.ID zoneid;

	private List<RPObject> addedRPObjects;

	private List<RPObject> modifiedAddedAttribsRPObjects;

	private List<RPObject> modifiedDeletedAttribsRPObjects;

	private List<RPObject> deletedRPObjects;

	private RPObject myRPObjectModifiedAdded;

	private RPObject myRPObjectModifiedDeleted;

	private static CachedCompressedPerception cache = CachedCompressedPerception.get();

	/** Constructor for allowing creation of an empty message */
	public MessageS2CPerception() {
		super(MessageType.S2C_PERCEPTION, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and
	 * perception to send.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param perception
	 *            the perception we are going to send.
	 */
	public MessageS2CPerception(Channel source, Perception perception) {
		super(MessageType.S2C_PERCEPTION, source);

		typePerception = perception.type;
		zoneid = perception.zoneid;
		addedRPObjects = perception.addedList;
		modifiedAddedAttribsRPObjects = perception.modifiedAddedList;
		modifiedDeletedAttribsRPObjects = perception.modifiedDeletedList;
		deletedRPObjects = perception.deletedList;
	}

	/**
	 * sets the added and deleted information for the destination player
	 *
	 * @param added   added information
	 * @param deleted deleted information
	 */
	public void setMyRPObject(RPObject added, RPObject deleted) {
		myRPObjectModifiedAdded = added;
		myRPObjectModifiedDeleted = deleted;
	}

	/**
	 * gets the added information for the personal object
	 *
	 * @return added information to the personal object
	 */
	public RPObject getMyRPObjectAdded() {
		return myRPObjectModifiedAdded;
	}

	/**
	 * gets the deleted information for the personal object
	 *
	 * @return deleted information from the personal object
	 */
	public RPObject getMyRPObjectDeleted() {
		return myRPObjectModifiedDeleted;
	}

	/**
	 * sets the timestamp of this perception
	 *
	 * @param ts counter
	 */
	public void setPerceptionTimestamp(int ts) {
		timestampPerception = ts;
	}

	/**
	 * gets the timestamp of this perception
	 *
	 * @return counter
	 */
	public int getPerceptionTimestamp() {
		return timestampPerception;
	}

	/**
	 * gets the type of this perception
	 *
	 * @return full or delta
	 */
	public byte getPerceptionType() {
		return typePerception;
	}

	/**
	 * gets the id of the zone, this perception is for
	 *
	 * @return IRPZone.ID
	 */
	public IRPZone.ID getRPZoneID() {
		return zoneid;
	}

	/**
	 * This method returns the list of modified objects
	 *
	 * @return List<RPObject> of added objects
	 */
	public List<RPObject> getAddedRPObjects() {
		return addedRPObjects;
	}

	/**
	 * This method returns the list of modified objects
	 *
	 * @return List<RPObject> of modified objects that has attributes added
	 */
	public List<RPObject> getModifiedAddedRPObjects() {
		return modifiedAddedAttribsRPObjects;
	}

	/**
	 * This method returns the list of modified objects
	 *
	 * @return List<RPObject> of modified objects that has attributes removed
	 */
	public List<RPObject> getModifiedDeletedRPObjects() {
		return modifiedDeletedAttribsRPObjects;
	}

	/**
	 * This method returns the list of deleted objects
	 *
	 * @return List<RPObject> of deleted objects
	 */
	public List<RPObject> getDeletedRPObjects() {
		return deletedRPObjects;
	}

	/**
	 * This method returns a String that represent the object
	 *
	 * @return a string representing the object.
	 */
	@Override
	public String toString() {
		StringBuilder perception_string = new StringBuilder();
		perception_string.append("Type: " + typePerception + " Timestamp: " + timestampPerception
		        + ") contents: ");

		perception_string.append("\n  zoneid: " + zoneid + "\n");
		perception_string.append("\n  added: \n");
		for (RPObject object : addedRPObjects) {
			perception_string.append("    " + object + "\n");
		}

		perception_string.append("\n  modified added: \n");
		for (RPObject object : modifiedAddedAttribsRPObjects) {
			perception_string.append("    " + object + "\n");
		}

		perception_string.append("\n  modified deleted: \n");
		for (RPObject object : modifiedDeletedAttribsRPObjects) {
			perception_string.append("    " + object + "\n");
		}

		perception_string.append("\n  deleted: \n");
		for (RPObject object : deletedRPObjects) {
			perception_string.append("    " + object + "\n");
		}

		perception_string.append("\n  my object modified added: \n");
		perception_string.append("    " + myRPObjectModifiedAdded + "\n");
		perception_string.append("\n  my object modified deleted: \n");
		perception_string.append("    " + myRPObjectModifiedDeleted + "\n");

		return perception_string.toString();
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("writing Object: [" + this + "]");
		}

		super.writeObject(out);
		out.write(getPrecomputedStaticPartPerception());
		out.write(getDynamicPartPerception(out.getProtocolVersion()));
	}

	private void setZoneid(RPObject object, String zoneid) {
		object.put("zoneid", zoneid);

//		Iterator<RPSlot> it = object.slotsIterator();
//		while (it.hasNext()) {
//			RPSlot slot = it.next();
//			for (RPObject son : slot) {
//				setZoneid(son, zoneid);
//			}
//		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		byte[] byteArray = in.readByteArray();
		ByteArrayInputStream array = new ByteArrayInputStream(byteArray);
		java.util.zip.InflaterInputStream szlib = new java.util.zip.InflaterInputStream(array,
		        new java.util.zip.Inflater());
		InputSerializer ser = new InputSerializer(szlib);
		ser.setProtocolVersion(protocolVersion);

		try {
			typePerception = ser.readByte();
			zoneid = (IRPZone.ID) ser.readObject(new IRPZone.ID(""));
			addedRPObjects = new LinkedList<RPObject>();
			deletedRPObjects = new LinkedList<RPObject>();
			modifiedAddedAttribsRPObjects = new LinkedList<RPObject>();
			modifiedDeletedAttribsRPObjects = new LinkedList<RPObject>();

			int added = ser.readInt();

			if (added > TimeoutConf.MAX_ARRAY_ELEMENTS) {
				throw new IOException("Illegal request of an list of " + String.valueOf(added)
				        + " size");
			}
			logger.debug(added + " added objects.");
			for (int i = 0; i < added; ++i) {
				RPObject object = (RPObject) ser.readObject(new RPObject());
				setZoneid(object, zoneid.getID());
				addedRPObjects.add(object);
			}

			int modAdded = ser.readInt();

			if (modAdded > TimeoutConf.MAX_ARRAY_ELEMENTS) {
				throw new IOException("Illegal request of an list of " + String.valueOf(modAdded)
				        + " size");
			}
			logger.debug(modAdded + " modified Added objects..");
			for (int i = 0; i < modAdded; ++i) {
				RPObject object = (RPObject) ser.readObject(new RPObject());
				setZoneid(object, zoneid.getID());
				modifiedAddedAttribsRPObjects.add(object);
			}

			int modDeleted = ser.readInt();

			if (modDeleted > TimeoutConf.MAX_ARRAY_ELEMENTS) {
				throw new IOException("Illegal request of an list of " + String.valueOf(modDeleted)
				        + " size");
			}
			logger.debug(modDeleted + " modified Deleted objects..");
			for (int i = 0; i < modDeleted; ++i) {
				RPObject object = (RPObject) ser.readObject(new RPObject());
				setZoneid(object, zoneid.getID());
				modifiedDeletedAttribsRPObjects.add(object);
			}

			int del = ser.readInt();

			if (del > TimeoutConf.MAX_ARRAY_ELEMENTS) {
				throw new IOException("Illegal request of an list of " + String.valueOf(del)
				        + " size");
			}
			logger.debug(del + " deleted objects..");
			for (int i = 0; i < del; ++i) {
				RPObject object = (RPObject) ser.readObject(new RPObject());
				setZoneid(object, zoneid.getID());
				deletedRPObjects.add(object);
			}
		} catch (IOException ioe) {
			InputStream stream = new java.util.zip.InflaterInputStream(new ByteArrayInputStream(
			        byteArray), new java.util.zip.Inflater());
			NDC.push("message is [" + this + "]\n");
			NDC.push("message dump is [\n" + Utility.dumpInputStream(stream) + "\n]\n");
			logger.error("error in getMessage", ioe);
			NDC.pop();
			NDC.pop();
			return;
		}

		/** Dynamic part */
		array = new ByteArrayInputStream(in.readByteArray());
		ser = new InputSerializer(array);
		ser.setProtocolVersion(protocolVersion);

		timestampPerception = ser.readInt();

		logger.debug("read My RPObject");
		byte modifiedAddedMyRPObjectPresent = ser.readByte();
		if (modifiedAddedMyRPObjectPresent == 1) {
			myRPObjectModifiedAdded = (RPObject) ser.readObject(new RPObject());
			setZoneid(myRPObjectModifiedAdded, zoneid.getID());
		} else {
			myRPObjectModifiedAdded = null;
		}

		byte modifiedDeletedMyRPObjectPresent = ser.readByte();
		if (modifiedDeletedMyRPObjectPresent == 1) {
			myRPObjectModifiedDeleted = (RPObject) ser.readObject(new RPObject());
			setZoneid(myRPObjectModifiedDeleted, zoneid.getID());
		} else {
			myRPObjectModifiedDeleted = null;
		}
	}

	static class CachedCompressedPerception {

		static class CacheKey {

			private final byte type;

			private final IRPZone.ID zoneid;

			private final int protocolVersion;

			public CacheKey(byte type, IRPZone.ID zoneid, int protocolVersion) {
				this.type = type;
				this.zoneid = zoneid;
				this.protocolVersion = protocolVersion;
			}

			@Override
			public boolean equals(Object obj) {
				if (obj instanceof CacheKey) {
					CacheKey a = (CacheKey) obj;
					if (a.type == type && a.zoneid.equals(zoneid) && a.protocolVersion == protocolVersion) {
						return true;
					}
				}

				return false;
			}

			@Override
			public int hashCode() {
				return (type + 1) * zoneid.hashCode() * protocolVersion;
			}
		}

		private final Map<CacheKey, byte[]> cachedContent;

		private CachedCompressedPerception() {
			cachedContent = new HashMap<CacheKey, byte[]>();
		}

		static CachedCompressedPerception instance;

		synchronized static public CachedCompressedPerception get() {
			if (instance == null) {
				instance = new CachedCompressedPerception();
			}

			return instance;
		}

		synchronized public void clear() {
			cachedContent.clear();
		}

		synchronized public byte[] get(MessageS2CPerception perception) throws IOException {
			CacheKey key = new CacheKey(perception.typePerception, perception.zoneid, perception.protocolVersion);

			if (!cachedContent.containsKey(key)) {
				logger.debug("Perception not found in cache");
				ByteArrayOutputStream array = new ByteArrayOutputStream();
				DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
				OutputSerializer serializer = new OutputSerializer(out_stream);
				serializer.setProtocolVersion(perception.getProtocolVersion());

				perception.computeStaticPartPerception(serializer);
				out_stream.close();
				byte[] content = array.toByteArray();

				cachedContent.put(key, content);
			} else {
				logger.debug("Perception FOUND in cache");
			}

			return cachedContent.get(key);
		}
	}

	/**
	 * clears the cached perceptions to start the next turn fresh.
	 */
	public static void clearPrecomputedPerception() {
		cache.clear();
	}

	private byte[] getPrecomputedStaticPartPerception() throws IOException {
		return cache.get(this);
	}

	private byte[] getDynamicPartPerception(int protocolVersion) throws IOException {
		ByteArrayOutputStream array = new ByteArrayOutputStream();
		OutputSerializer serializer = new OutputSerializer(array);
		serializer.setProtocolVersion(protocolVersion);

		serializer.write(timestampPerception);
		if (myRPObjectModifiedAdded == null) {
			serializer.write((byte) 0);
		} else {
			serializer.write((byte) 1);
			myRPObjectModifiedAdded.writeObject(serializer, DetailLevel.PRIVATE);
		}

		if (myRPObjectModifiedDeleted == null) {
			serializer.write((byte) 0);
		} else {
			serializer.write((byte) 1);
			myRPObjectModifiedDeleted.writeObject(serializer, DetailLevel.PRIVATE);
		}

		return array.toByteArray();
	}

	void computeStaticPartPerception(OutputSerializer ser) throws IOException {
		ser.write(typePerception);
		ser.write(zoneid);

		ser.write(addedRPObjects.size());
		for (RPObject object : addedRPObjects) {
			ser.write(object);
		}

		ser.write(modifiedAddedAttribsRPObjects.size());
		for (RPObject object : modifiedAddedAttribsRPObjects) {
			ser.write(object);
		}

		ser.write(modifiedDeletedAttribsRPObjects.size());
		for (RPObject object : modifiedDeletedAttribsRPObjects) {
			ser.write(object);
		}

		ser.write(deletedRPObjects.size());
		for (RPObject object : deletedRPObjects) {
			ser.write(object);
		}
	}

	@Override
	public void writeToJson(StringBuilder out) {
		super.writeToJson(out);
		out.append(",");
		OutputSerializer.writeJson(out, "zoneid", zoneid.getID());
		out.append(",");
		OutputSerializer.writeJson(out, "sync");
		out.append(":");
		if (typePerception == Perception.SYNC) {
			out.append("true");
		} else {
			out.append("false");
		}

		// public
		if ((addedRPObjects != null) && !addedRPObjects.isEmpty()) {
			OutputSerializer.writeObjectCollectionToJson(out, "aO", addedRPObjects, DetailLevel.NORMAL);
		}
		if ((modifiedAddedAttribsRPObjects != null) && !modifiedAddedAttribsRPObjects.isEmpty()) {
			OutputSerializer.writeObjectCollectionToJson(out, "aA", modifiedAddedAttribsRPObjects, DetailLevel.NORMAL);
		}
		if ((modifiedDeletedAttribsRPObjects != null) && !modifiedDeletedAttribsRPObjects.isEmpty()) {
			OutputSerializer.writeObjectCollectionToJson(out, "dA", modifiedDeletedAttribsRPObjects, DetailLevel.NORMAL);
		}
		if ((deletedRPObjects != null) && !deletedRPObjects.isEmpty()) {
			OutputSerializer.writeObjectCollectionToJson(out, "dO", deletedRPObjects, DetailLevel.NORMAL);
		}

		// private
		if ((myRPObjectModifiedAdded != null)) {
			out.append(",\"aM\":{");
			myRPObjectModifiedAdded.writeToJson(out, DetailLevel.PRIVATE);
			out.append("}");
		}
		if ((myRPObjectModifiedDeleted != null)) {
			out.append(",\"dM\":{");
			myRPObjectModifiedDeleted.writeToJson(out, DetailLevel.PRIVATE);
			out.append("}");
		}
	}

	@Override
	public boolean isSkippable() {
		if ((addedRPObjects != null) && !addedRPObjects.isEmpty()) {
			return false;
		}
		if ((modifiedAddedAttribsRPObjects != null) && !modifiedAddedAttribsRPObjects.isEmpty()) {
			return false;
		}
		if ((modifiedDeletedAttribsRPObjects != null) && !modifiedDeletedAttribsRPObjects.isEmpty()) {
			return false;
		}
		if ((deletedRPObjects != null) && !deletedRPObjects.isEmpty()) {
			return false;
		}
		if ((myRPObjectModifiedAdded != null)) {
			return false;
		}
		if ((myRPObjectModifiedDeleted != null)) {
			return false;
		}
		return true;
	}

	/**
	 * is this message a perception
	 *
	 * @return true, if this message is a perception; false otherwise
	 */
	@Override
	public boolean isPerception() {
		return true;
	}

}
