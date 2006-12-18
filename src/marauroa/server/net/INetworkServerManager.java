package marauroa.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import marauroa.common.net.Message;

public interface INetworkServerManager {

	/** 
	 * This method notify the thread to finish it execution
	 */
	public abstract void finish();

	/** 
	 * This method returns a Message from the list or block for timeout milliseconds
	 * until a message is available or null if timeout happens.
	 *
	 * @param timeout timeout time in milliseconds
	 * @return a Message or null if timeout happens
	 */
	public abstract Message getMessage(int timeout);

	/** 
	 * This method blocks until a message is available
	 *
	 * @return a Message
	 */
	public abstract Message getMessage();

	/**
	 * This method add a message to be delivered to the client the message
	 * is pointed to.
	 *
	 * @param msg the message to be delivered.
	 * @throws IOException 
	 */
	public abstract void sendMessage(Message msg);

	public abstract boolean isStillRunning();

	public abstract void disconnectClient(InetSocketAddress address);

	public abstract void start();

}