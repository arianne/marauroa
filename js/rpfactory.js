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

	this.create = function(rpclass) {
		var ctor = this._default;
		if (typeof(this[rpclass]) != "undefined") {
			ctor = this[rpclass];
		}
		return marauroa.util.fromProto(ctor);
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
		marauroa.log.debug("Unhandled event: ", this, " on ", rpobject);
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

	this.create = function(rpclass) {
		var ctor = this._default;
		if (typeof(this[rpclass]) != "undefined") {
			ctor = this[rpclass];
		}
		return marauroa.util.fromProto(ctor);
	}
}
