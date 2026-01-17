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

import { marauroa } from "./Marauroa";
import { RPEvent } from "./RPEvent";
import { RPObject } from "./RPObject";

class DefaultRPEvent extends RPEvent {

	override execute(rpobject: RPObject) {
		if (marauroa.debug.unknownEvents) {
			console.log("Unhandled event: ", this, " on ", rpobject);
		}
	}
}

let rpEvents = new Map();
rpEvents.set("_default", DefaultRPEvent);

/**
 * creates RPEvent
 *
 * use the rpclass name as attribute name for a prototype object
 */
export class RPEventFactory {

	register(name: string, clazz: typeof RPEvent) {
		rpEvents.set(name, clazz);
	}

	create(rpclass: string) {
		let ctor = rpEvents.get(rpclass);
		if (!ctor) {
			ctor = rpEvents.get("_default");
		}
		return new ctor();
	}

}
