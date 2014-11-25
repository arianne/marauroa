package marauroa.server.net.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import javax.servlet.http.HttpSession;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.io.UnicodeSupportingInputStreamReader;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

/**
 * a websocket connection to a client
 *
 * @author hendrik
 */
public class WebSocketChannel extends WebSocketAdapter {
	private static Logger logger = Log4J.getLogger(WebSocketChannel.class);

	private static WebSocketConnectionManager webSocketServerManager = WebSocketConnectionManager.get();
	private String username;
	private InetSocketAddress address;

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		address = sess.getRemoteAddress();
		username = extractUsernameFromSession(sess.getUpgradeRequest());
		webSocketServerManager.onConnect(this);
		logger.debug("Socket Connected: " + sess);
	}

	/**
	 * extracts the username from the session, supporting both java and php sessions
	 *
	 * @param request HttpServletRequest
	 * @return username
	 */
	private String extractUsernameFromSession(UpgradeRequest request) {

		// first try java session
		HttpSession session = (HttpSession) request.getSession();
		if (session != null) {
			String temp = (String) session.getAttribute("marauroa_authenticated_username");
			if (temp != null) {
				return temp;
			}
		}

		// Jetty returns null instead of an empty list if there is no cookie header.
		if (request.getCookies() == null) {
			return null;
		}

		// try php session
		for (HttpCookie cookie : request.getCookies()) {
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
				String prefix = Configuration.getConfiguration().get("php_session_file_prefix", "/var/lib/php5/sess_");
				String filename = prefix + sessionid;
				if (new File(filename).canRead()) {
					br = new BufferedReader(new UnicodeSupportingInputStreamReader(new FileInputStream(filename)));
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

	@Override
	public void onWebSocketText(String message) {
		super.onWebSocketText(message);
		webSocketServerManager.onMessage(this, message);
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason);
		webSocketServerManager.onDisconnect(this);
		logger.debug("Socket Closed: [" + statusCode + "] " + reason);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		super.onWebSocketError(cause);
		if (cause instanceof SocketTimeoutException) {
			onWebSocketClose(-1, "Timeout");
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
	public void sendMessage(String json) {
		try {
			this.getRemote().sendString(json);
		} catch (IOException e) {
			logger.error(e, e);
		}
	}

	/**
	 * closes the socket
	 */
	public void close() {
		// TODO Auto-generated method stub

	}

}
