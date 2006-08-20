/* $Id: RPActionList.java,v 1.3 2006/08/20 15:40:15 wikipedian Exp $ */
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
package marauroa.server.game;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import marauroa.common.game.RPAction;

/** This class represents a list of RPActions and uses a LinkedList as backstore. * */
public class RPActionList implements Iterable<RPAction> {
	/** A LinkedList<RPAction> that contains actions */
	private LinkedList<RPAction> actionsList;

	public RPActionList() {
		actionsList = new LinkedList<RPAction>();
	}

	/**
	 * This method adds a new rp action to list
	 * 
	 * @param rp_action -
	 *            RPAction to add into list
	 * @return actions that was just added
	 */
	public RPAction add(RPAction rp_action) {
		actionsList.add(rp_action);
		return (rp_action);
	}

	/**
	 * This method gets the RPAction
	 * 
	 * @param index
	 *            index of RPAction to retrieve
	 * @return actions that was just added
	 */
	public RPAction get(int index) {
		return (actionsList.get(index));
	}

	/**
	 * This method removes the RPAction at position index
	 * 
	 * @param index
	 *            index of RPAction to remove
	 * @return actions that was just removed
	 */
	public RPAction remove(int index) {
		return actionsList.remove(index);
	}

	/**
	 * This method gets the size
	 * 
	 * @return count of RPActions in this list
	 */
	public int size() {
		return (actionsList.size());
	}

	/**
	 * gets the RP Actions Iterator
	 * 
	 * @return Iterator<RPAction>
	 */
	public Iterator<RPAction> iterator() {
		return (Collections.unmodifiableList(actionsList).iterator());
	}

}
