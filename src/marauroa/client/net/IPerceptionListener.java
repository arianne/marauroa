/* $Id: IPerceptionListener.java,v 1.9 2007/03/23 20:39:15 arianne_rpg Exp $ */
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

import marauroa.common.game.RPObject;
import marauroa.common.net.message.MessageS2CPerception;

/**
 * The IPerceptionListener interface provides methods that are called while
 * applying the perception
 */
public interface IPerceptionListener {

	/**
	 * onAdded is called when an object is added to the world for first time or
	 * after a sync perception.
	 * Return true to stop further processing.
	 * @param object the added object.
	 * @return true to stop further processing
	 */
	public boolean onAdded(RPObject object);

	/**
	 * onModifiedAdded is called when an object is modified by adding or
	 * changing one of its attributes. Return true to stop further processing.
	 * Note that the method is called *before* modifing the object.
	 * @param object the original object
	 * @param changes the added and modified changes.
	 * @return true to stop further processing
	 */
	public boolean onModifiedAdded(RPObject object, RPObject changes);

	/**
	 * onModifiedDeleted is called each time the object has one of its
	 * attributes removed. Return true to stop further processing. Note that the
	 * method is called *before* modifing the object.
	 * @param object the original object
	 * @param changes the deleted attributes.
	 * @return true to stop further processing
	 */
	public boolean onModifiedDeleted(RPObject object, RPObject changes);

	/**
	 * onDeleted is called when an object is removed of the world Return true to
	 * stop further processing.
	 * @param object the original object
	 * @return true to stop further processing
	 */
	public boolean onDeleted(RPObject object);

	/**
	 * onMyRPObject is called when our rpobject avatar is processed. Return true
	 * to stop further processing.
	 * @param added the added and modified attributes and slots
	 * @param deleted the deleted attributes
	 * @return true to stop further processing
	 */
	public boolean onMyRPObject(RPObject added, RPObject deleted);

	/**
	 * onClear is called when the whole world is going to be cleared. It happens
	 * on sync perceptions Return true to stop further processing.
	 * @return true to stop further processing
	 */
	public boolean onClear();

	/**
	 * onSynced is called when the client recover sync
	 */
	public void onSynced();

	/**
	 * onUnsynced is called when the client lose sync
	 */
	public void onUnsynced();

	/**
	 * onPerceptionBegin is called when the perception is going to be applied
	 * @param type type of the perception: SYNC or DELTA
	 * @param timestamp the timestamp of the perception
	 */
	public void onPerceptionBegin(byte type, int timestamp);

	/**
	 * onPerceptionBegin is called when the perception has been applied
	 * @param type type of the perception: SYNC or DELTA
	 * @param timestamp the timestamp of the perception
	 */
	public void onPerceptionEnd(byte type, int timestamp);

	/**
	 * onException is called when an exception happens
	 * @param e the exception that happened.
	 * @param perception the message that causes the problem
	 */
	public void onException(Exception e, MessageS2CPerception perception) throws Exception;
}
