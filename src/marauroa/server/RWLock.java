/* $Id: RWLock.java,v 1.8 2007/04/09 14:39:58 arianne_rpg Exp $ */
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
package marauroa.server;

import marauroa.common.Log4J;

/**
 * This class is a Reader/Writters lock A Reader Writer Lock is a
 * synchronization mechanism allowing access to data.<br>
 * It allows multiple threads to read the data simultaneously, but only one
 * thread at a time to update it.
 * <p>
 * Controlling concurrent access to a shared, mutable resource is a classic
 * problem. Clients request, and later release, either read-only or read-write
 * access to the resource. To preserve consistency but minimize waiting, one
 * usually wishes to allow either any number of readers or a single writer, but
 * not both, to have access at any one time.
 * <p>
 * While a thread is updating, no other thread can read the data. The name is
 * misleading. It may cause you to think there are two locks; in reality there
 * is a single lock that restricts both reading and writing.
 */
public class RWLock {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(RWLock.class);

	private volatile int givenLocks;

	private volatile int waitingWriters;

	private Object mutex;

	/**
	 * Constructor
	 */
	public RWLock() {
		mutex = new Object();
		givenLocks = 0;
		waitingWriters = 0;
	}

	/**
	 * Request a reader lock.<br>
	 * Readers can obtain a lock as long as there is no writer.
	 */
	public void requestReadLock() {
		synchronized (mutex) {
			try {
				while ((givenLocks == -1) || (waitingWriters != 0)) {
					mutex.wait(100);
				}
			} catch (InterruptedException ie) {
				logger.debug("interrupted while requesting a read lock", ie);
			}
			givenLocks++;
		}
	}

	/**
	 * Request a Writers lock. A writer can obtain the lock as long as there are
	 * no more writers nor readers using the lock.
	 */
	public void requestWriteLock() {
		synchronized (mutex) {
			waitingWriters++;
			try {
				while (givenLocks != 0) {
					mutex.wait(100);
				}
			} catch (InterruptedException ie) {
				logger.debug("interrupted while requesting a write lock", ie);
			}
			waitingWriters--;
			givenLocks = -1;
		}
	}

	/**
	 * This releases the lock for both readers and writers.
	 */
	public void releaseLock() {
		synchronized (mutex) {
			if (givenLocks == 0) {
				return;
			}
			if (givenLocks == -1) {
				givenLocks = 0;
			} else {
				givenLocks--;
			}
			mutex.notifyAll();
		}
	}
}
