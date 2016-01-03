"use strict";

var marauroa = new function() {}

marauroa.debug = {
	messages: false,
	unknownEvents: true
}

if (!window.console) {
	window.console = {};
}
if (!window.console.log) {
	window.console.log = function() {};
	window.console.info = function() {};
	window.console.warn = function() {};
	window.console.error = function(text) {alert(text)};
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
			if (obj.hasOwnProperty(i)) {
				return obj[i];
			}
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
