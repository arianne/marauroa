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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import marauroa.common.Configuration;
import marauroa.server.game.rp.RPServerManager;

/**
 * a servlet for static content
 *
 * @author hendrik
 */
public class WebServletForStaticContent extends HttpServlet {

	private static final long serialVersionUID = 3182173716768800221L;
	private final RPServerManager rpMan;

	/**
	 * creates a WebServletForStaticContent
	 *
	 * @param rpMan RPServerManager
	 */
	public WebServletForStaticContent(RPServerManager rpMan) {
		this.rpMan = rpMan;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String filename = request.getPathInfo();
		filename = filename.substring(request.getContextPath().length());
		if (filename.endsWith("js")) {
			response.setContentType("text/javascript");
		} else if (filename.endsWith(".html")) {
			response.setContentType("text/html");
		} else if (filename.endsWith(".css")) {
			response.setContentType("text/css");
		} else if (filename.endsWith(".xml")) {
			response.setContentType("text/xml");
		} else {
			String contentType = rpMan.getMimeTypeForResource(filename);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
			response.setContentType(contentType);
		}

		Configuration conf = Configuration.getConfiguration();
		if (conf.has("debug_fake_web_username")) {
			request.getSession().setAttribute("marauroa_authenticated_username", conf.get("debug_fake_web_username"));
		}

		sendFile(response, filename);
	}

	/**
	 * sends a file to the server
	 *
	 * @param response response object to send the file to
	 * @param filename name of file to send
	 * @throws IOException in case of an input/output error
	 */
	private void sendFile(HttpServletResponse response, String filename) throws IOException {
		String name = filename;

		// prevent directory traversing
		if (name.indexOf("..") > -1) {
			throw new FileNotFoundException(name);
		}

		// optional marauroa path prefix
		if (name.startsWith("/marauroa/")) {
			name = name.substring(9);
		}

		InputStream is = null;
		OutputStream os = null;
		try {
			is = WebServletForStaticContent.class.getClassLoader().getResourceAsStream("js" + name);
			if (is == null) {
				is = WebServletForStaticContent.class.getClassLoader().getResourceAsStream("srcjs" + name);
			}
			if (is == null) {
				is = rpMan.getResource(name);
			}
			if (is == null) {
				throw new FileNotFoundException(name);
			}
			os = response.getOutputStream();
			byte[] buffer = new byte[8192];
			int count = is.read(buffer);

			while (count > -1) {
				os.write(buffer, 0, count);
				count = is.read(buffer);
			}
		} finally {
			if (is != null) {
				is.close();
			}
			if (os != null) {
				os.close();
			}
		}
	}
}
