marauroa.rpobjectFactory = new function(){
	this._default = function() {};
	this._default.onEvent = function(e) {
		marauroa.log.debug("Event: " + JSON.stringify(e));
	}

	this.createRPObject = function(rpclass) {
		var ctor = this._default;
		if (typeof(this[rpclass]) != "undefined") {
			ctor = this[rpclass];
		}
		return new ctor;
	}

}