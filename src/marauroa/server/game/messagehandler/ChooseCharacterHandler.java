/* $Id: ChooseCharacterHandler.java,v 1.2 2010/05/13 18:36:24 nhnb Exp $ */
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
import marauroa.common.game.RPObject;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SChooseCharacter;
import marauroa.common.net.message.MessageS2CChooseCharacterACK;
import marauroa.common.net.message.MessageS2CChooseCharacterNACK;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;

/**
 * Process the choose character message from client.
 * This message is the one that move the player from
 * login stage to game stage.
 */
class ChooseCharacterHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);

	/**
	 * This methods handles the logic when a Choose Character message is
	 * received from client, checking the message and choosing the character.
	 *
	 * This method will send also the reply ACK or NACK to the message.
	 *
	 * @param msg
	 *            The ChooseCharacter message
	 */
	@Override
	public void process(Message message) {
		MessageC2SChooseCharacter msg = (MessageC2SChooseCharacter) message;
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			/*
			 * verify event so that we can trust that it comes from our player
			 * and that it has completed the login stage.
			 */
			if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE)) {
				return;
			}

			/* We check if this account has such player. */
			if (entry.hasCharacter(msg.getCharacter())) {
				/* We set the character in the entry info */
				entry.character = msg.getCharacter();

				/* We restore back the character to the world */
				playerContainer.getLock().requestWriteLock();
				RPObject object = entry.loadRPObject();

				if (object != null) {
					/*
					 * We set the clientid attribute to link easily the object with
					 * is player runtime information
					 */
					object.put("#clientid", clientid);
				} else {
					logger.warn("could not load object for character("+entry.character+") from database");
				}

				/* We ask RP Manager to initialize the object */
				if(rpMan.onInit(object)) {
					/* Correct: Character exist */
					MessageS2CChooseCharacterACK msgChooseCharacterACK = new MessageS2CChooseCharacterACK(
							msg.getSocketChannel());
					msgChooseCharacterACK.setClientID(clientid);
					netMan.sendMessage(msgChooseCharacterACK);

					/* And finally sets this connection state to GAME_BEGIN */
					entry.state = ClientState.GAME_BEGIN;
					playerContainer.getLock().releaseLock();
					return;
				} else {
					/* This account doesn't own that character */
					logger.warn("RuleProcessor rejected character(" + msg.getCharacter()+")");
				}
				playerContainer.getLock().releaseLock();
			} else {
				/* This account doesn't own that character */
				logger.warn("Client(" + msg.getAddress().toString() + ") hasn't character("
						+ msg.getCharacter() + ")");
			}

			/*
			 * If the account doesn't own the character OR if the rule processor rejected it.
			 * So we return it back to login complete stage.
			 */
			entry.state = ClientState.LOGIN_COMPLETE;

			/* Error: There is no such character */
			MessageS2CChooseCharacterNACK msgChooseCharacterNACK = new MessageS2CChooseCharacterNACK(
					msg.getSocketChannel());

			msgChooseCharacterNACK.setClientID(clientid);
			netMan.sendMessage(msgChooseCharacterNACK);

		} catch (Exception e) {
			logger.error("error when processing character event", e);
		}
	}

}
