/***************************************************************************
 *                   (C) Copyright 2003-2023 - Marauroa                    *
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

import java.util.concurrent.locks.ReentrantReadWriteLock;

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

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * Request a reader lock.<br>
	 * Readers can obtain a lock as long as there is no writer.
	 */
	public void requestReadLock() {
		// for compatibility we cannot use readLocks because old code calls 
		// releaseLock without specifying whether it was a read or write lock
		this.lock.writeLock().lock();
	}

	/**
	 * Request a Writers lock. A writer can obtain the lock as long as there are
	 * no more writers nor readers using the lock.
	 */
	public void requestWriteLock() {
		this.lock.writeLock().lock();
	}

	/**
	 * This releases the lock for both readers and writers.
	 */
	public void releaseLock() {
		this.lock.writeLock().unlock();
	}
}
