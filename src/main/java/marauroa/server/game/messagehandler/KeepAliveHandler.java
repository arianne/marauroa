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
import marauroa.common.net.message.MessageC2SKeepAlive;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;

/**
 * Recieve keep alive messages from client.
 */
class KeepAliveHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(KeepAliveHandler.class);

	/**
	 * This message is send from client to confirm that he is still alive and has not timeout. 
	 *
	 * @param message
	 *            the keep alive message
	 */
	@Override
	public void process(Message message) {
		MessageC2SKeepAlive alive = (MessageC2SKeepAlive) message;
		try {
			int clientid = alive.getClientID();
			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(alive, entry, ClientState.GAME_BEGIN, ClientState.LOGIN_COMPLETE)) {
				return;
			}

			entry.update();
		} catch (Exception e) {
			logger.error("error while processing Keep Alive event", e);
		}
	}

}
