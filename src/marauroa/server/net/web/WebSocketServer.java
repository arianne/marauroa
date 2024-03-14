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
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.scan.StandardJarScanner;

import marauroa.common.Configuration;
import marauroa.server.marauroad;
import marauroa.server.game.rp.RPServerManager;

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

	private static String createTemporaryFolder() throws IOException {
		Path dir = Files.createTempDirectory("maraurora");
		dir.toFile().deleteOnExit();
		return dir.toAbsolutePath().toString();
	}

	/**
	 * starts the web server
	 *
	 * @throws Exception in case of an unexpected exception
	 */
	public static void startWebSocketServer() throws Exception {

		Configuration conf = Configuration.getConfiguration();
		String host = conf.get("http_host", "127.0.0.1");
		int port = conf.getInt("http_port", -1);
		if (port < 0) {
			return;
		}

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(createTemporaryFolder());

		Connector connector = new Connector();
		connector.setPort(port);
		connector.setProperty("address", host);
		tomcat.setConnector(connector);

		StandardHost standardHost = (StandardHost) tomcat.getHost();
		standardHost.setErrorReportValveClass("marauroa.server.net.web.DetaillessErrorReportValve");

		Context context = tomcat.addContext("", createTemporaryFolder());
		StandardJarScanner scan = (StandardJarScanner) context.getJarScanner();
		scan.setScanClassPath(true);
		scan.setScanBootstrapClassPath(true);
		scan.setScanAllDirectories(false);
		scan.setScanAllFiles(false);
		ContextConfig contextConfig = new ContextConfig();
		context.addLifecycleListener(contextConfig);

		tomcat.addServlet("", "websocket", new WebSocketServlet());
		context.addServletMappingDecoded("/ws/*", "websocket");

		RPServerManager rpServerManager = marauroad.getMarauroa().getRPServerManager();
		tomcat.addServlet("", "static", new WebServletForStaticContent(rpServerManager));
		context.addServletMappingDecoded("/*", "static");

		tomcat.start();
	}

}
