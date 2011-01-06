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
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.glines.socketio.common.DisconnectReason;
import com.glines.socketio.server.SocketIOInbound;

/**
 * a websocket channel which identifies a connection to a webclient.
 *
 * @author hendrik
 */
// TODO: don't extend SocketChannel but use some
public class WebSocketChannel extends SocketChannel implements SocketIOInbound {

	private String sessionId;
	private SocketIOOutbound outboundSocket;
	private WebSocketServerManager webSocketServerManager;

	/**
	 * creates a new WebSocketChannel
	 *
	 * @param webSocketServerManager 
	 * @param sessionId sessionid
	 */
	public WebSocketChannel(WebSocketServerManager webSocketServerManager, String sessionId) {
		super(null);
		this.webSocketServerManager = webSocketServerManager;
		this.sessionId = sessionId;
	}
	
	//
	// SocketChannel
	//

	@Override
	public Socket socket() {
		return null;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean isConnectionPending() {
		return false;
	}

	@Override
	public boolean connect(SocketAddress remote) throws IOException {
		return false;
	}

	@Override
	public boolean finishConnect() throws IOException {
		return false;
	}

	@Override
	public int read(ByteBuffer dst) throws IOException {
		return 0;
	}

	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		return 0;
	}

	@Override
	public int write(ByteBuffer src) throws IOException {
		return 0;
	}

	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		return 0;
	}

	@Override
	protected void implCloseSelectableChannel() throws IOException {
		// ignore
	}

	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
		// ignore
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

}
