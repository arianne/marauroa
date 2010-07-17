/* $Id: CreateCharacterHandler.java,v 1.6 2010/07/17 23:43:27 nhnb Exp $ */
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
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CCreateCharacterNACK;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.dbcommand.LoadAllActiveCharactersCommand;

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
	 * @param message
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
			 * will also return a result of the character that we must forward to
			 * player.
			 */
			CharacterResult val = rpMan.createCharacter(entry.username, msg.getCharacter(), 
					msg.getTemplate(), msg.getAddress().getHostAddress());
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
				msgCreateCharacterACK.setProtocolVersion(msg.getProtocolVersion());
				netMan.sendMessage(msgCreateCharacterACK);

				/*
				 * Build player character list and send it to client
				 */
				DBCommand command = new LoadAllActiveCharactersCommand(entry.username,
						new SendCharacterListHandler(netMan, msg.getProtocolVersion()), 
						clientid, msg.getSocketChannel(), msg.getProtocolVersion());
				DBCommandQueue.get().enqueue(command);
			} else {
				/*
				 * It also may fail to create the character. Explain the reasons
				 * to player.
				 */
				MessageS2CCreateCharacterNACK msgCreateCharacterNACK = new MessageS2CCreateCharacterNACK(
				        msg.getSocketChannel(), result);
				msgCreateCharacterNACK.setClientID(clientid);
				msgCreateCharacterNACK.setProtocolVersion(msg.getProtocolVersion());
				netMan.sendMessage(msgCreateCharacterNACK);
			}
		} catch (Exception e) {
			logger.error("Unable to create a character", e);
		}
	}
}
