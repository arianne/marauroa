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

import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SLogout;
import marauroa.common.net.message.MessageS2CLogoutACK;
import marauroa.common.net.message.MessageS2CLogoutNACK;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.rp.DebugInterface;

/**
 * Request server to exit and free resources associated.
 * It may fail if RP decides not to allow player logout.
 */
class LogoutHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(LogoutHandler.class);

	/**
	 * This method is called when server receives a logout message from a
	 * player. It handles all the logic to effectively logout the player and
	 * free the associated resources.
	 *
	 * @param message
	 *            the logout message
	 */
	@Override
	public void process(Message message) {
		MessageC2SLogout msg = (MessageC2SLogout) message;
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			/*
			 * Verify event so that we can trust that it comes from our player
			 * and that it has completed the login stage.
			 */
			if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE, ClientState.GAME_BEGIN)) {
				return;
			}

			RPObject object = entry.object;

			boolean shouldLogout = true;

			/*
			 * We request to logout of game to RP Manager If may be successful or
			 * fail and we keep on game.
			 */
			if (entry.state == ClientState.GAME_BEGIN) {
				playerContainer.getLock().requestWriteLock();

				try {

					int reason = msg.getReason();
					if (reason > 0) {
						DebugInterface.get().onCrash(object);
						return; // we treat crashes as timeouts for now
					}

					if (rpMan.onExit(object)) {
						/* NOTE: Set the Object so that it is stored in Database */
						entry.storeRPObject(object);
					} else {
						/*
						 * If RPManager returned false, that means that logout is
						 * not allowed right now, so player request is rejected.
						 * This can be useful to disallow logout on some situations.
						 */
						shouldLogout = false;
					}
				} finally {
					playerContainer.getLock().releaseLock();
				}
			}

			if (shouldLogout) {
				stats.add("Players logout", 1);
				logger.info("Logging out correctly channel: " + entry.getInetSocketAddress());
				playerContainer.remove(clientid);

				/* Send Logout ACK message */
				MessageS2CLogoutACK msgLogout = new MessageS2CLogoutACK(msg.getChannel());

				msgLogout.setClientID(clientid);
				msgLogout.setProtocolVersion(msg.getProtocolVersion());
				netMan.sendMessage(msgLogout);

				entry.state = ClientState.LOGOUT_ACCEPTED;
			} else {
				MessageS2CLogoutNACK msgLogout = new MessageS2CLogoutNACK(msg.getChannel());
				msgLogout.setClientID(clientid);
				msgLogout.setProtocolVersion(msg.getProtocolVersion());
				netMan.sendMessage(msgLogout);
			}
		} catch (Exception e) {
			logger.error("error while processing LogoutEvent", e);
		}
	}

}
