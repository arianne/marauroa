/* $Id: AddRemoveCharacterThread.java,v 1.1 2010/05/13 20:53:04 nhnb Exp $ */
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

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Log4J;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.rp.RPServerManager;

/**
 * Thread that disconnect players.
 * It has to be done this way because we can't run it on the main loop of GameServerManager,
 * because it locks waiting for new messages to arrive, so the player keeps unremoved until a 
 * message is recieved.
 * 
 * This way players are removed as they are requested to be.
 * 
 * @author miguel, hendrik
 *
 */
public class AddRemoveCharacterThread extends Thread {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(AddRemoveCharacterThread.class);

	private boolean keepRunning = true;
	private BlockingQueue<SocketChannel> channels;

	/** We need rp manager to run the messages and actions from players */
	private RPServerManager rpMan;

	/** The playerContainer handles all the player management */
	private PlayerEntryContainer playerContainer;


	/**
	 * Constructor.
	 * It just gives a nice name to the thread.
	 */
	public AddRemoveCharacterThread(PlayerEntryContainer playerContainer, RPServerManager rpMan) {
		super("AddRemoveCharacterThread");
		channels = new LinkedBlockingQueue<SocketChannel>();
		this.playerContainer = playerContainer;
		this.rpMan = rpMan;
	}
	
	/**
	 * This method is used mainly by onDisconnect and RPServerManager to force
	 * the disconnection of a player entry.
	 *
	 * @param channel
	 *            the socket channel of the player entry to remove.
	 */
	public void disconnect(SocketChannel channel) {
		try {
			channels.put(channel);
		} catch (InterruptedException e) {
			/*
			 * Not really instereted in.
			 */
		}
	}

	@Override
	public void run() {
		while (keepRunning) {
			SocketChannel channel = null;

			/*
			 * We keep waiting until we are signaled to remove a player.
			 * This way we avoid wasting CPU cycles.
			 */
			try {
				channel = channels.take();
			} catch (InterruptedException e1) {
				/*
				 * Not interested.
				 */
			}

			playerContainer.getLock().requestWriteLock();

			PlayerEntry entry = playerContainer.get(channel);
			if (entry != null) {
				/*
				 * First we remove the entry from the player container.
				 * null means it was already removed by another thread.
				 */
				if (playerContainer.remove(entry.clientid) == null) {
					continue;
				}

				/*
				 * If client is still loging in, don't notify RP as it knows nothing about
				 * this client. That means state != of GAME_BEGIN
				 */
				if (entry.state == ClientState.GAME_BEGIN) {
					/*
					 * If client was playing the game request the RP to disconnected it.
					 */
					try {
						rpMan.onTimeout(entry.object);
						entry.storeRPObject(entry.object);
					} catch (Exception e) {
						logger.error("Error disconnecting player" + entry, e);
					}
				}

				/*
				 * We set the entry to LOGOUT_ACCEPTED state so it can also be freed by
				 * GameServerManager to make room for new players.
				 */
				entry.state = ClientState.LOGOUT_ACCEPTED;
			} else {
				/*
				 * Player may have logout correctly or may have even not started.
				 */
				logger.debug("No player entry for channel: " + channel);
			}

			playerContainer.getLock().releaseLock();
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
