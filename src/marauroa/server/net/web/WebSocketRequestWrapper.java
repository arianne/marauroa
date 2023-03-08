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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;
import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.crypto.Hash;
import marauroa.common.io.UnicodeSupportingInputStreamReader;

/**
 * wraps a request in order to make metadata available to a websocket endpoint
 *
 * @author hendrik
 */
public class WebSocketRequestWrapper extends HttpServletRequestWrapper {

	/** a random secret which prevents direct invokation of the internal websocket endpoint */
	public static final String SECRET = Hash.toHexString(Hash.random(255));
	private static Logger logger = Log4J.getLogger(WebSocketRequestWrapper.class);

	private HttpServletRequest request;

	/**
	 * creates a webSocketRequestWrapper
	 *
	 * @param request original request
	 */
	public WebSocketRequestWrapper(HttpServletRequest request) {
		super(request);
		this.request = request;
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		Map<String, String[]> res = new HashMap<String, String[]>();
		res.put("secret", new String[] { SECRET });
		res.put("useragent", new String[] { request.getHeader("User-Agent") });
		res.put("origin", new String[] { request.getHeader("Origin") });
		res.put("address", new String[] { extractAddress() });
		res.put("marauroa_authenticated_usernam", new String[] { extractUsernameFromSession() });
		return res;
	}

	private String extractAddress() {
		String address = request.getRemoteAddr();
		InetAddress remoteAddr;
		try {
			remoteAddr = InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		if (remoteAddr.isLoopbackAddress()) {
			String xff = request.getHeader("X-Forwarded-For");
			if (xff != null) {
				int pos = xff.lastIndexOf(" ");
				if (pos > -1) {
					xff = xff.substring(pos + 1);
				}
				address = xff;
			}
		}
		return address;
	}
	
	/**
	 * extracts the username from the session, supporting both java and php sessions
	 *
	 * @return username
	 */
	public String extractUsernameFromSession() {
		HttpSession session = request.getSession(false);

		// first try java session
		if (session != null) {
			String temp = (String) session.getAttribute("marauroa_authenticated_username");
			if (temp != null) {
				return temp;
			}
		}

		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}

		// try php session
		for (Cookie cookie : cookies) {
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
				String prefix = Configuration.getConfiguration().get("php_session_file_prefix",
						"/var/lib/php5/sess_");
				String filename = prefix + sessionid;
				if (new File(filename).canRead()) {
					br = new BufferedReader(
							new UnicodeSupportingInputStreamReader(new FileInputStream(filename)));
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

}
