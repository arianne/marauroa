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

import { RPObject } from "./RPObject";

let rpClasses = new Map();
rpClasses.set("_default", RPObject);

/**
 * marauroa.rpobjectFactory
 * creates RPObjects
 *
 * use the rpclass name as attribute name for a prototype object
 */
export class RPObjectFactory {

	register<T extends RPObject>(name: string, clazz: new() => T) {
		rpClasses.set(name, clazz);
	}

	create(rpclass: string): RPObject {
		let ctor = rpClasses.get(rpclass);
		if (!ctor) {
			console.log("Unknown RPClass " + rpclass);
			ctor = rpClasses.get("_default");
		}
		let temp = new ctor();
		temp.init();
		return temp;
	};

}
