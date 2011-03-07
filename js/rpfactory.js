/**
 * creates RPObjects
 *
 * use the rpclass name as attribute name for a prototype object
 */
marauroa.rpobjectFactory = new function(){
	this._default = function() {};
	this._default.onEvent = function(e) {
		marauroa.log.debug("Event: ", e);
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
