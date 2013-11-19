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

import static marauroa.common.net.message.Message.MessageType.C2S_ACTION;
import static marauroa.common.net.message.Message.MessageType.C2S_CHOOSECHARACTER;
import static marauroa.common.net.message.Message.MessageType.C2S_CREATEACCOUNT;
import static marauroa.common.net.message.Message.MessageType.C2S_CREATECHARACTER;
import static marauroa.common.net.message.Message.MessageType.C2S_KEEPALIVE;
import static marauroa.common.net.message.Message.MessageType.C2S_LOGIN_REQUESTKEY;
import static marauroa.common.net.message.Message.MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD;
import static marauroa.common.net.message.Message.MessageType.C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED;
import static marauroa.common.net.message.Message.MessageType.C2S_LOGIN_SENDPROMISE;
import static marauroa.common.net.message.Message.MessageType.C2S_LOGIN_SENDUSERNAMEANDPASSWORD;
import static marauroa.common.net.message.Message.MessageType.C2S_LOGOUT;
import static marauroa.common.net.message.Message.MessageType.C2S_OUTOFSYNC;
import static marauroa.common.net.message.Message.MessageType.C2S_TRANSFER_ACK;
import static marauroa.common.net.message.Message.MessageType.P2S_CREATEACCOUNT;
import static marauroa.common.net.message.Message.MessageType.P2S_CREATECHARACTER;

import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.crypto.RSAKey;
import marauroa.common.i18n.I18N;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.Message.MessageType;
import marauroa.server.game.Statistics;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.net.INetworkServerManager;

/**
 * Dispatches messages to the appropriate handlers
 *
 * @author hendrik
 */
public class MessageDispatcher {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(MessageDispatcher.class);

	private final Map<MessageType, MessageHandler> handlers = new HashMap<MessageType, MessageHandler>();

	/**
	 * init the handlers map
	 */
	private void initMap() {
		handlers.put(C2S_LOGIN_REQUESTKEY, new LoginRequestKeyHandler());
		handlers.put(C2S_LOGIN_SENDPROMISE, new LoginSendPromiseHandler());
		handlers.put(C2S_LOGIN_SENDNONCENAMEANDPASSWORD, new SecuredLoginHandler());
		handlers.put(C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED, new SecuredLoginHandler());
		handlers.put(C2S_LOGIN_SENDUSERNAMEANDPASSWORD, new SecuredLoginHandler());
		handlers.put(C2S_CHOOSECHARACTER, new ChooseCharacterHandler());
		handlers.put(C2S_LOGOUT, new LogoutHandler());
		handlers.put(C2S_ACTION, new ActionHandler());
		handlers.put(C2S_OUTOFSYNC, new OutOfSyncHandler());
		handlers.put(C2S_KEEPALIVE, new KeepAliveHandler());
		handlers.put(C2S_TRANSFER_ACK, new TransferACKHandler());
		handlers.put(C2S_CREATEACCOUNT, new CreateAccountHandler());
		handlers.put(C2S_CREATECHARACTER, new CreateCharacterHandler());

		handlers.put(P2S_CREATEACCOUNT, new CreateAccountHandler());
		handlers.put(P2S_CREATECHARACTER, new CreateCharacterHandler());
	}

	/**
	 * creates a new MessageDispatcher
	 */
	public MessageDispatcher() {
		initMap();
	}

	/**
	 * Initializes the MessageHandlers
	 *
	 * @param netMan INetworkServerManager
	 * @param rpMan RPServerManager
	 * @param playerContainer PlayerEntryContainer
	 * @param stats Statistics
	 * @param key RSAKey
	 */
	public void init(INetworkServerManager netMan, RPServerManager rpMan,
			PlayerEntryContainer playerContainer, Statistics stats, RSAKey key) {
		for (MessageHandler handler: handlers.values()) {
			handler.init(netMan, rpMan, playerContainer, stats, key);
		}
	}

	/**
	 * dispatches the message to the appropriate handler
	 * @param msg
	 */
	public void dispatchMessage(Message msg) {
		logger.debug("Processing " + msg.getType());
		MessageHandler handler = findMessageHandler(msg);
		setThreadLanguage(msg);

		try {

			handler.process(msg);

		} finally {
			I18N.resetThreadLocale();
		}
	}

	/**
	 * finds the handler for this message type
	 *
	 * @param msg Message
	 * @return MessageHandler
	 */
	private MessageHandler findMessageHandler(Message msg) {
		MessageHandler handler = handlers.get(msg.getType());
		if (handler == null) {
			handler = new UnkownMessageHandler();
		}
		return handler;
	}

	/**
	 * sets the thread language for this client
	 *
	 * @param msg Message
	 */
	private void setThreadLanguage(Message msg) {
		int clientid = msg.getClientID();
		PlayerEntry entry = PlayerEntryContainer.getContainer().get(clientid);
		if (entry != null) {
			I18N.setThreadLocale(entry.locale);
		}
	}
}
