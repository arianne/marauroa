/* $Id: IPerceptionListener.java,v 1.1 2005/01/23 21:00:41 arianne_rpg Exp $ */
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

import marauroa.common.game.*;
import marauroa.common.net.*;

/** The IPerceptionListener interface provides methods that are called while
 *  applying the perception */
public interface IPerceptionListener
	{
	/** onAdded is called when an object is added to the world for first time.
	 *  Return true to stop further processing. */
	public boolean onAdded(RPObject object);
	/** onModifiedAdded is called when an object is modified by adding or changing
	 *  one of its attributes. Return true to stop further processing. */
	public boolean onModifiedAdded(RPObject object, RPObject changes);
	/** onModifiedDeleted is called each time the object has one of its attributes
	 *  removed. Return true to stop further processing. */

	public boolean onModifiedDeleted(RPObject object, RPObject changes);
	/** onDeleted is called when an object is removed of the world
	 *  Return true to stop further processing. */
	public boolean onDeleted(RPObject object);
	/** onMyRPObject is called when our rpobject avatar is processed.
	 *  Return true to stop further processing. */
	public boolean onMyRPObject(boolean changed,RPObject object);
	/** onClear is called when the whole world is going to be cleared.
	 *  It happens on sync perceptions
	 *  Return true to stop further processing. */
	public boolean onClear();
 
	/** onTimeout is called when the client has timeout, that is, when it is 50 turns far from server*/
	public int onTimeout();
	/** onSynced is called when the client recover sync */
	public int onSynced();
	/** onUnsynced is called when the client lose sync */
	public int onUnsynced();
	
	/** onPerceptionBegin is called when the perception is going to be applied */
	public int onPerceptionBegin(byte type, int timestamp);
	/** onPerceptionBegin is called when the perception has been applied */
	public int onPerceptionEnd(byte type, int timestamp);
	/** onException is called when an exception happens */
	public int onException(Exception e, MessageS2CPerception perception) throws Exception;
	}

