package marauroa.client.net;

import java.net.InetSocketAddress;

import marauroa.common.net.Message;

/**
 * All network-communication is done through this interface. There are different
 * implementations.
 */
public interface NetworkClientManagerInterface {

	/**
	 * This method notify the thread to finish it execution
	 */
	public abstract void finish();

	/**
	 * Returns the ip address and port-number
	 * 
	 * @return InetSocketAddress
	 */
	public abstract InetSocketAddress getAddress();

	/**
	 * This method returns a Message from the list or block for timeout
	 * milliseconds until a message is available or null if timeout happens.
	 * 
	 * @param timeout
	 *            timeout time in milliseconds
	 * @return a Message or null if timeout happens
	 */
	public abstract Message getMessage(int timeout);

	/**
	 * This method add a message to be delivered to the client the message is
	 * pointed to.
	 * 
	 * @param msg
	 *            the message to be delivered.
	 */
	public abstract void addMessage(Message msg);

}