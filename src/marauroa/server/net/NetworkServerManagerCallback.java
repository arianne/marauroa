package marauroa.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Callback interface into NetworkManagerServer to prevent
 * dependency loop between NetworkManagerServer and ...Writer/Reader.
 *
 * @author hendrik
 */
public interface NetworkServerManagerCallback {

	/**
	 * Are we still running?
	 *
	 * @return keepRunning
	 */
	public boolean isStillRunning();
	
	/**
	 * Notifies the NetworkServerManager that the read-thread has finished.
	 */
	public void finishedReadThread();

	/**
	 * Receives a Message
	 *
	 * @param data data
	 * @param inetSocketAddress the address of the client socket (ip+port)
	 * @throws IOException on an io-error.
	 */
	public void receiveMessage(byte[] data, InetSocketAddress inetSocketAddress) throws IOException;

	/**
	 * Disconnect a client freeing associated resources
	 *
	 * @param inetSocketAddress InetSocketAddress
	 */
	public void disconnectClient(InetSocketAddress inetSocketAddress);
}
