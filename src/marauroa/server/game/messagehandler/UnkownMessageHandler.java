package marauroa.server.game.messagehandler;

import marauroa.common.Log4J;
import marauroa.common.net.message.Message;
import marauroa.server.game.GameServerManager;

/**
 * Handles unknown messages
 *
 * @author hendrik
 */
class UnkownMessageHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);

	@Override
	public void process(Message message) {
		logger.error("Received unknown message: " + message);
	}

}
