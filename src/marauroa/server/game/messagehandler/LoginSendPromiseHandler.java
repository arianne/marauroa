/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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

import java.io.IOException;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SLoginSendPromise;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendNonce;
import marauroa.server.game.container.PlayerEntry;

/**
 * Receive the client hash promise of the password. Now
 * client has a PlayerEntry container.
 */
class LoginSendPromiseHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(LoginSendPromiseHandler.class);
	private int maxNumberOfPlayers = 128;

	public LoginSendPromiseHandler() {
		try {
			String temp = Configuration.getConfiguration().get("max_number_of_players");
			if (temp != null) {
				maxNumberOfPlayers = Integer.parseInt(temp.trim());
			}
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	/**
	 * This method is part of the crypto process to login the player. It creates
	 * a place for the player on the server, so we should handle some timeout
	 * here to avoid DOS attacks.
	 *
	 * @param msg
	 *            the promise message.
	 */
	@Override
	public void process(Message msg) {
		try {
			/*
			 * If there is no more room we try to get an idle player that we can
			 * disconnect We can define an idle player entry like a player that
			 * has connected to server and requested to login, but never
			 * actually completed login stage. This client is taking server
			 * resources but doing nothing useful at all.
			 */
			if (playerContainer.size() >= maxNumberOfPlayers) {
				logger.info("Server is full, making room now");
				/* Let's try to make some room for more players. */
				PlayerEntry candidate = playerContainer.getIdleEntry();

				if (candidate != null) {
					/*
					 * Cool!, we got a candidate, so we remove and disconnect
					 * it.
					 */
					netMan.disconnectClient(candidate.channel);

					/*
					 * HACK: Remove the entry now so we can continue.
					 */				
					playerContainer.remove(candidate.clientid);
				}
			}

			/*
			 * We give a new try to see if we can create a entry for this
			 * player.
			 */
			if (playerContainer.size() >= maxNumberOfPlayers) {
				/* Error: Too many clients logged on the server. */
				logger.warn("Server is full, Client(" + msg.getAddress().toString()
				        + ") can't login. You may want to increase max_number_of_players in your server.init. Current value is: " + maxNumberOfPlayers);

				/* Notify player of the event. */
				MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(),
				        MessageS2CLoginNACK.Reasons.SERVER_IS_FULL);
				msgLoginNACK.setProtocolVersion(msg.getProtocolVersion());
				netMan.sendMessage(msgLoginNACK);
				return;
			}

			MessageC2SLoginSendPromise msgLoginSendPromise = (MessageC2SLoginSendPromise) msg;

			PlayerEntry entry = playerContainer.add(msgLoginSendPromise.getSocketChannel());
			entry.setProtocolVersion(msg.getProtocolVersion());

			byte[] serverNonce = Hash.random(Hash.hashLength());
			byte[] clientNonceHash = msgLoginSendPromise.getHash();

			entry.loginInformations = new PlayerEntry.SecuredLoginInfo(key, clientNonceHash,
			        serverNonce, msgLoginSendPromise.getAddress());

			MessageS2CLoginSendNonce msgLoginSendNonce = new MessageS2CLoginSendNonce(msg
			        .getSocketChannel(), serverNonce);
			msgLoginSendNonce.setClientID(entry.clientid);
			msgLoginSendNonce.setProtocolVersion(msg.getProtocolVersion());
			netMan.sendMessage(msgLoginSendNonce);
		} catch (Exception e) {
			logger.error("client not found", e);
		}
	}


}
