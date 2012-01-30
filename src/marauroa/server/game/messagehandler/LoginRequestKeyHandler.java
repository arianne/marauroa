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
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendKey;

/**
 * Process the C2S Login request key message.
 */
class LoginRequestKeyHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(LoginRequestKeyHandler.class);

	/**
	 * This method handles the initial login of the client into the server.
	 * First, it compares the game and version that client is running and if
	 * they match request the client to send the key to continue login process.
	 *
	 * Otherwise, it rejects client because game is incompatible.
	 *
	 * @param msg
	 *            the login message.
	 */
	@Override
	public void process(Message msg) {
		MessageC2SLoginRequestKey msgRequest = (MessageC2SLoginRequestKey) msg;

		/*
		 * Check game version with data suplied by client. The RP may decide to
		 * deny login to this player.
		 */
		if (rpMan.checkGameVersion(msgRequest.getGame(), msgRequest.getVersion())) {
			/*
			 * If this is correct we send player the server key so it can sign
			 * the password.
			 */
			MessageS2CLoginSendKey msgLoginSendKey = new MessageS2CLoginSendKey(msg
			        .getSocketChannel(), key);
			msgLoginSendKey.setClientID(Message.CLIENTID_INVALID);
			msgLoginSendKey.setProtocolVersion(msg.getProtocolVersion());
			netMan.sendMessage(msgLoginSendKey);
		} else {
			/* Error: Incompatible game version. Update client */
			logger.debug("Client is running an incompatible game version. Client("
			        + msg.getAddress().toString() + ") can't login");

			/* Notify player of the event by denying the login. */
			MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(),
			        MessageS2CLoginNACK.Reasons.GAME_MISMATCH);
			msgLoginNACK.setProtocolVersion(msg.getProtocolVersion());
			netMan.sendMessage(msgLoginNACK);
		}
	}
}
