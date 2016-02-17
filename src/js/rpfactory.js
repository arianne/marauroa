/***************************************************************************
 *                   (C) Copyright 2011-2016 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

var marauroa = window.marauroa || {};

/**
 * creates RPObjects
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpobjectFactory = new function(){
	this._default = function() {};
	this._default.onEvent = function(e) {
		var event = marauroa.rpeventFactory.create(e.c);
		for (var i in e.a) {
			if (e.a.hasOwnProperty(i)) {
				event[i] = e.a[i];
			}
			event._rpclass = e.c;
		}
		// Event slots
		for (var slot in e.s) {
			if (e.s.hasOwnProperty(slot)) {
				event[slot] = e.s[slot];
			}
		}
		event.execute(this);
	}
	this._default.set = function(key, value) {
		this[key] = value;
	}
	this._default.setMapEntry = function(map, key, value) {
		this[map][key] = value;
	}
	this._default.unset = function(key) {
		delete this[key];
	}
	this._default.unsetMapEntry = function(map, key) {
		delete this[map][key];
	}
	this._default.destroy = function(parent) {
		// do nothing
	}
	this._default.createSlot = function(name) {
		var slot = marauroa.rpslotFactory.create(name);
		slot._parent = this;
		return slot;
	}

	this._default.init = function() {
		// do nothing
	}

	this.create = function(rpclass) {
		var ctor = this._default;
		if (typeof(this[rpclass]) != "undefined") {
			ctor = this[rpclass];
		}
		var temp = marauroa.util.fromProto(ctor);
		temp.init();
		return temp;
	}
}




/**
 * creates RPEvent
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpeventFactory = new function(){
	this._default = function() {};
	this._default.execute = function(rpobject) {
		if (marauroa.debug.unknownEvents) {
			console.log("Unhandled event: ", this, " on ", rpobject);
		}
	}

	this.create = function(rpclass) {
		var ctor = this._default;
		if (typeof(this[rpclass]) != "undefined") {
			ctor = this[rpclass];
		}
		return marauroa.util.fromProto(ctor);
	}
}



/**
 * creates RPSlot
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpslotFactory = new function(){
	this._default = function() {
		// this method is never called?!
		this._objects = [];
	};
	this._default.add = function(value) {
		if (value && value.id) {
			this._objects.push(value);
		}
	}
	this._default.get = function(key) {
		var idx = this.getIndex(key);
		if (idx > -1) {
			return this._objects[idx];
		}
		return undefined;
	}
	this._default.getByIndex = function(idx) {
		return this._objects[idx];
	}
	this._default.count = function() {
		return this._objects.length;
	}
	this._default.getIndex = function(key) {
		var i;
		var c = this._objects.length;
		for (i = 0; i < c; i++) {
			if (this._objects[i].id === key) {
				return i;
			}
		}
		return -1;
	}
	this._default.del = function(key) {
		var idx = this.getIndex(key);
		if (idx > -1) {
			this._objects.splice(idx, 1);
		}
	}
	this._default.first = function() {
		if (this._objects.length > 0) {
			return this._objects[0];
		}
		return undefined;
	}

	this.create = function(name) {
		var ctor = this._default;
		if (typeof(this[name]) != "undefined") {
			ctor = this[name];
		}
		var slot = marauroa.util.fromProto(ctor);
		slot._name = name;
		slot._objects = [];
		return slot;
	}
}
