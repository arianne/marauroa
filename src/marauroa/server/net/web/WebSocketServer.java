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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

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
		System.err.println("The instructions, you followed, are outdated.");
		System.err.println("Please use the normal marauroa.server.marauroad as main class");
	}

	/**
	 * starts the web server
	 *
	 * @throws Exception in case of an unexpected exception
	 */
	public static void startWebSocketServer() throws Exception {
		Configuration conf = Configuration.getConfiguration();
		if (!conf.has("http_port")) {
			return;
		}

		Server server = new Server();

		String host = conf.get("http_host", "localhost");
		int port = -1;
		if (conf.has("http_port")) {
			port = conf.getInt("http_port", 8080);
			ServerConnector connector = new ServerConnector(server);
			connector.setHost(host);
			connector.setPort(port);
// TODO			connector.setForwarded(Boolean.parseBoolean(conf.get("http_forwarded", "false")));
			server.addConnector(connector);
		}

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		server.setHandler(context);

		ServletHolder holderEvents = new ServletHolder("ws", WebSocketServlet.class);
		context.addServlet(holderEvents, "/ws/*");
		
		context.addServlet(new ServletHolder(new WebServletForStaticContent(marauroad.getMarauroa().getRPServerManager())), "/*");

		server.start();
	}
}
