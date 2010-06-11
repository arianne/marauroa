/* $Id: MessageHandler.java,v 1.2 2010/06/11 21:08:36 nhnb Exp $ */
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

import marauroa.common.Log4J;
import marauroa.common.crypto.RSAKey;
import marauroa.common.net.message.Message;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.Statistics;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.net.INetworkServerManager;

/**
 * an interface to handle messages
 *
 * @author hendrik
 */
abstract class MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);

	/** We need network server manager to be able to send messages */
	protected INetworkServerManager netMan;

	/** We need rp manager to run the messages and actions from players */
	protected RPServerManager rpMan;

	/** The playerContainer handles all the player management */
	protected PlayerEntryContainer playerContainer;

	/** Statistics about actions runs */
	protected Statistics stats;

	/** The server RSA Key */
	protected RSAKey key;

	/**
	 * This checks if the message is valid to trigger the event. The player has
	 * to:
	 * <ul>
	 * <li>Be known to the Server (logged in)</li>
	 * <li>Completed the login procedure</li>
	 * <li>Must have the correct IP<->clientid relation </li>
	 * </ul>
	 *
	 * @param msg the message to check
	 * @param entry PlayerEntry
	 * @param states valid client states
	 * @return true, the event is valid, else false
	 */
	protected boolean isValidEvent(Message msg, PlayerEntry entry, ClientState... states) {
		if (entry == null) {
			/*
			 * Error: Player didn't login.
			 */
			logger.warn("Client(" + msg.getAddress() + ") has not login yet");
			return false;
		}

		/*
		 * Now we check if client is in any of the valid states
		 */
		boolean isInCorrectState = false;
		for (ClientState state : states) {
			if (entry.state == state) {
				isInCorrectState = true;
			}
		}

		/*
		 * And it it is not in the correct state, return false.
		 */
		if (!isInCorrectState) {
			StringBuffer statesString = new StringBuffer();
			for (ClientState state : states) {
				statesString.append(state + " ");
			}

			logger.warn("Client(" + msg.getAddress() + ") is not in the required state ("
			        + statesString.toString() + ")");
			return false;
		}

		/*
		 * Finally we check if another player is trying to inject messages into
		 * a different player avatar.
		 */
		if (entry.channel != msg.getSocketChannel()) {
			/*
			 * Info: Player is using a different socket to communicate with
			 * server.
			 */
			logger.warn("Client(" + msg.getAddress() + ") has not correct IP<->clientid relation");
			return false;
		}

		/*
		 * If nothing of the above happens, it means this event is valid and we
		 * keep processing it.
		 */
		return true;
	}

	/**
	 * process the message
	 *
	 * @param message Message
	 */
	public abstract void process(Message message);


	/**
	 * Initializes the MessageHandler
	 *
	 * @param netMan INetworkServerManager
	 * @param rpMan RPServerManager
	 * @param playerContainer PlayerEntryContainer
	 * @param stats Statistics
	 * @param key RSAKey
	 */
	public void init(INetworkServerManager netMan, RPServerManager rpMan,
			PlayerEntryContainer playerContainer, Statistics stats, RSAKey key) {
		this.netMan = netMan;
		this.rpMan = rpMan;
		this.playerContainer = playerContainer;
		this.stats = stats;
		this.key = key;
	}


}
