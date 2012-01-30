/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputSerializer is used to serialize classes that implement the Serializable
 * interface into a OutputStream.
 *
 */
public class OutputSerializer {

	private OutputStream out;
	private int protocolVersion = NetConst.NETWORK_PROTOCOL_VERSION;

	/**
	 * Constructor that defines a specified OutputStream for the serializer
	 *
	 * @param out
	 *            the OutputStream to which objects are serialized
	 */
	public OutputSerializer(OutputStream out) {
		this.out = out;
	}

	/**
	 * Add the Object to the serializer, if it implements the
	 * marauroa.common.net.Serializable interface
	 *
	 * @param obj
	 *            the object to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(marauroa.common.net.Serializable obj) throws IOException {
		obj.writeObject(this);
	}

	/**
	 * Add the byte to the serializer
	 *
	 * @param a
	 *            the byte to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(byte a) throws IOException {
		out.write(a);
	}

	/**
	 * Add the byte array to the serializer
	 *
	 * @param a
	 *            the byte array to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(byte[] a) throws IOException {
		write(a.length);
		out.write(a);
	}

	/**
	 * Add a byte array whose size is smaller than 255 to the serializer
	 *
	 * @param a
	 *            the byte array to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write255LongArray(byte[] a) throws IOException {
		if (a.length > 255) {
			throw new IOException("Array too large for write255LongArray.");
		}

		write((byte) a.length);
		out.write(a);
	}

	/**
	 * Add a long byte array whose size is smaller than 2^31 to the serializer
	 *
	 * @param a
	 *            the byte array to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write65536LongArray(byte[] a) throws IOException {
		if (a.length > Short.MAX_VALUE) {
			throw new IOException();
		}

		write((short) a.length);
		out.write(a);
	}

	/**
	 * Add the short to the serializer
	 *
	 * @param a
	 *            the short to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(short a) throws IOException {
		byte[] tmp = new byte[] { (byte) (a & 0xff), (byte) (a >> 8 & 0xff)};
		out.write(tmp, 0, 2);
	}

	/**
	 * Add the int to the serializer
	 *
	 * @param a
	 *            the int to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(int a) throws IOException {
		byte[] tmp = new byte[] { (byte) (a & 0xff), (byte) (a >> 8 & 0xff),
				(byte) (a >> 16 & 0xff), (byte) (a >>> 24) };
		out.write(tmp);
	}

	/**
	 * Add the float to the serializer
	 *
	 * @param a
	 *            the int to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(float a) throws IOException {
		int intBits = Float.floatToIntBits(a);
		byte[] tmp = new byte[] { (byte) (intBits & 0xff), (byte) (intBits >> 8 & 0xff),
				(byte) (intBits >> 16 & 0xff), (byte) (intBits >>> 24) };
		out.write(tmp);
	}

	/**
	 * Add the String to the serializer, using UTF-8 encoding
	 *
	 * @param a
	 *            the String to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(String a) throws IOException {
		write(a.getBytes("UTF-8"));
	}

	/**
	 * Add a short string to the serializer, using UTF-8 encoding
	 *
	 * @param a
	 *            the String to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write255LongString(String a) throws IOException {
		write255LongArray(a.getBytes("UTF-8"));
	}

	/**
	/**
	 * Add a long string to the serializer, using UTF-8 encoding
	 *
	 * @param a
	 *            the String to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write65536LongString(String a) throws IOException {
		write65536LongArray(a.getBytes("UTF-8"));
	}

	/**
	 * Add the String array to the serializer, using UTF-8 encoding
	 *
	 * @param a
	 *            the String array to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(String[] a) throws IOException {
		write(a.length);
		for (int i = 0; i < a.length; i++) {
			write(a[i]);
		}
	}

	/**
	 * Add the Object array to the serializer, if it implements the
	 * marauroa.common.net.Serializable interface
	 *
	 * @param objs
	 *            the object array to serialize
	 * @throws IOException
	 *            in case of an IO-error
	 */
	public void write(marauroa.common.net.Serializable[] objs) throws IOException {
		write(objs.length);
		for (int i = 0; i < objs.length; i++) {
			write(objs[i]);
		}
	}

	/**
	 * gets the protocolVersion
	 *
	 * @return protocol version
	 */
	public int getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * sets the protocol version
	 *
	 * @param protocolVersion protocol version
	 */
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}
}
