marauroa.rpobjectFactory = {
	createRPObject: function(rpclass) {
		ctr = _default;
		if (typeof(this[rpclass]) != "undefined") {
			ctor = this[rpclass];
		}
		return new ctor;
	},

	_default: function() {
		
	}
}