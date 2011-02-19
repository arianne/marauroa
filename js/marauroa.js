var marauroa = new function() {}

marauroa.log = {};
marauroa.log.debug = console.debug || function(text) {};
marauroa.log.info = console.info || function(text) {};
marauroa.log.warn = console.warn || function(text) {};
marauroa.log.error = console.error || function(text) {alert(text)};

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
