/***************************************************************************
 *                   (C) Copyright 2009-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db.command;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Pair;

/**
 * An asynchronous command queue.
 *
 * @author hendrik, madmetzger
 */
public class DBCommandQueue {
	private static DBCommandQueue instance;

	private BlockingQueue<Pair<DBCommand, Boolean>> pendingCommands = new LinkedBlockingQueue<Pair<DBCommand, Boolean>>();
// TODO:	private LinkedList<DBCommand> processedCommands = new LinkedList<DBCommand>();

	/**
	 * gets the singleton instance
	 *
	 * @return DBCommandQueue
	 */
	public static DBCommandQueue get() {
		if (instance == null) {
			instance = new DBCommandQueue();
		}
		return instance;
	}

	/**
	 * enqueues a "fire and forget" command.
	 *
	 * @param command DBCommand to add to the queue
	 */
	public void enqueue(DBCommand command) {
		pendingCommands.add(new Pair<DBCommand, Boolean>(command, Boolean.FALSE));
	}

	/**
	 * enqueues a command and remembers the result.
	 *
	 * @param command DBCommand to add to the queue
	 */
	public void enqueueAndAwaitResult(DBCommand command) {
		pendingCommands.add(new Pair<DBCommand, Boolean>(command, Boolean.TRUE));
	}
}
