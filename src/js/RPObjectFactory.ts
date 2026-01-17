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
import { RPObject } from "./RPObject";
import { MarauroaUtils } from "./MarauroaUtils";

(function() {

let rpClasses = new Map();

/**
 * marauroa.rpobjectFactory
 * creates RPObjects
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpobjectFactory = new function() {

	this.register = function(name: string, clazz: typeof RPObject) {
		rpClasses.set(name, clazz);
	}

	this.create = function(rpclass: string): RPObject {
		let ctor = rpClasses.get(rpclass);
		if (!ctor) {
			console.log("Unknown RPClass " + rpclass);
			ctor = rpClasses.get("_default");
		}
		let temp = new ctor();
		temp.init();
		return temp;
	};

	rpClasses.set("_default", RPObject);
}



let defaultRPEvent = {
	execute: function(rpobject) {
		if (marauroa.debug.unknownEvents) {
			console.log("Unhandled event: ", this, " on ", rpobject);
		}
	}
}

let rpEvents = new Map();

/**
 * creates RPEvent
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpeventFactory = new function(){

	this.register = function(name, clazz) {
		rpEvents.set(name, clazz);
	}

	this.create = function(rpclass) {
		let ctor = rpEvents.get(rpclass);
		if (!ctor) {
			if (ctor) {
				console.log("Deprecated use of marauroa.rpeventFactory[name]. Please use marauroa.rpeventFactory.register() instead.")
			} else {
				ctor = rpEvents.get["_default"];
			}
		}
		return MarauroaUtils.fromProto(ctor, {});
	}

	rpEvents.set("_default", defaultRPEvent);
}

let defaultRPSlot = {
	_objects: [],

	add: function(value) {
		if (value && value["id"]) {
			this._objects.push(value);
		}
	},

	get: function(key) {
		var idx = this.getIndex(key);
		if (idx > -1) {
			return this._objects[idx];
		}
		return undefined;
	},

	getByIndex: function(idx) {
		return this._objects[idx];
	},

	count: function() {
		return this._objects.length;
	},

	getIndex: function(key) {
		var i;
		var c = this._objects.length;
		for (i = 0; i < c; i++) {
			if (this._objects[i]["id"] === key) {
				return i;
			}
		}
		return -1;
	},

	del: function(key) {
		var idx = this.getIndex(key);
		if (idx > -1) {
			this._objects.splice(idx, 1);
		}
	},

	first: function() {
		if (this._objects.length > 0) {
			return this._objects[0];
		}
		return undefined;
	}
}

let rpSlots = new Map();

/**
 * creates RPSlot
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpslotFactory = new function(){

	this.register = function(name, clazz) {
		rpSlots.set(name, clazz);
	}

	this.create = function(name) {
		let ctor = rpSlots.get(name);
		if (!ctor) {
			ctor = marauroa.rpslotFactory[name];
			if (ctor) {
				console.log("Deprecated use of marauroa.rpslotFactory[name]. Please use marauroa.rpslotFactory.register() instead.")
			} else {
				ctor = rpSlots.get("_default");
			}
		}
		let slot = MarauroaUtils.fromProto(ctor, {});
		slot._name = name;
		slot._objects = [];
		return slot;
	};

	rpSlots.set("_default", defaultRPSlot);
};

})();