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

import marauroa.common.Configuration;
import marauroa.server.marauroad;
import marauroa.server.net.IServerManager;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.glines.socketio.server.transport.FlashSocketTransport;

/**
 * web socket server
 *
 * @author hendrik
 */
public class WebSocketServer {

	/**
	 * starts a Marauroa server with web socket support
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		marauroad.getMarauroa().init(args);

		startWebSocketServer();

		marauroad.getMarauroa().start();
	}

	private static void startWebSocketServer() throws Exception {
		Configuration conf = Configuration.getConfiguration();
		if (!conf.has("http_port") && conf.has("ajp_port")) {
			return;
		}

		Server server = new Server();

		String host = conf.get("http_host", "localhost");
		int port = -1;
		if (conf.has("http_port")) {
			port = conf.getInt("http_port", 8080);
			SelectChannelConnector connector = new SelectChannelConnector();
			connector.setHost(host);
			connector.setPort(port);
			server.addConnector(connector);
		}

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		ServletHolder holder = new ServletHolder(new WebSocketConnectionManager(
				(IServerManager) marauroad.getMarauroa().getNetMan()));
		holder.setInitParameter(FlashSocketTransport.FLASHPOLICY_SERVER_HOST_KEY, host);
		holder.setInitParameter(FlashSocketTransport.FLASHPOLICY_DOMAIN_KEY, host);
		holder.setInitParameter(FlashSocketTransport.FLASHPOLICY_PORTS_KEY, "" + port);
		context.addServlet(holder, "/socket.io/*");

		server.setHandler(context);
		server.start();
	}
}
