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

import java.nio.channels.SocketChannel;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SCreateCharacter;
import marauroa.common.net.message.MessageP2SCreateCharacter;
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CCreateCharacterNACK;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
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
	private static final marauroa.common.Logger logger = Log4J.getLogger(CreateCharacterHandler.class);

	/**
	 * This message is used to create a character in a game account. It may fail
	 * if the player already exists or if any of the fields are empty.
	 *
	 * @param message
	 *            The create account message.
	 */
	@Override
	public void process(Message message) {
		try {
			int clientid = message.getClientID();
			int protocolVersion = message.getProtocolVersion();
			SocketChannel channel = message.getSocketChannel();

			if (message instanceof MessageC2SCreateCharacter) {
				MessageC2SCreateCharacter msg = (MessageC2SCreateCharacter) message;

				RPObject template = msg.getTemplate();
				String character = msg.getCharacter();

				PlayerEntry entry = playerContainer.get(clientid);
				String address = msg.getAddress().getHostAddress();

				// verify event
				if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE)) {
					logger.warn("invalid create character event (client unknown, not logged in or wrong ip-address)");
					return;
				}
				int maxNumberOfCharacters = Configuration.getConfiguration().getInt("limit_characters_per_account", Integer.MAX_VALUE);
				if (entry.characterCounter >= maxNumberOfCharacters) {
					Result result = Result.FAILED_TOO_MANY;
					MessageS2CCreateCharacterNACK msgCreateCharacterNACK = new MessageS2CCreateCharacterNACK(channel, result);
					msgCreateCharacterNACK.setClientID(clientid);
					msgCreateCharacterNACK.setProtocolVersion(protocolVersion);
					netMan.sendMessage(msgCreateCharacterNACK);
					return;
				}

				createCharacter(entry.username, character, template, clientid, address, channel,
						protocolVersion, true);

			} else {
				MessageP2SCreateCharacter msg = (MessageP2SCreateCharacter) message;

				RPObject template = msg.getTemplate();
				String character = msg.getCharacter();
				String address = msg.getForwardedFor();

				if ((msg.getCredentials() != null)
						&& (msg.getCredentials().equals(Configuration.getConfiguration().get(
								"proxy_credentials")))) {
					createCharacter(msg.getUsername(), character, template, clientid, address,
							channel, protocolVersion, false);
				} else {
					logger.warn("Invalid credentials for proxy method.");
				}
			}
		} catch (Exception e) {
			logger.error("Unable to create a character", e);
		}
	}

	private void createCharacter(String username, String character, RPObject template,
			int clientid, String address, SocketChannel channel, int protocolVersion,
			boolean sendListOfCharacters) {
		/*
		 * We request the creation of an character for a logged player. It
		 * will also return a result of the character that we must forward to
		 * player.
		 */
		CharacterResult val = rpMan.createCharacter(username, character, template, address);
		Result result = val.getResult();

		if (result == Result.OK_CREATED) {
			/*
			 * If the character is created notify player and send him a
			 * Character list message.
			 */
			logger.debug("Character (" + character + ") created for account " + username);
			MessageS2CCreateCharacterACK msgCreateCharacterACK = new MessageS2CCreateCharacterACK(
					channel, val.getCharacter(), val.getTemplate());
			msgCreateCharacterACK.setClientID(clientid);
			msgCreateCharacterACK.setProtocolVersion(protocolVersion);
			netMan.sendMessage(msgCreateCharacterACK);

			/*
			 * Build player character list and send it to client
			 */
			if (sendListOfCharacters) {
				DBCommand command = new LoadAllActiveCharactersCommand(username,
						new SendCharacterListHandler(netMan, protocolVersion), clientid, channel,
						protocolVersion);
				DBCommandQueue.get().enqueue(command);
			}
		} else {
			/*
			 * It also may fail to create the character. Explain the reasons
			 * to player.
			 */
			MessageS2CCreateCharacterNACK msgCreateCharacterNACK = new MessageS2CCreateCharacterNACK(
					channel, result);
			msgCreateCharacterNACK.setClientID(clientid);
			msgCreateCharacterNACK.setProtocolVersion(protocolVersion);
			netMan.sendMessage(msgCreateCharacterNACK);
		}
	}
}
