package marauroa.server.net;

import java.io.IOException;
import java.net.InetSocketAddress;

import marauroa.common.net.Message;

/** A Network Server Manager is an active object ( a thread ) that send and recieve messages
 *  from clients. There is not transport or technology imposed.
 *  
 * @author miguel
 */
public interface INetworkServerManager {
	public abstract PacketValidator getValidator();
	
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

	/** 
	 * This method disconnect a client or silently fails if client doesn't exists.
	 *  
	 * @param address
	 */
	public abstract void disconnectClient(InetSocketAddress address);

	/**
	 * This method inits the active object 
	 */
	public abstract void start();
	
	/** 
	 * This method notify the active object to finish it execution
	 */
	public abstract void finish();


}