marauroa.rpobjectFactory = {
	createRPObject: function(rpclass) {
		var ctor = this._default;
		if (typeof(this[rpclass]) != "undefined") {
			ctor = this[rpclass];
		}
		return new ctor;
	},

	_default: function() {
		this.onEvent = function(e) {
			marauroa.log.debug("Event: " + JSON.stringify(e));
		}
	}
}