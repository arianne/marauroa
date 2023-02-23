package marauroa.server.net.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WriteCallback;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.io.UnicodeSupportingInputStreamReader;
import marauroa.server.game.rp.DebugInterface;

/**
 * a websocket connection to a client
 *
 * @author hendrik
 */
public class WebSocketChannel extends WebSocketAdapter implements WriteCallback {
	private static Logger logger = Log4J.getLogger(WebSocketChannel.class);

	private static WebSocketConnectionManager webSocketServerManager = WebSocketConnectionManager.get();
	private LinkedList<String> queue = new LinkedList<String>();
	private boolean sending = false;
	
	private String username;
	private String useragent;
	private InetSocketAddress address;


	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess);
		UpgradeRequest upgradeRequest = sess.getUpgradeRequest();
		extractAddress(sess, upgradeRequest);
		useragent = upgradeRequest.getHeader("User-Agent");
		username = extractUsernameFromSession((HttpSession) upgradeRequest.getSession(), upgradeRequest.getCookies());
		webSocketServerManager.onConnect(this);
		logger.debug("Socket Connected: " + sess);
	}

	private void extractAddress(Session sess, UpgradeRequest upgradeRequest) {
		address = sess.getRemoteAddress();
		if (address.getAddress().isLoopbackAddress()) {
			String xff = upgradeRequest.getHeader("X-Forwarded-For");
			if (xff != null) {
				int pos = xff.lastIndexOf(" ");
				if (pos > -1) {
					xff = xff.substring(pos + 1);
				}
				address = new InetSocketAddress(xff, 0);
			}
		}
	}

	/**
	 * extracts the username from the session, supporting both java and php sessions
	 *
	 * @param session HttpSession
	 * @param cookies HttpCookies
	 * @return username
	 */
	public static String extractUsernameFromSession(HttpSession session, List<HttpCookie> cookies) {

		// first try java session
		if (session != null) {
			String temp = (String) session.getAttribute("marauroa_authenticated_username");
			if (temp != null) {
				return temp;
			}
		}

		// Jetty returns null instead of an empty list if there is no cookie header.
		if (cookies == null) {
			return null;
		}

		// try php session
		for (HttpCookie cookie : cookies) {
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
		String msg = DebugInterface.get().onMessage(useragent, message);
		super.onWebSocketText(msg);
		webSocketServerManager.onMessage(this, msg);
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
	public synchronized void sendMessage(String json) {
		queue.add(json);
		if (!this.sending) {
			sendNextMessage();
		}
	}

	private synchronized void sendNextMessage() {
		String message = queue.poll();
		if (message == null) {
			this.sending = false;
			return;
		}
		RemoteEndpoint remote = this.getRemote();
		if (remote != null) {
			this.sending = true;
			remote.sendString(message, this);
		}
	}

	/**
	 * closes the websocket channel
	 */
	public void close() {
		getSession().close();
	}

	public void writeFailed(Throwable e) {
		logger.error(e, e);
	}

	public synchronized void writeSuccess() {
		sendNextMessage();
	}
}
