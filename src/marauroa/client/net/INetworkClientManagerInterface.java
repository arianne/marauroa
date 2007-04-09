package marauroa.client.net;

import java.net.InetSocketAddress;

import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;

/**
 * All network-communication is done through this interface. There are different
 * implementations.
 */
public interface INetworkClientManagerInterface {

	/**
	 * This method notify the thread to finish it execution
	 */
	void finish();

	/**
	 * Returns the ip address and port-number
	 *
	 * @return InetSocketAddress
	 */
	InetSocketAddress getAddress();

	/**
	 * This method returns a Message from the list or block for timeout
	 * milliseconds until a message is available or null if timeout happens.
	 *
	 * @param timeout
	 *            timeout time in milliseconds
	 * @return a Message or null if timeout happens
	 * @throws InvalidVersionException
	 */
	Message getMessage(int timeout) throws InvalidVersionException;

	/**
	 * This method add a message to be delivered to the client the message is
	 * pointed to.
	 *
	 * @param msg
	 *            the message to be delivered.
	 */
	void addMessage(Message msg);

	/**
	 * returns true unless it is sure that we are disconnected.
	 *
	 * @return true if we may be online
	 */
	boolean getConnectionState();
}