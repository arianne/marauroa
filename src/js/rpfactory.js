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
			marauroa.log.debug("Unhandled event: ", this, " on ", rpobject);
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
	this._default = function() {};
	this._default.add = function(key, value) {
		this[key] = value;
	}
	this._default.del = function(key) {
		delete this[key];
	}
	this._default.first = function() {
		for (var i in this) {
			if (!isNaN(i)) {
				return this[i];
			}
		}
	}

	this.create = function(name) {
		var ctor = this._default;
		if (typeof(this[name]) != "undefined") {
			ctor = this[name];
		}
		var slot = marauroa.util.fromProto(ctor);
		slot._name = name;
		return slot;
	}
}
