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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An asynchronous command queue.
 *
 * @author hendrik, madmetzger
 */
public class DBCommandQueue {
	private static DBCommandQueue instance;

	private BlockingQueue<DBCommandMetaData> pendingCommands = new LinkedBlockingQueue<DBCommandMetaData>();
	private List<DBCommandMetaData> processedCommands = Collections.synchronizedList(new LinkedList<DBCommandMetaData>());

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
		pendingCommands.add(new DBCommandMetaData(command, Thread.currentThread(), false));
	}

	/**
	 * enqueues a command and remembers the result.
	 *
	 * @param command DBCommand to add to the queue
	 */
	public void enqueueAndAwaitResult(DBCommand command) {
		pendingCommands.add(new DBCommandMetaData(command, Thread.currentThread(), true));
	}

	/**
	 * gets the processed results of the specified DBCommand class that have
	 * been requested in the current thread.
	 *
	 * @param <T> the type of the DBCommand
	 * @param clazz the type of the DBCommand
	 * @return a list of processed DBCommands; it may be empty
	 */
	@SuppressWarnings("unchecked")
	public <T extends DBCommand> List<T> getResults(Class<T> clazz) {
		LinkedList<T> res = new LinkedList<T>();
		Thread currentThread = Thread.currentThread();

		synchronized(processedCommands) {
			Iterator<DBCommandMetaData> itr = processedCommands.iterator();
			while (itr.hasNext()) {
				DBCommandMetaData metaData = itr.next();
				DBCommand command = metaData.getCommand();
				if (clazz.isAssignableFrom(command.getClass())) {
					if (metaData.getRequestingThread() == currentThread)
					res.add((T) command);
					itr.remove();
				}
			}
		}
		return res;
	}
}