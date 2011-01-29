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

