/* $Id: DisconnectHandler.java,v 1.1 2010/05/16 15:24:24 nhnb Exp $ */
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

import marauroa.common.Log4J;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.rp.RPServerManager;

/**
 * disconnects a player and removes the character from the world.
 *
 * @author hendrik
 */
public class DisconnectHandler implements DelayedEventHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(DisconnectHandler.class);

	private PlayerEntryContainer playerContainer = PlayerEntryContainer.getContainer();

	public void handleDelayedEvent(RPServerManager rpMan, Object data) {
		SocketChannel channel = (SocketChannel) data;

		playerContainer.getLock().requestWriteLock();

		PlayerEntry entry = playerContainer.get(channel);
		if (entry != null) {
			/*
			 * First we remove the entry from the player container.
			 * null means it was already removed by another thread.
			 */
			if (playerContainer.remove(entry.clientid) == null) {
				return;
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
