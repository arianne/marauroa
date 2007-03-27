package marauroa.server.net.flood;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.server.net.IDisconnectedListener;

/**
 * This class implements a Flood checking for all the connections to server.
 * What is a flood is implemented at the IFloodCheck interface.
 * @author miguel
 *
 */
public class FloodValidator implements IDisconnectedListener{
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(FloodValidator.class);

	/** Stores the connections */
	Map<SocketChannel, FloodMeasure> connections;

	/** This interface implements the flood checking. */
	IFloodCheck floodCheck;

	/**
	 * Constructor
	 * @param check the implementation of the flood check.
	 */
	public FloodValidator(IFloodCheck check) {
		connections=new HashMap<SocketChannel, FloodMeasure>();
		this.floodCheck=check;
	}

	/**
	 * Adds a new channel to the flood validator.
	 * @param channel the new added channel.
	 */
	public void add(SocketChannel channel) {
		connections.put(channel, new FloodMeasure(channel));
	}

	/**
	 * Callback method.
	 * It will be called by NIOServer when the connection is closed.
	 */
	public void onDisconnect(SocketChannel channel) {
		connections.remove(channel);
	}

	/**
	 * Returns true if the channel passed as param is considered to be flooding
	 *
	 * @param channel the channel we got the new message from
	 * @param length the length in bytes of the message.
	 * @return true if it is flooding.
	 */
	public boolean isFlooding(SocketChannel channel, int length) {
		FloodMeasure entry=connections.get(channel);
		if(entry==null) {
			logger.warn("This connection is not registered. Impossible: "+channel);
			return true;
		}

		entry.addMessage(length);

		boolean result=floodCheck.isFlooding(entry);

		if(result) {
			logger.info("Connection determined to be flooding");
			entry.warning();
		}

		return result;
	}

	 /**
	  * This method will call onFlood method of the flood check so appropiate actions
	 * can be taken for a flooding channel
	  */
	public void onFlood(SocketChannel channel) {
		floodCheck.onFlood(channel);
	}
}
