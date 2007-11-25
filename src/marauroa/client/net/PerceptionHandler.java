/* $Id: PerceptionHandler.java,v 1.22 2007/11/25 19:03:16 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.client.net;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.game.Perception;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.net.message.MessageS2CPerception;

import org.apache.log4j.NDC;

/**
 * The PerceptionHandler class is in charge of applying correctly the
 * perceptions to the world. You should always use this class because it is a
 * complex task that is easy to do in the wrong way.
 */
public class PerceptionHandler {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(PerceptionHandler.class);

	/** This is the class that interpret the perception contents. */
	private IPerceptionListener listener;

	/** A list of previous perceptions that are still waiting for being applied. */
	private List<MessageS2CPerception> previousPerceptions;

	/** the timestamp of last sucessfully applied perception */
	private int previousTimestamp;

	/** This is true if we are synced with server representation. */
	private boolean synced;

	/**
	 * Constructor
	 * 
	 */
	public PerceptionHandler() {
		synced = false;
		previousTimestamp = -1;
		previousPerceptions = new LinkedList<MessageS2CPerception>();
	}

	/**
	 * Constructor
	 * 
	 * @param listener
	 *            the listener that will give meaning to perception handler.
	 */
	public PerceptionHandler(IPerceptionListener listener) {
		this();
		this.listener = listener;
	}

	/**
	 * Apply a perception to a world instance.
	 * 
	 * @param message
	 *            the perception message
	 * @param world_instance
	 *            a map representing objects stored in a zone.
	 * @throws Exception
	 */
	public void apply(MessageS2CPerception message, Map<RPObject.ID, RPObject> world_instance)
	        throws Exception {
		listener.onPerceptionBegin(message.getPerceptionType(), message.getPerceptionTimestamp());
		
		/*
		 * We want to clear previous delta^2 info in the objects.
		 * Delta^2 is only useful in server for getting changes done to the object.
		 */
		for(RPObject obj: world_instance.values()) {
			obj.resetAddedAndDeleted();
		}

		/*
		 * When we get a sync perception, we set sync flag to true and clear the
		 * stored data to renew it.
		 */
		if (message.getPerceptionType() == Perception.SYNC) {
			try {
				/** OnSync: Keep processing */
				previousTimestamp = message.getPerceptionTimestamp();
				previousPerceptions.clear();

				applyPerceptionAddedRPObjects(message, world_instance);
				applyPerceptionMyRPObject(message, world_instance);

				if (!synced) {
					synced = true;
					listener.onSynced();
				}
			} catch (Exception e) {
				listener.onException(e, message);
			}
			/*
			 * When we get a delta perception, we need to check that it is the
			 * one that we are expecting.
			 */
		} else if (message.getPerceptionType() == Perception.DELTA
		        && previousTimestamp + 1 == message.getPerceptionTimestamp()) {
			try {
				/** OnSync: Keep processing */
				previousTimestamp = message.getPerceptionTimestamp();

				applyPerceptionDeletedRPObjects(message, world_instance);
				applyPerceptionModifiedRPObjects(message, world_instance);
				applyPerceptionAddedRPObjects(message, world_instance);
				applyPerceptionMyRPObject(message, world_instance);
			} catch (Exception e) {
				listener.onException(e, message);
			}
			/*
			 * In any other case, store the perception and check if it helps
			 * applying any of the still to be applied perceptions.
			 */
		} else {
			previousPerceptions.add(message);

			for (Iterator<MessageS2CPerception> it = previousPerceptions.iterator(); it.hasNext();) {
				MessageS2CPerception previousmessage = it.next();
				if (previousTimestamp + 1 == previousmessage.getPerceptionTimestamp()) {
					try {
						/** OnSync: Keep processing */
						previousTimestamp = previousmessage.getPerceptionTimestamp();

						applyPerceptionDeletedRPObjects(previousmessage, world_instance);
						applyPerceptionModifiedRPObjects(previousmessage, world_instance);
						applyPerceptionAddedRPObjects(previousmessage, world_instance);
						applyPerceptionMyRPObject(previousmessage, world_instance);
					} catch (Exception e) {
						listener.onException(e, message);
					}
					it.remove();
				}
			}

			/* If there are no preceptions that means we are synced */
			if (previousPerceptions.isEmpty()) {
				synced = true;
				listener.onSynced();
			} else {
				synced = false;
				listener.onUnsynced();
			}
		}

		/* Notify the listener that the perception is applied */
		listener.onPerceptionEnd(message.getPerceptionType(), message.getPerceptionTimestamp());
	}

	/**
	 * This method applys perceptions addedto the Map<RPObject::ID,RPObject>
	 * passed as argument. It clears the map if this is a sync perception
	 * 
	 * @param message
	 *            the perception message
	 * @param world
	 *            the container of objects
	 */
	private void applyPerceptionAddedRPObjects(MessageS2CPerception message,
	        Map<RPObject.ID, RPObject> world) throws RPObjectNotFoundException {
		try {
			/*
			 * If the perception is Sync, we clear the contents of the
			 * container.
			 */
			if (message.getPerceptionType() == Perception.SYNC) {
				if (!listener.onClear()) {
					world.clear();
				}
			}

			/* Now add the objects to the container. */
			for (RPObject object : message.getAddedRPObjects()) {
				if (!listener.onAdded(object)) {
					world.put(object.getID(), object);
				}
			}
		} catch (Exception e) {
			logger.error("error in applyPerceptionAddedRPObjects", e);
			throw new RPObjectNotFoundException(RPObject.INVALID_ID);
		}
	}

	/**
	 * This method applys perceptions deleted to the Map<RPObject::ID,RPObject>
	 * passed as argument.
	 * 
	 * @param message
	 *            the perception message
	 * @param world
	 *            the container of objects
	 */
	private void applyPerceptionDeletedRPObjects(MessageS2CPerception message,
	        Map<RPObject.ID, RPObject> world) throws RPObjectNotFoundException {
		try {
			for (RPObject object : message.getDeletedRPObjects()) {
				if (!listener.onDeleted(object)) {
					world.remove(object.getID());
				}
			}
		} catch (Exception e) {
			logger.error("error in applyPerceptionDeletedRPObjects", e);
			throw new RPObjectNotFoundException(RPObject.INVALID_ID);
		}
	}

	/**
	 * This method applys perceptions modified added and modified deleted to the
	 * Map<RPObject::ID,RPObject> passed as argument.
	 * 
	 * @param message
	 *            the perception message
	 * @param world
	 *            the container of objects
	 */
	private void applyPerceptionModifiedRPObjects(MessageS2CPerception message,
	        Map<RPObject.ID, RPObject> world) throws RPObjectNotFoundException {
		try {
			/* First we remove the deleted attributes */
			for (RPObject object : message.getModifiedDeletedRPObjects()) {
				RPObject w_object = world.get(object.getID());
				if (!listener.onModifiedDeleted(w_object, object)) {
					w_object.applyDifferences(null, object);
				}
			}

			/* And then we add the new and modified attributes */
			for (RPObject object : message.getModifiedAddedRPObjects()) {
				RPObject w_object = world.get(object.getID());
				if (!listener.onModifiedAdded(w_object, object)) {
					w_object.applyDifferences(object, null);
				}
			}
		} catch (RPObjectNotFoundException e) {
			NDC.push("world is [" + world.toString() + "]");
			logger.error("error in applyModifiedRPObjects", e);
			NDC.pop();
			throw e;
		} catch (Exception e) {
			NDC.push("world is [" + world.toString() + "]");
			logger.error("error in applyModifiedRPObjects", e);
			NDC.pop();
			throw new RPObjectNotFoundException(RPObject.INVALID_ID);
		}
	}

	/**
	 * This method applys perceptions for our RPObject to the Map<RPObject::ID,RPObject>
	 * passed as argument.
	 * 
	 * @param message
	 *            the perception message
	 * @param world
	 *            the container of objects
	 */
	private void applyPerceptionMyRPObject(MessageS2CPerception message,
	        Map<RPObject.ID, RPObject> world) throws RPObjectNotFoundException {
		try {
			RPObject added = message.getMyRPObjectAdded();
			RPObject deleted = message.getMyRPObjectDeleted();

			if (!listener.onMyRPObject(added, deleted)) {
				RPObject.ID id = null;

				if (added != null) {
					id = added.getID();
				}

				if (deleted != null) {
					id = deleted.getID();
				}

				if (id == null) {
					return;
				}

				RPObject object = world.get(id);

				object.applyDifferences(added, deleted);
			}
		} catch (Exception e) {
			logger.error("error in applyPerceptionMyRPObject", e);
			throw new RPObjectNotFoundException(RPObject.INVALID_ID);
		}
	}
}
