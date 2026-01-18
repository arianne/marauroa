/***************************************************************************
 *                   (C) Copyright 2011-2026 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

import { RPSlot } from "./RPSlot";

let rpSlots = new Map();
rpSlots.set("_default", RPSlot);

/**
 * creates RPSlot
 *
 * use the rpclass name as attribute name for a prototype object
 */
export class RPSlotFactory {

	register<T extends RPSlot>(name: string, clazz: new() => T) {
		rpSlots.set(name, clazz);
	}

	create(name: string) {
		let ctor = rpSlots.get(name);
		if (!ctor) {
			ctor = rpSlots.get("_default");
		}
		let slot = new ctor();
		slot._name = name;
		slot._objects = [];
		return slot;
	};

};
