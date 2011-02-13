/**
 * The IPerceptionListener interface provides methods that are called while
 * applying the perception
 */
marauroa.perceptionListener = {

	/**
	 * onAdded is called when an object is added to the world for first time or
	 * after a sync perception. Return true to stop further processing.
	 *
	 * @param object
	 *            the added object.
	 * @return true to stop further processing
	 */
	onAdded: function(object) {
	},

	/**
	 * onModifiedAdded is called when an object is modified by adding or
	 * changing one of its attributes. Return true to stop further processing.
	 * Note that the method is called *before* modifying the object.
	 *
	 * @param object
	 *            the original object
	 * @param changes
	 *            the added and modified changes.
	 * @return true to stop further processing
	 */
	onModifiedAdded: function(object, changes) {
	},

	/**
	 * onModifiedDeleted is called each time the object has one of its
	 * attributes removed. Return true to stop further processing. Note that the
	 * method is called *before* modifying the object.
	 *
	 * @param object
	 *            the original object
	 * @param changes
	 *            the deleted attributes.
	 * @return true to stop further processing
	 */
	onModifiedDeleted: function(object, changes) {
	},

	/**
	 * onDeleted is called when an object is removed of the world Return true to
	 * stop further processing.
	 *
	 * @param object
	 *            the original object
	 * @return true to stop further processing
	 */
	onDeleted: function(object) {
	},

	/**
	 * onMyRPObject is called when our rpobject avatar is processed. Return true
	 * to stop further processing.
	 *
	 * @param added
	 *            the added and modified attributes and slots
	 * @param deleted
	 *            the deleted attributes
	 * @return true to stop further processing
	 */
	onMyRPObject: function(added, deleted) {
	},

	/**
	 * onClear is called when the whole world is going to be cleared. It happens
	 * on sync perceptions Return true to stop further processing.
	 *
	 * @return true to stop further processing
	 */
	onClear: function() {
	},

	/**
	 * onPerceptionBegin is called when the perception is going to be applied
	 *
	 * @param type
	 *            type of the perception: SYNC or DELTA
	 * @param timestamp
	 *            the timestamp of the perception
	 */
	onPerceptionBegin: function(type, timestamp) {
	},

	/**
	 * onPerceptionBegin is called when the perception has been applied
	 *
	 * @param type
	 *            type of the perception: SYNC or DELTA
	 * @param timestamp
	 *            the timestamp of the perception
	 */
	onPerceptionEnd: function(type, timestamp) {
	},

	/**
	 * onException is called when an exception happens
	 *
	 * @param exception
	 *            the exception that happened.
	 * @param perception
	 *            the message that causes the problem
	 */
	onException: function(exception, perception) {
	}
}
marauroa.currentZone = {
	clear: function() {
		for (var i in this) {
			if (this.hasOwnProperty(i) && typeof(this[i]) != "function") {
				marauroa.log.debug("deleting: " + i + " " + typeof(this[i]) + ": " + isNaN(i));
				delete this[i];
			}
		}
	}
}
marauroa.perceptionHandler = {

	/**
	 * Apply a perception.
	 * 
	 * @param msg
	 *            the perception msg
	 */
	apply: function(msg) {
		marauroa.perceptionListener.onPerceptionBegin(msg.sync, msg.s);

		// clean world on login/zone change
		if (msg.sync) {
			if (!marauroa.perceptionListener.onClear()) {
				marauroa.currentZone.clear();
			}
		}

		// apply perception
		this.applyPerceptionAddedRPObjects(msg);
		this.applyPerceptionModifiedRPObjects(msg);
		this.applyPerceptionDeletedRPObjects(msg);
		this.applyPerceptionMyRPObject(msg);

		// done, tell marauroa.perceptionListener
		marauroa.perceptionListener.onPerceptionEnd(msg.sync, msg.s);
	},

	/**
	 * This method applys perceptions addedto the Map<RPObject::ID,RPObject>
	 * passed as argument. It clears the map if this is a sync perception
	 * 
	 * @param msg
	 *            the perception message
	 */
	applyPerceptionAddedRPObjects: function(msg) {
		if (msg.aO) {
			for (var i in msg.aO) {
				if (msg.aO.hasOwnProperty(i)) {
					if (!marauroa.perceptionListener.onAdded(msg.aO[i])) {
						var o = marauroa.rpobjectFactory.createRPObject(msg.aO[i].c);
						this.addChanges(o, msg.aO[i]);
						marauroa.currentZone[msg.aO[i].a.id] = o;
					}
				}
			}
		}
	},

	/**
	 * This method applys perceptions deleted to the Map<RPObject::ID,RPObject>
	 * passed as argument.
	 * 
	 * @param msg
	 *            the perception message
	 */
	applyPerceptionDeletedRPObjects: function(msg) {
		if (msg.dO) {
			for (var i in msg.dO) {
				if (msg.dO.hasOwnProperty(i)) {
					if (!marauroa.perceptionListener.onDeleted(msg.dO[i])) {
						delete marauroa.currentZone[i];
					}
				}
			}
		}
	},

	/**
	 * This method applies perceptions modified added and modified deleted to the
	 * Map<RPObject.ID,RPObject> passed as argument.
	 * 
	 * @param msg
	 *            the perception message
	 */
	applyPerceptionModifiedRPObjects: function(msg) {

		// deleted attributes
		if (msg.dA) {
			for (var i in msg.dA) {
				if (msg.dA.hasOwnProperty(i)) {
					if (typeof(marauroa.currentZone[msg.dA[i].a.id]) != "undefined") {
						var o = marauroa.currentZone[msg.dA[i].a.id];
						if (!marauroa.perceptionListener.onModifiedDeleted(msg.dA[i])) {
							this.deleteChanges(o, msg.dA[i]);
						}
					}
				}
			}
		}

		// added attributes
		if (msg.aA) {
			for (var i in msg.aA) {
				if (msg.aA.hasOwnProperty(i)) {
					if (typeof(marauroa.currentZone[msg.aA[i].a.id]) != "undefined") {
						var o = marauroa.currentZone[msg.aA[i].a.id];
						if (!marauroa.perceptionListener.onModifiedAdded(msg.aA[i])) {
							this.addChanges(o, msg.aA[i]);
						}
					}
				}
			}
		}
	},

	/**
	 * This method applys perceptions for our RPObject to the Map<RPObject::ID,RPObject>
	 * passed as argument.
	 * 
	 * @param msg
	 *            the perception message
	 */
	applyPerceptionMyRPObject: function(msg) {

		this.addMyRPObjectToWorldIfPrivate(msg.aM);

		if (!marauroa.perceptionListener.onMyRPObject(msg.aM, msg.dM)) {
			var id = -1;
			if (typeof(msg.aM) != "undefined") {
				id = msg.aM.a.id;
			}

			if (typeof(msg.dM) != "undefined") {
				id = msg.dM.a.id;
			}

			if (typeof(id) == "undefined") {
				return;
			}

			var o = marauroa.currentZone[id];
			this.deleteChanges(o, msg.dM);
			this.addChanges(o, msg.aM);
		}
	},

	/**
	 * adds our RPObject to the world in case it was not already added by the public perception.
	 *
	 * @param added added changes of my object
	 */
	addMyRPObjectToWorldIfPrivate: function(added) {
		if (typeof(added) == "undefined") {
			return;
		}
		if (typeof(marauroa.currentZone[added.id]) != "undefined") {
			return;
		}
		if (!marauroa.perceptionListener.onAdded(added)) {
			var o = marauroa.rpobjectFactory.createRPObject(added.c);
			this.addChanges(o, added);
			marauroa.currentZone[added.id] = o;
		}
	},

	deleteChanges: function(object, diff) {
		if (typeof(diff) == "undefined") {
			return;
		}

		// delete attributes
		if (typeof(diff.a) != "undefined") {
			for (var i in diff.a) {
				if (diff.a.hasOwnProperty(i) && i != "id" && i != "zoneid") {
					delete object[i];
				}
			}
		}

		// delete slots and/or their content
		if (typeof(diff.s) != "undefined") {
			for (var i in diff.s) {
				if (!diff.s.hasOwnProperty(i)) {
					continue;
				} 
				if (isEmpty(diff.s[i])) {
					delete object[diff.s[i]];
				} else {
					for (var j in diff.s[i]) {
						if (diff.s[i].hasOwnProperty(j)) {
							delete object[i][diff.s[i][j].id];
						}
					}
				}
			}
		}

		// delete maps and/or their content
		if (typeof(diff.m) != "undefined") {
			for (var i in diff.m) {
				if (!diff.m.hasOwnProperty(i)) {
					continue;
				} 
				if (isEmpty(diff.m[i])) {
					delete object[diff.m[i]];
				} else {
					for (var j in diff.m[i]) {
						if (diff.m[i].hasOwnProperty(j)) {
							delete object[i][diff.m[i][j]];
						}
					}
				}
			}
		}
		// TODO: links
	},

	isEmpty: function(obj) {
		for (var i in obj) {
			if (obj.hasOwnProperty(i)) {
				return false;
			}
		}
		return true;
	},

	addChanges: function(object, diff) {
		if (typeof(diff) == "undefined") {
			return;
		}

		// attributes
		for (var i in diff.a) {
			if (diff.a.hasOwnProperty(i)) {
				object[i] = diff.a[i];
			}
		}

		// maps
		if (typeof(diff.m) != "undefined") {
			for (var i in diff.m) {
				if (diff.m.hasOwnProperty(i)) {
					if (typeof(object[i]) == "undefined") {
						object[i] = {};
					}
					for (var j in diff.m[i]) {
						if (diff.m[i].hasOwnProperty(j)) {
							object[i][j] = diff.m[i][j];
						}
					}
				} 
			}
		}

		// slots
		if (typeof(diff.s) != "undefined") {
			for (var i in diff.s) {
				if (diff.s.hasOwnProperty(i)) {
					if (typeof(object[i]) == "undefined") {
						object[i] = {};
					}
					for (var j in diff.s[i]) {
						if (diff.s[i].hasOwnProperty(j)) {
							if (typeof(object[i][diff.m[i][j].id]) == "undefined") {
								diff.m[i][j].id = {};
							}
							this.addChanges(diff.m[i][j].id, diff.m[i][j])
						}
					}
				} 
			}
		}

		// events
		if (typeof(diff.e) != "undefined" && typeof(object.onEvent) != "undefined") {
			for (var i in diff.e) {
				if (diff.e.hasOwnProperty(i)) {
					object.onEvent(diff.e[i]);
				} 
			}
		}

		// TODO: links
	}

}
