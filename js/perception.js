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
marauroa.currentZone = {}
marauroa.perceptionHandler = {

	/**
	 * Apply a perception.
	 * 
	 * @param message
	 *            the perception message
	 */
	apply: function(perceptionmessage) {
		marauroa.perceptionListener.onPerceptionBegin(message.sync, message.s);

		// clean world on login/zone change
		if (message.sync) {
			if (!marauroa.perceptionListener.onClear()) {
				marauroa.currentZone.clear();
			}
		}

		// apply perception
		applyPerceptionAddedRPObjects(message);
		applyPerceptionModifiedRPObjects(message);
		applyPerceptionDeletedRPObjects(message);
		applyPerceptionMyRPObject(message);

		// done, tell marauroa.perceptionListener
		marauroa.perceptionListener.onPerceptionEnd(message.sync, message.s);
	},

	/**
	 * This method applys perceptions addedto the Map<RPObject::ID,RPObject>
	 * passed as argument. It clears the map if this is a sync perception
	 * 
	 * @param message
	 *            the perception message
	 */
	applyPerceptionAddedRPObjects: function(message) {
		if (message.aO) {
			for (i in message.aO) {
				if (!marauroa.perceptionListener.onAdded(message.aO[i])) {
					o = marauroa.rpobjectFactory.createRPObject(message.aO[i].c);
					addChanges(o, message.aO[i]);
					marauroa.currentZone[message.aO[i].a.id] = o;
				}
			}
		}
	},

	/**
	 * This method applys perceptions deleted to the Map<RPObject::ID,RPObject>
	 * passed as argument.
	 * 
	 * @param message
	 *            the perception message
	 */
	applyPerceptionDeletedRPObjects: function(message) {
		if (message.dO) {
			for (i in message.dO) {
				if (!marauroa.perceptionListener.onDeleted(message.dO[i])) {
					delete marauroa.currentZone[i];
				}
			}
		}
	},

	/**
	 * This method applies perceptions modified added and modified deleted to the
	 * Map<RPObject.ID,RPObject> passed as argument.
	 * 
	 * @param message
	 *            the perception message
	 */
	applyPerceptionModifiedRPObjects: function(message) {

		// deleted attributes
		if (message.dA) {
			for (i in message.dA) {
				if (typeof(marauroa.currentZone[message.dA[i].a.id]) != "undefined") {
					o = marauroa.currentZone[message.dA[i].a.id];
					if (!marauroa.perceptionListener.onModifiedDeleted(message.dA[i])) {
						deleteChanges(o, message.dA[i]);
					}
				}
			}
		}

		// added attributes
		if (message.aA) {
			for (i in message.aA) {
				if (typeof(marauroa.currentZone[message.aA[i].a.id]) != "undefined") {
					o = marauroa.currentZone[message.aA[i].a.id];
					if (!marauroa.perceptionListener.onModifiedAdded(message.aA[i])) {
						addChanges(o, message.aA[i]);
					}
				}
			}
		}
	},

	/**
	 * This method applys perceptions for our RPObject to the Map<RPObject::ID,RPObject>
	 * passed as argument.
	 * 
	 * @param message
	 *            the perception message
	 */
	applyPerceptionMyRPObject: function(message) {

		addMyRPObjectToWorldIfPrivate(added);

		if (!marauroa.perceptionListener.onMyRPObject(message.aM, message.dM)) {
			if (typeof(message.aM) != "undefined") {
				id = message.aM.a.id;
			}

			if (typeof(message.dM) != "undefined") {
				id = message.dM.a.id;
			}

			if (typeof(id) == "undefined") {
				return;
			}

			o = marauroa.currentZone[id];
			deleteChanges(o, message.dM);
			addChanges(o, message.aM);
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
			o = marauroa.rpobjectFactory.createRPObject(added.c);
			addChanges(o, added);
			marauroa.currentZone[added.id] = o;
		}
	},

	deleteChanges: function(object, diff) {
		if (typeof(diff) == "undefined") {
			return;
		}
		for (i in diff) {
			if (i != "id" && i != "zoneid" && i[0] != "_slots" && i[0] != "_links" && i[0] != "_events") {
				object[i] = undefined;
			}
		}
		// TODO: slots, maps, links
	},

	addChanges: function(object, diff) {
		if (typeof(diff) == "undefined") {
			return;
		}
		for (i in diff) {
			if (i != "id" && i != "zoneid" && i[0] != "_slots" && i[0] != "_links" && i[0] != "_events") {
				object[i] = diff[i];
			}
		}
		
		// TODO: slots, maps, links, events
	}

}
