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

"use strict";

var marauroa = window.marauroa = window.marauroa || {};

(function() {

let defaultRPObject = {
	onEvent: function(e) {
		var event = marauroa.rpeventFactory.create(e["c"]);
		for (var i in e["a"]) {
			if (e["a"].hasOwnProperty(i)) {
				event[i] = e["a"][i];
			}
			event["_rpclass"] = e["c"];
		}
		// Event slots
		for (var slot in e["s"]) {
			if (e["s"].hasOwnProperty(slot)) {
				event[slot] = e["s"][slot];
			}
		}
		event.execute(this);
	},
	set: function(key, value) {
		this[key] = value;
	},
	setMapEntry: function(map, key, value) {
		this[map][key] = value;
	},
	unset: function(key) {
		delete this[key];
	},
	unsetMapEntry: function(map, key) {
		delete this[map][key];
	},
	destroy: function(parent) {
		// do nothing
	},
	createSlot: function(name) {
		var slot = marauroa.rpslotFactory.create(name);
		slot._parent = this;
		return slot;
	},

	init: function() {
		// do nothing
	}
	
}

let rpClasses = new Map();

/**
 * marauroa.rpobjectFactory
 * creates RPObjects
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpobjectFactory = new function() {

	this.register = function(name, clazz) {
		rpClasses.set(name, clazz);
	}

	this.create = function(rpclass) {
		let ctor = rpClasses.get(rpclass);
		if (!ctor) {
			if (ctor) {
				console.log("Deprecated use of marauroa.rpobjectFactory[name]. Please use marauroa.rpobjectFactory.register() instead.")
			} else {
				ctor = rpClasses.get("_default");
			}
		}
		let temp = marauroa.util.fromProto(ctor);
		temp.init();
		return temp;
	};

	rpClasses.set("_default", defaultRPObject);
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
		return marauroa.util.fromProto(ctor);
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
		let slot = marauroa.util.fromProto(ctor);
		slot._name = name;
		slot._objects = [];
		return slot;
	};

	rpSlots.set("_default", defaultRPSlot);
};

})();