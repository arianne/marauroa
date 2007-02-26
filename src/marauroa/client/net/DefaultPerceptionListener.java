/* $Id: DefaultPerceptionListener.java,v 1.5 2007/02/26 20:08:11 arianne_rpg Exp $ */
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

public class DefaultPerceptionListener implements IPerceptionListener {
	public DefaultPerceptionListener() {
	}

	public boolean onAdded(RPObject object) {
		return false;
	}

	public boolean onModifiedAdded(RPObject object, RPObject changes) {
		return false;
	}

	public boolean onModifiedDeleted(RPObject object, RPObject changes) {
		return false;
	}

	public boolean onDeleted(RPObject object) {
		return false;
	}

	public boolean onMyRPObject(RPObject added, RPObject deleted) {
		return false;
	}

	public boolean onClear() {
		return false;
	}

	public int onTimeout() {
		return 0;
	}

	public void onSynced() {
	}

	public void onUnsynced() {
	}

	public void onPerceptionBegin(byte type, int timestamp) {
	}

	public void onPerceptionEnd(byte type, int timestamp) {
	}

	public void onException(Exception e, MessageS2CPerception perception)
			throws Exception {
		System.out.println(e.getMessage());
		System.out.println(perception);
		e.printStackTrace();

		throw e;
	}
}
