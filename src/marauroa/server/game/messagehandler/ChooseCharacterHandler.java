/* $Id: ChooseCharacterHandler.java,v 1.9 2010/07/18 13:46:27 nhnb Exp $ */
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

import java.nio.channels.SocketChannel;

import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SChooseCharacter;
import marauroa.common.net.message.MessageS2CChooseCharacterACK;
import marauroa.common.net.message.MessageS2CChooseCharacterNACK;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.dbcommand.LoadActiveCharacterCommand;
import marauroa.server.game.dbcommand.LoadCharacterCommand;
import marauroa.server.game.rp.RPServerManager;

/**
 * Process the choose character message from client.
 * This message is the one that move the player from
 * login stage to game stage.
 */
class ChooseCharacterHandler extends MessageHandler implements DelayedEventHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);

	/**
	 * This methods handles the logic when a Choose Character message is
	 * received from client, checking the message and choosing the character.
	 *
	 * This method will send also the reply ACK or NACK to the message.
	 *
	 * @param message
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

			/* We set the character in the entry info */
			entry.character = msg.getCharacter();

			PlayerEntry oldEntry = playerContainer.getOldEntry(entry);
			if ((oldEntry != null) && (oldEntry.state == ClientState.GAME_BEGIN)) {
				reownOldEntry(oldEntry, entry);
				return;
			}

			loadAndPlaceInWorld(msg, clientid, entry);

		} catch (Exception e) {
			logger.error("error when processing character event", e);
		}
	}

	private void reownOldEntry(PlayerEntry oldEntry, PlayerEntry entry) {
		// remove character from world
		playerContainer.getLock().requestWriteLock();
		RPObject object = oldEntry.object; // (RPObject) oldEntry.object.clone();
		rpMan.onTimeout(oldEntry.object);

		// Disconnect player of server.
		logger.debug("Disconnecting PREVIOUS " + oldEntry.channel + " with " + oldEntry);
		netMan.disconnectClient(oldEntry.channel);
		playerContainer.remove(oldEntry.clientid);

		// add player object again (create a new instance of the object to clear the internal state)
		object = DAORegister.get().getRPObjectFactory().transform(object);
		completeLoadingCharacterIntoWorld(rpMan, entry.clientid, entry.channel, entry, object);
		playerContainer.getLock().releaseLock();
	}

	/**
	 * Asynchronously loads a player from the database and later places it into the world
	 *
	 * @param msg MessageC2SChooseCharacter
	 * @param clientid clientid
	 * @param entry PlayerEntry
	 */
	private void loadAndPlaceInWorld(MessageC2SChooseCharacter msg, int clientid, PlayerEntry entry) {
		DBCommand command = new LoadActiveCharacterCommand(entry.username, entry.character, this, clientid, msg.getSocketChannel(), msg.getProtocolVersion());
		DBCommandQueue.get().enqueue(command);
	}

	/**
	 * After validating that both the player entry and and the loaded player object are valid,
	 * places the player into the world.
	 *
	 * @param rpMan RPServerManager
	 * @param data LoadCharacterCommand
	 */
	public void handleDelayedEvent(RPServerManager rpMan, Object data) {
		LoadCharacterCommand cmd = (LoadCharacterCommand) data;
		RPObject object = cmd.getObject();
		int clientid = cmd.getClientid();

		PlayerEntry entry = playerContainer.get(clientid);
		if (entry == null) {
			return;
		}

		// does the character exist and belong to the user?
		if (object == null) {
			logger.warn("could not load object for character " + entry.character + " for user " + cmd.getUsername() + " from database");
			rejectClient(cmd.getChannel(), clientid, entry);
			return;
		}

		/* We restore back the character to the world */
		playerContainer.getLock().requestWriteLock();
		completeLoadingCharacterIntoWorld(rpMan, clientid, cmd.getChannel(), entry, object);
		playerContainer.getLock().releaseLock();
	}

	private void completeLoadingCharacterIntoWorld(RPServerManager rpMan,
			int clientid, SocketChannel channel, PlayerEntry entry, RPObject object) {

		if (object != null) {
			object.put("#clientid", clientid);
		}

		entry.setObject(object);

		/* We ask RP Manager to initialize the object */
		if(rpMan.onInit(object)) {
			/* Correct: Character exist */
			MessageS2CChooseCharacterACK msgChooseCharacterACK = new MessageS2CChooseCharacterACK(channel);
			msgChooseCharacterACK.setClientID(clientid);
			msgChooseCharacterACK.setProtocolVersion(entry.getProtocolVersion());
			netMan.sendMessage(msgChooseCharacterACK);

			/* And finally sets this connection state to GAME_BEGIN */
			entry.state = ClientState.GAME_BEGIN;
		} else {
			logger.warn("RuleProcessor rejected character(" + entry.character + ")");
			rejectClient(channel, clientid, entry);
		}
	}


	/**
	 * If the account doesn't own the character OR if the rule processor rejected it.
	 * So we return it back to login complete stage.
	 *
	 * @param channel SocketChannel
	 * @param clientid clientid
	 * @param entry PlayerEntry
	 */
	private void rejectClient(SocketChannel channel, int clientid, PlayerEntry entry) {
		entry.state = ClientState.LOGIN_COMPLETE;

		/* Error: There is no such character */
		MessageS2CChooseCharacterNACK msgChooseCharacterNACK = 
			new MessageS2CChooseCharacterNACK(channel);

		msgChooseCharacterNACK.setClientID(clientid);
		msgChooseCharacterNACK.setProtocolVersion(entry.getProtocolVersion());
		netMan.sendMessage(msgChooseCharacterNACK);
	}

}
