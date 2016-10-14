/* $Id: INetworkServerManager.java,v 1.12 2007/12/04 20:00:10 martinfuchs Exp $ */
/***************************************************************************
 *                   (C) Copyright 2010-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.web;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.net.Channel;
import marauroa.common.net.ConnectionManager;
import marauroa.common.net.MessageFactory;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.server.marauroad;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.dbcommand.LoadAllActiveCharactersCommand;
import marauroa.server.game.messagehandler.SendCharacterListHandler;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.IServerManager;

import org.json.simple.JSONValue;

/**
 * a network manager implementation that uses a websocket server for web based clients.
 *
 * @author hendrik
 */
public class WebSocketConnectionManager implements ConnectionManager {

	private static Logger logger = Log4J.getLogger(WebSocketConnectionManager.class);

	private final IServerManager serverManager;
	private final Set<WebSocketChannel> channels = Collections.synchronizedSet(new HashSet<WebSocketChannel>());
	private static WebSocketConnectionManager instance;

	synchronized static WebSocketConnectionManager get() {
		if (instance == null) {
			instance = new WebSocketConnectionManager((IServerManager)  marauroad.getMarauroa().getNetMan());
		}
		return instance;
	}

	/**
	 * creates a new WebSocketServerManager
	 *
	 * @param netMan classic network server manager.
	 */
	WebSocketConnectionManager(IServerManager netMan) {
		this.serverManager = netMan;
	}

	/**
	 * a client connected
	 *
	 * @param webSocketChannel channel to the client
	 */
	void onConnect(WebSocketChannel webSocketChannel) {
		channels.add(webSocketChannel);
		Channel channel = serverManager.onConnect(this, webSocketChannel.getAddress(), webSocketChannel);
		PlayerEntry entry = PlayerEntryContainer.getContainer().add(channel);
		if (webSocketChannel.getUsername() != null) {
			entry.state = ClientState.LOGIN_COMPLETE;
			entry.username = webSocketChannel.getUsername();
			entry.disableTimeout();

			// greet the client with a character list (which allows the client to learn its clientid)
			DBCommand command = new LoadAllActiveCharactersCommand(entry.username,
					new SendCharacterListHandler((INetworkServerManager) serverManager, 0),
					entry.clientid, channel, 0);
			DBCommandQueue.get().enqueue(command);
		} else {
			entry.state = ClientState.CONNECTION_ACCEPTED;
			entry.disableTimeout();
			Message msg = new MessageC2SLoginRequestKey(channel, true);
			msg.setClientID(entry.clientid);
			serverManager.onMessage(this, webSocketChannel, msg);
		}

	}

	/**
	 * a client disconnected
	 *
	 * @param webSocketChannel channel to the client
	 */
	public void onDisconnect(WebSocketChannel webSocketChannel) {
		channels.remove(webSocketChannel);
		serverManager.onDisconnect(this, webSocketChannel);
	}

	/**
	 * a client sent a message
	 *
	 * @param webSocketChannel channel to the client
	 * @param message message
	 */
	@SuppressWarnings("unchecked")
	public void onMessage(WebSocketChannel webSocketChannel, String message) {
		logger.debug(" message: " + message);


		Map<String, Object> map = (Map<String, Object>) (JSONValue.parse(message));
		try {
			Message msg = MessageFactory.getFactory().getMessage(map);
			serverManager.onMessage(this, webSocketChannel, msg);
		} catch (IOException e) {
			logger.error(map);
		} catch (RuntimeException e) {
			logger.error(map);
		}
	}

	public void finish() {
		// empty
	}

	public boolean isFinished() {
		return true;
	}

	public void send(Object internalChannel, Message msg, boolean isPerceptionRequired) {
		if (!isPerceptionRequired && msg.isSkippable()) {
			return;
		}
		StringBuilder out = new StringBuilder();
		out.append("{");
		msg.writeToJson(out);
		out.append("}");
		if (out.length() > 2) {
			((WebSocketChannel) internalChannel).sendMessage(out.toString());
		}
	}

	public void close(Object internalChannel) {
		((WebSocketChannel) internalChannel).close();
	}

}
