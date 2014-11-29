var marauroa = new function() {}

marauroa.debug = {
	messages: false,
	unknownEvents: true
}

marauroa.log = {};

if ((typeof(console) != "undefined") && (typeof(console.debug) != "undefined")) {
	marauroa.log.debug = function () { console.debug.apply(console, arguments); };
	marauroa.log.info = function () { console.info.apply(console, arguments); };
	marauroa.log.warn = function () { console.warn.apply(console, arguments); };
	marauroa.log.error = function () { console.error.apply(console, arguments); };
} else {
	marauroa.log.debug = function(text) {};
	marauroa.log.info = function(text) {};
	marauroa.log.warn = function(text) {};
	marauroa.log.error = function(text) {alert(text)};
}

marauroa.util = {
	isEmpty: function(obj) {
		for (var i in obj) {
			if (obj.hasOwnProperty(i)) {
				return false;
			}
		}
		return true;
	},

	isEmptyExceptId: function(obj) {
		for (var i in obj) {
			if (i != "id" && obj.hasOwnProperty(i)) {
				return false;
			}
		}
		return true;
	},

	first: function(obj) {
		for (var i in obj) {
			return obj[i];
		}
	},

	fromProto: function(proto, def) {
		var f = function() {
			this.proto = proto;
		};
		f.prototype = proto;
		var obj = new f();
		if (!def) {
			return obj;
		}
		return marauroa.util.merge(obj, def);
	},
	
	merge: function(a, b) {
		for (var key in b) {
			a[key] = b[key];
		}
		return a;
	}
}

String.prototype.trim = function() {
	return this.replace(/^\s+|\s+$/g, "");
};
