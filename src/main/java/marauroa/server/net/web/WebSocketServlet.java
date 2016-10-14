package marauroa.server.net.web;

import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * A websocket dispatcher servlet
 *
 * @author hendrik
 */
public class WebSocketServlet extends org.eclipse.jetty.websocket.servlet.WebSocketServlet{
	private static final long serialVersionUID = -1812859392719209598L;

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.register(WebSocketChannel.class);
	}

}
