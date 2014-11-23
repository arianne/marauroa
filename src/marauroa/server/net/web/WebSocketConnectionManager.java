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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.io.UnicodeSupportingInputStreamReader;
import marauroa.common.net.Channel;
import marauroa.common.net.ConnectionManager;
import marauroa.common.net.MessageFactory;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.dbcommand.LoadAllActiveCharactersCommand;
import marauroa.server.game.messagehandler.SendCharacterListHandler;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.IServerManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import com.glines.socketio.server.SocketIOInbound;
import com.glines.socketio.server.SocketIOServlet;

/**
 * a network manager implementation that uses a socket.io server for web based clients.
 *
 * @author hendrik
 */
public class WebSocketConnectionManager extends SocketIOServlet implements ConnectionManager {

	private static final long serialVersionUID = 4898279536921406401L;
	private static Logger logger = Log4J.getLogger(WebSocketConnectionManager.class);

	private final IServerManager serverManager;
	private final Set<WebSocketChannel> channels = Collections.synchronizedSet(new HashSet<WebSocketChannel>());

	/**
	 * creates a new WebSocketServerManager
	 *
	 * @param netMan classic network server manager.
	 */
	public WebSocketConnectionManager(IServerManager netMan) {
		this.serverManager = netMan;
	}

	@Override
	protected SocketIOInbound doSocketIOConnect(HttpServletRequest request, String[] protocols) {
		try {
			InetSocketAddress address = InetSocketAddress.createUnresolved(request.getRemoteAddr(), request.getRemotePort());
			return new WebSocketChannel(this, address, extractUsernameFromSession(request));
		} catch (UnknownHostException e) {
			logger.error(e, e);
		}
		return null;
	}

	/**
	 * extracts the username from the session, supporting both java and php sessions
	 *
	 * @param request HttpServletRequest
	 * @return username
	 */
	private String extractUsernameFromSession(HttpServletRequest request) {

		// first try java session
		HttpSession session = request.getSession(false);
		if (session != null) {
			String username = (String) request.getSession().getAttribute("marauroa_authenticated_username");
			if (username != null) {
				return username;
			}
		}

		// Jetty returns null instead of an empty list if there is no cookie header.
		if (request.getCookies() == null) {
			return null;
		}

		// try php session
		for (Cookie cookie : request.getCookies()) {
			if (!cookie.getName().equals("PHPSESSID")) {
				continue;
			}

			String sessionid = cookie.getValue();
			if (!sessionid.matches("[A-Za-z0-9]+")) {
				logger.warn("Invalid PHPSESSID=" + sessionid);
				continue;
			}

			BufferedReader br = null;
			try {
				String prefix = Configuration.getConfiguration().get("php_session_file_prefix", "/var/lib/php5/sess_");
				String filename = prefix + sessionid;
				if (new File(filename).canRead()) {
					br = new BufferedReader(new UnicodeSupportingInputStreamReader(new FileInputStream(filename)));
					String line;
					while ((line = br.readLine()) != null) {
						int pos1 = line.indexOf("marauroa_authenticated_username|s:");
						if (pos1 < 0) {
							continue;
						}

						// logger.debug("phpsession-entry: " + line);
						pos1 = line.indexOf("\"", pos1);
						int pos2 = line.indexOf("\"", pos1 + 2);
						if (pos1 > -1 && pos2 > -1) {
							logger.debug("php session username: " + line.substring(pos1 + 1, pos2));
							return line.substring(pos1 + 1, pos2);
						}
					}
				} else {
					logger.warn("Cannot read php session file: " + filename);
				}
			} catch (IOException e) {
				logger.error(e, e);
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						logger.error(e, e);
					}
				}
			}

		}

		return null;
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
	 * @param messageType type of message
	 * @param message message
	 */
	@SuppressWarnings("unchecked")
	public void onMessage(WebSocketChannel webSocketChannel, int messageType, String message) {
		logger.debug("messageType: " + messageType + " message: " + message);
		
		
		Map<String, Object> map = (Map<String, Object>) ((JSONArray) JSONValue.parse(message)).get(0);
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
