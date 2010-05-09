/* $Id: CreateCharacterHandler.java,v 1.1 2010/05/09 19:42:51 nhnb Exp $ */
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
import marauroa.common.game.CharacterResult;
import marauroa.common.game.Result;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SCreateCharacter;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CCreateCharacterNACK;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;

/**
 * This is a create character request. It require that
 * client has correctly logged to server. Once client
 * create character a new Choose Character message is
 * sent.
 */
class CreateCharacterHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);

	/**
	 * This message is used to create a character in a game account. It may fail
	 * if the player already exists or if any of the fields are empty.
	 *
	 * @param msg
	 *            The create account message.
	 */
	@Override
	public void process(Message message) {
		MessageC2SCreateCharacter msg = (MessageC2SCreateCharacter) message;
		try {
			int clientid = msg.getClientID();
			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE)) {
				logger.warn("invalid create character event (client unknown, not logged in or wrong ip-address)");
				return;
			}

			/*
			 * We request the creation of an account for a logged player. It
			 * will also return a result of the character that we must follow to
			 * player.
			 */
			CharacterResult val = rpMan.createCharacter(entry.username, msg.getCharacter(), msg
			        .getTemplate());
			Result result = val.getResult();

			if (result == Result.OK_CREATED) {
				/*
				 * If the character is created notify player and send him a
				 * Character list message.
				 */
				logger.debug("Character (" + msg.getCharacter() + ") created for account "
				        + entry.username);
				MessageS2CCreateCharacterACK msgCreateCharacterACK = new MessageS2CCreateCharacterACK(
				        msg.getSocketChannel(), val.getCharacter(), val.getTemplate());
				msgCreateCharacterACK.setClientID(clientid);
				netMan.sendMessage(msgCreateCharacterACK);

				/*
				 * Build player character list and send it to client
				 */
				String[] characters = entry.getCharacters().toArray(new String[entry.getCharacters().size()]);
				MessageS2CCharacterList msgCharacters = new MessageS2CCharacterList(msg
				        .getSocketChannel(), characters);
				msgCharacters.setClientID(clientid);
				netMan.sendMessage(msgCharacters);
			} else {
				/*
				 * It also may fail to create the character. Explain the reasons
				 * to player.
				 */
				MessageS2CCreateCharacterNACK msgCreateCharacterNACK = new MessageS2CCreateCharacterNACK(
				        msg.getSocketChannel(), result);
				netMan.sendMessage(msgCreateCharacterNACK);
			}
		} catch (Exception e) {
			logger.error("Unable to create a character", e);
		}
	}
}
