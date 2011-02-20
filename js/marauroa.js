var marauroa = new function() {}

marauroa.log = {};

if (typeof(console) != "undefined") {
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

	first: function(obj) {
		for (var i in obj) {
			return obj[i];
		}
		return null;
	}
}
