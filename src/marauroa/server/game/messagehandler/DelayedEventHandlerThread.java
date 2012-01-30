/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.messagehandler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Log4J;
import marauroa.common.Pair;
import marauroa.server.game.rp.RPServerManager;

/**
 * Thread that handles delayed events like disconnecting players.
 * It has to be done this way because we can't run it on the main loop of GameServerManager,
 * because it locks waiting for new messages to arrive. This way the events are handles as 
 * they are requested to be.
 * 
 * @author miguel, hendrik
 *
 */
public class DelayedEventHandlerThread extends Thread {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(DelayedEventHandlerThread.class);

	private boolean keepRunning = true;
	private BlockingQueue<Pair<DelayedEventHandler, Object>> queue;

	/** We need rp manager to run the messages and actions from players */
	private RPServerManager rpMan;

	private static DelayedEventHandlerThread instance;

	/**
	 * gets the last instantiated DelayedEventHandlerThread
	 *
	 * @return DelayedEventHandlerThread or <code>null</code> if it has never been instantiated
	 */
	public static DelayedEventHandlerThread get() {
		return instance;
	}

	/**
	 * Creates a new DelayedEventHandlerThread
	 *
	 * @param rpMan RPServerManager
	 */
	public DelayedEventHandlerThread(RPServerManager rpMan) {
		super("DelayedEventHandlerThread");
		queue = new LinkedBlockingQueue<Pair<DelayedEventHandler, Object>>();
		this.rpMan = rpMan;
		set(this);
	}

	/**
	 * sets the instance references
	 *
	 * @param thread DelayedEventHandlerThread
	 */
	private static void set(DelayedEventHandlerThread thread) {
		DelayedEventHandlerThread.instance = thread;
	}

	/**
	 * adds a delayed event for processing.
	 *
	 * @param handler DelayedEventHandler
	 * @param data data for the handler
	 */
	public void addDelayedEvent(DelayedEventHandler handler, Object data) {
		try {
			queue.put(new Pair<DelayedEventHandler, Object>(handler, data));
		} catch (InterruptedException e) {
			logger.error(e, e);
		}
	}

	@Override
	public void run() {
		try {
			while (keepRunning) {
				/*
				 * We keep waiting until we are signaled to process some events.
				 * This way we avoid wasting CPU cycles.
				 */
				Pair<DelayedEventHandler, Object> entry = queue.take();
				entry.first().handleDelayedEvent(rpMan, entry.second());
			}
		} catch (InterruptedException e1) {
			/*
			 * Not interested.
			 */
		}
	}



	/**
	 * Should the thread be kept running?
	 *
	 * @param keepRunning set to false to stop it.
	 */
	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}
}
