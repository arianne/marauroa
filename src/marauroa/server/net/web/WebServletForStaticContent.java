/***************************************************************************
 *                   (C) Copyright 2010-2024 - Marauroa                    *
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

import org.apache.log4j.Logger;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import marauroa.common.Configuration;
import marauroa.server.game.rp.DebugInterface;
import marauroa.server.game.rp.RPServerManager;

/**
 * a servlet for static content
 *
 * @author hendrik
 */
public class WebServletForStaticContent extends HttpServlet {
	private static Logger logger = Logger.getLogger(WebServletForStaticContent.class);

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
		try {
			String filename = request.getPathInfo();
			filename = filename.substring(request.getContextPath().length());
			String contentType = guessContentType(filename);
			response.setContentType(contentType);
	
			Configuration conf = Configuration.getConfiguration();
			if (conf.has("debug_fake_web_username")) {
				request.getSession().setAttribute("marauroa_authenticated_username", conf.get("debug_fake_web_username"));
			}
	
			String csp = "default-src 'none'; script-src 'self'; connect-src 'self' ws://*:* wss://*:*; img-src * data: blob: filesystem:; media-src * data: blob: filesystem:; style-src 'self'; font-src 'self'; frame-ancestors 'none'; sandbox allow-forms allow-same-origin allow-scripts allow-popups allow-modals allow-orientation-lock allow-pointer-lock allow-presentation allow-top-navigation allow-downloads";
			if (conf.has("content_security_policy")) {
				csp = conf.get("content_security_policy");
			}
			response.setHeader("Content-Security-Policy", csp);
	
			if (filename.endsWith(".css") || filename.endsWith(".html") || filename.endsWith(".js") || filename.endsWith(".json")) {
				response.setHeader("Cache-Control", "no-store, must-revalidate");
			}
	
			sendFile(request, response, filename);
		} catch (FileNotFoundException e) {
			response.sendError(404, "Not Found.");
		} catch (IOException e) {
			logger.error(e, e);
			response.sendError(500, "Unexpected error.");
		} 
	}

	/**
	 * guesses the Content-Type header based on filename extension
	 *
	 * @param filename filename
	 * @return Content-Type
	 */
	private String guessContentType(String filename) {
		String contentType = null;
		if (filename.endsWith("js")) {
			contentType = "text/javascript";
		} else if (filename.endsWith("json")) {
			contentType = "application/json";
		} else if (filename.endsWith(".html")) {
			contentType = "text/html";
		} else if (filename.endsWith(".css")) {
			contentType = "text/css";
		} else if (filename.endsWith(".xml")) {
			contentType = "text/xml";
		} else {
			contentType = rpMan.getMimeTypeForResource(filename);
			if (contentType == null) {
				contentType = "application/octet-stream";
			}
		}
		return contentType;
	}

	/**
	 * sends a file to the server
	 *
	 * @param request request object
	 * @param response response object to send the file to
	 * @param filename name of file to send
	 * @throws IOException in case of an input/output error
	 */
	private void sendFile(HttpServletRequest request, HttpServletResponse response, String filename) throws IOException {
		String name = filename;

		// prevent directory traversing
		if (name.indexOf("..") > -1) {
			throw new FileNotFoundException(name);
		}

		// optional marauroa path prefix
		if (name.startsWith("/marauroa/")) {
			name = name.substring(9);
		}
		if (name.startsWith("/srcjs/")) {
			name = name.substring(6);
		}
		if (name.startsWith("/src/js/")) {
			name = name.substring(7);
		}
		if (name.startsWith("/build/js/")) {
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
				is = WebServletForStaticContent.class.getClassLoader().getResourceAsStream("src/js" + name);
			}
			if (is == null) {
				is = DebugInterface.get().onFileRequest(request, response, name);
			}
			if (is == null) {
				is = rpMan.getResource(name);
			}
			if (is == null) {
				response.setStatus(404);
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
