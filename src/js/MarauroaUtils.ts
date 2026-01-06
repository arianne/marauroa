/**
 * @license
 ***************************************************************************
 *                   (C) Copyright 2011-2026 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

export class MarauroaUtils {

	static isEmpty(obj: object) {
		for (let i in obj) {
			if (obj.hasOwnProperty(i)) {
				return false;
			}
		}
		return true;
	}

	static isEmptyExceptId(obj: object) {
		for (let i in obj) {
			if (i !== "id" && obj.hasOwnProperty(i)) {
				return false;
			}
		}
		return true;
	}

	static first(obj: object) {
		for (var i in obj) {
			if (obj.hasOwnProperty(i)) {
				return obj[i];
			}
		}
	}

	static fromProto(proto: any, def: object) {
		let obj: object;
		if (typeof(proto) === "function") {
			obj = new proto();
		} else {
			/**
			 * @constructor
			 */
			var F = function() {
				this.proto = proto;
			};
			F.prototype = proto;
			obj = new F();
		}

		if (!def) {
			return obj;
		}
		return MarauroaUtils.merge(obj, def);
	}

	static merge(a: object, b: object) {
		for (let key in b) {
			a[key] = b[key];
		}
		return a;
	}
};
