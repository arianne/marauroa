/* $Id: RPEventDesc.java,v 1.1 2007/02/06 18:19:33 arianne_rpg Exp $ */
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

import java.util.HashMap;
import java.util.Map;

/** 
 * This class describe an attribute:
 * - Its type
 * - its visibility
 * - If it is storable or volatile
 * @author miguel
 */
// TODO: Refactor. It is the same code than AttributeDesc
class RPEventDesc implements marauroa.common.net.Serializable {
	private static short lastCode = 0;

	private static Map<String, Short> eventsIntegerMap = new HashMap<String, Short>();

	private static short getValidCode(String name) {
		if (!eventsIntegerMap.containsKey(name)) {
			eventsIntegerMap.put(name, new Short(++lastCode));
		}

		return eventsIntegerMap.get(name);
	}

	public RPEventDesc() {
	}

	public RPEventDesc(String name, byte type, byte flags) {
		code = getValidCode(name);
		this.name = name;
		this.type = type;
		this.flags = flags;
	}

	/** int value representing attribute */
	public short code;

	/** event name */
	public String name;

	/** event type */
	public byte type;

	/** attribute visibility and storability */
	public byte flags;

	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws java.io.IOException {
		out.write(code);
		out.write(name);
		out.write(type);
		out.write(flags);
	}

	public void readObject(marauroa.common.net.InputSerializer in)
			throws java.io.IOException, java.lang.ClassNotFoundException {
		code = in.readShort();
		name = in.readString();
		type = in.readByte();
		flags = in.readByte();
	}
}
