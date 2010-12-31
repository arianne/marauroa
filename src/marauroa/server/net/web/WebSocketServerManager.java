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

import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import marauroa.common.net.message.Message;
import marauroa.server.net.IDisconnectedListener;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.nio.NIONetworkServerManager;
import marauroa.server.net.validator.ConnectionValidator;

import com.glines.socketio.server.SocketIOInbound;
import com.glines.socketio.server.SocketIOServlet;

/**
 * a network manager implementation that uses a socket.io server for web based clients.
 *
 * @author hendrik
 */
public class WebSocketServerManager extends SocketIOServlet implements INetworkServerManager {

	private static final long serialVersionUID = 4898279536921406401L;
	private NIONetworkServerManager netMan;
	private Set<WebSocketChannel> channels = Collections.synchronizedSet(new HashSet<WebSocketChannel>());

	/**
	 * creates a new WebSocketServerManager
	 *
	 * @param netMan classic network server manager.
	 */
	public WebSocketServerManager(NIONetworkServerManager netMan) {
		this.netMan = netMan;
	}

	@Override
	public void registerDisconnectedListener(IDisconnectedListener listener) {
		netMan.registerDisconnectedListener(listener);
	}

	@Override
	public ConnectionValidator getValidator() {
		return netMan.getValidator();
	}

	@Override
	public Message getMessage() {
		return null;
	}

	@Override
	public void sendMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnectClient(SocketChannel channel) {
		// TODO Auto-generated method stub
	}

	@Override
	public void start() {
		// do nothing
	}

	@Override
	public void finish() {
		// do nothing
	}

	@Override
	protected SocketIOInbound doSocketIOConnect(HttpServletRequest request, String[] protocols) {
		return new WebSocketChannel(this, (String) request.getSession().getAttribute("jsessionid"));
	}

	/**
	 * a client connected
	 * 
	 * @param webSocketChannel channel to the client
	 */
	void onConnect(WebSocketChannel webSocketChannel) {
		channels.add(webSocketChannel);
	}

	/**
	 * a client disconnected
	 *
	 * @param webSocketChannel channel to the client
	 */
	public void onDisconnect(WebSocketChannel webSocketChannel) {
		channels.remove(webSocketChannel);
		netMan.notifyDisconnectListener(webSocketChannel);
	}

}
