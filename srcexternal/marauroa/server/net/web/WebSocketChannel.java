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
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.glines.socketio.common.DisconnectReason;
import com.glines.socketio.server.SocketIOFrame;
import com.glines.socketio.server.SocketIOInbound;

/**
 * a websocket channel which identifies a connection to a webclient.
 *
 * @author hendrik
 */
public class WebSocketChannel implements SocketIOInbound {

	private final String username;
	private SocketIOOutbound outboundSocket;
	private final WebSocketConnectionManager webSocketServerManager;
	private final InetAddress address;

	/**
	 * creates a new WebSocketChannel
	 *
	 * @param webSocketServerManager
	 * @param address ip-address of other end
	 * @param username sessionid
	 * @throws UnknownHostException in case the ip-address is invalid
	 */
	public WebSocketChannel(WebSocketConnectionManager webSocketServerManager, String address, String username) throws UnknownHostException {
		this.webSocketServerManager = webSocketServerManager;
		this.address = InetAddress.getByName(address);
		this.username = username;
	}

	//
	// SocketIOInbound
	//

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public void onConnect(SocketIOOutbound outbound) {
		this.outboundSocket = outbound;
		webSocketServerManager.onConnect(this);
	}

	@Override
	public void onDisconnect(DisconnectReason reason, String errorMessage) {
		webSocketServerManager.onDisconnect(this);
	}

	@Override
	public void onMessage(int messageType, String message) {
		webSocketServerManager.onMessage(this, messageType, message);
	}

	/**
	 * sends a message to the client
	 *
	 * @param json json string to send
	 */
	public void sendMessage(String json) {
		try {
			outboundSocket.sendMessage(SocketIOFrame.JSON_MESSAGE_TYPE, json);
		} catch (IOException e) {
			outboundSocket.disconnect();
		}
	}

	/**
	 * closes the connection
	 */
	public void close() {
		outboundSocket.close();
		outboundSocket.disconnect();
	}

	/**
	 * gets the ip-address
	 *
	 * @return address
	 */
	public InetAddress getAddress() {
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

}
