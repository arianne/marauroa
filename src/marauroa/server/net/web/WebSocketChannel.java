/***************************************************************************
 *                   (C) Copyright 2010-2023 - Marauroa                    *
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
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import jakarta.websocket.CloseReason;
import jakarta.websocket.CloseReason.CloseCodes;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint.Basic;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.game.rp.DebugInterface;

/**
 * a websocket connection to a client
 *
 * @author hendrik
 */
@ServerEndpoint(value = "/wsinternal")
public class WebSocketChannel {
	private static Logger logger = Log4J.getLogger(WebSocketChannel.class);

	private static WebSocketConnectionManager webSocketServerManager = WebSocketConnectionManager.get();
	private Session socketSession;

	private String username;
	private String useragent;
	private InetSocketAddress address;

	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		this.socketSession = session;

		Map<String, List<String>> params = session.getRequestParameterMap();
		if (!WebSocketRequestWrapper.SECRET.equals(params.get("secret").get(0))) {
			logger.warn("Direct request to /wsinternal");
			close();
			return;
		}
		
		address = new InetSocketAddress(params.get("address").get(0), 0);
		useragent = params.get("useragent").get(0);
		String origin = params.get("origin").get(0);
		try {
			String expectedOrigin = Configuration.getConfiguration().get("http_origin");
			if ((expectedOrigin != null) && !expectedOrigin.equals(origin)) {
				logger.warn("Expected origin " + expectedOrigin + " from client " + address + " but got " + origin);
				close();
				return;
			}
		} catch (IOException e) {
			logger.error(e, e);
			close();
			return;
		}
		username = params.get("marauroa_authenticated_usernam").get(0);
		if (username == null) {
			logger.warn("No username in request by" + address);
			close();
			return;
		}
		webSocketServerManager.onConnect(this);
		logger.debug("Socket Connected: " + session);
	}


	@OnMessage
	public void onWebSocketText(String message) {
		String msg = DebugInterface.get().onMessage(useragent, message);
		webSocketServerManager.onMessage(this, msg);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		webSocketServerManager.onDisconnect(this);
		logger.debug("WebSocket Closed");
	}


	@OnError
	public void onError(Session session, Throwable cause) {
		if (cause instanceof SocketTimeoutException) {
			onClose(session, new CloseReason(CloseCodes.UNEXPECTED_CONDITION, "Timeout"));
			return;
		}
		logger.error(cause, cause);
	}

	/**
	 * gets the ip-address and port
	 *
	 * @return address
	 */
	public InetSocketAddress getAddress() {
		return address;
	}

	/**
	 * gets the username
	 *
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * sends a message to the client
	 *
	 * @param json json string to send
	 */
	public synchronized void sendMessage(String json) {
		try {
			Basic remote = socketSession.getBasicRemote();
			if (remote != null) {
				remote.sendText(json);
			}
		} catch (IOException e) {
			logger.error(e, e);
		} catch (IllegalStateException e) {
			logger.warn(e);
		}
	}

	/**
	 * closes the websocket channel
	 */
	public void close() {
		try {
			socketSession.close();
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

}
