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
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SOutOfSync;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;

/**
 * When client get out of sync because it has lost part
 * of the messages stream, it can request a
 * synchronization to continue game.
 */
//TODO: Consider removing this message as with TCP it won't fail.
class OutOfSyncHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(OutOfSyncHandler.class);

	/**
	 * This message is send from client to notify that client suffered a network
	 * problem and request data to be resend again to it.
	 *
	 * @param message
	 *            the out of sync message
	 */
	@Override
	public void process(Message message) {
		MessageC2SOutOfSync msg = (MessageC2SOutOfSync) message;
		try {
			int clientid = msg.getClientID();
			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			/*
			 * Notify Player Entry that this player is out of Sync
			 */
			entry.requestSync();
		} catch (Exception e) {
			logger.error("error while processing OutOfSyncEvent", e);
		}
	}

}
