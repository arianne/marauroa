/***************************************************************************
 *                   (C) Copyright 2011-2016 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

"use strict";

var marauroa = window.marauroa || {};

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
	window.console.error = function(text) {
		alert(text);
	};
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
			if (i !== "id" && obj.hasOwnProperty(i)) {
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
		var F = function() {
			this.proto = proto;
		};
		F.prototype = proto;
		var obj = new F();
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
};
