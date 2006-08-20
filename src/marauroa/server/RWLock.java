/* $Id: RWLock.java,v 1.4 2006/08/20 15:40:09 wikipedian Exp $ */
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

import org.apache.log4j.Logger;

/**
 * This class is a Reader/Writters lock A Reader Writer Lock is a
 * synchronization mechanism allowing access to data. It allows multiple threads
 * to read the data simultaneously, but only one thread at a time to update it.
 * While a thread is updating, no other thread can read the data. The name is
 * misleading. It may cause you to think there are two locks; in reality there
 * is a single lock that restricts both reading and writing.
 */
public class RWLock {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(RWLock.class);

	private volatile int givenLocks;

	private volatile int waitingWriters;

	private Object mutex;

	public RWLock() {
		mutex = new Object();
		givenLocks = 0;
		waitingWriters = 0;
	}

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
