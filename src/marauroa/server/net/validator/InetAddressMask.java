/* $Id: InetAddressMask.java,v 1.7 2010/06/12 15:08:42 nhnb Exp $ */
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

package marauroa.server.net.validator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import marauroa.common.Utility;

/**
 * This class is a mask that determines if a IPv4 address match with the mask.
 * It works only for IPv4 addresses
 */
public class InetAddressMask {

	private byte address[];

	private byte mask[];

	/**
	 * creates a new InetAddressMask object.
	 *
	 * @param address address in form aaa.bbb.ccc.ddd
	 * @param mask network mask in form aaa.bbb.ccc.ddd
	 */
	public InetAddressMask(String address, String mask) {
		this(string2bytes(address), string2bytes(mask));
	}

	/**
	 * creates a new InetAddressMask object.
	 *
	 * @param address address
	 * @param mask network mask
	 */
	public InetAddressMask(byte address[], byte mask[]) {
		this.address = Utility.copy(address);
		this.mask = Utility.copy(mask);
		address[0] = (byte) (address[0] & mask[0]);
		address[1] = (byte) (address[1] & mask[1]);
		address[2] = (byte) (address[2] & mask[2]);
		address[3] = (byte) (address[3] & mask[3]);
	}

	/**
	 * checks whether the provided InetAddress object is within the range
	 * specified in this object.
	 *
	 * @param anotherAddress address to check
	 * @return true if it is within this range, false otherwise
	 */
	public boolean matches(InetAddress anotherAddress) {
		byte ob[] = anotherAddress.getAddress();
		ob[0] = (byte) (ob[0] & mask[0]);
		ob[1] = (byte) (ob[1] & mask[1]);
		ob[2] = (byte) (ob[2] & mask[2]);
		ob[3] = (byte) (ob[3] & mask[3]);

		boolean ret = true;
		ret = ret && (ob[3] ^ address[3]) == 0;
		ret = ret && (ob[2] ^ address[2]) == 0;
		ret = ret && (ob[1] ^ address[1]) == 0;
		ret = ret && (ob[0] ^ address[0]) == 0;
		// this kind of evaluation allows better debugging.
		// this can be rewritten later to
		// ret = (ob[3]^address[3]) == 0 && (ob[2]^address[2]) == 0 &&
		// (ob[1]^address[1]) == 0 && (ob[0]^address[0]) == 0;
		// which must be performanter - but no one knows one what part exactly
		// was false and what part was true...

		return (ret);
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + Arrays.hashCode(address);
		result = PRIME * result + Arrays.hashCode(mask);
		return result;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof InetAddressMask) {
			InetAddressMask other = (InetAddressMask) object;
			return Arrays.equals(address, other.address) && Arrays.equals(mask, other.mask);
		}
		return false;
	}

	@Override
	public String toString() {
		try {
			return (InetAddress.getByAddress(address).getHostAddress() + "/" + InetAddress
			        .getByAddress(mask).getHostAddress());
		} catch (UnknownHostException e) {
			return ("");
		}
	}

	/**
	 * converts string in form aaa.bbb.ccc.ddd into byte
	 * array[]{aaa,bbb,ccc,ddd}
	 *
	 * @param ipv4Address address string
	 * @return byte array
	 */
	public static byte[] string2bytes(String ipv4Address) {
		String[] str_bytes = ipv4Address.split("\\.");
		byte addr[] = new byte[4];
		addr[0] = addr[1] = addr[2] = addr[3] = 0;

		for (int i = 0; i < str_bytes.length && i < 4; i++) {
			addr[i] = (byte) Integer.parseInt(str_bytes[i]);
		}

		return (addr);
	}
}
