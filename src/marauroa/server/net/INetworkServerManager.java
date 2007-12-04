/* $Id: INetworkServerManager.java,v 1.12 2007/12/04 20:00:10 martinfuchs Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net;

import java.nio.channels.SocketChannel;

import marauroa.common.net.message.Message;
import marauroa.server.net.validator.ConnectionValidator;

/**
 * A Network Server Manager is an active object ( a thread ) that send and
 * receive messages from clients. There is not transport or technology imposed.
 * <p>
 * The Network Manager is our router that sends and receives messages to and
 * from the network. The manager exposes the interfaces that allow:
 * <ul>
 * <li>Reading a message from the network
 * <li>Sending a message to the network
 * <li>Finalizing the manager
 * </ul>
 * 
 * Now lets get back to the interface as exposed to other objects.<br>
 * 
 * The Write method is immediate, just call it with the message to send, making
 * sure that you have correctly filled SourceAddress and ClientID. The message
 * will then be sent to the Client.
 * 
 * The Read method is blocking, when you call the Read method it either returns
 * a message from the queue or if the queue is empty the thread blocks (sleeps)
 * until one arrives.
 * 
 * @author miguel
 */
public interface INetworkServerManager {

	/**
	 * Register a listener that will be called when a disconnected event
	 * happens. It is up to the implementer if this call add or replace the
	 * actual listener.
	 * 
	 * @param listener
	 *            a listener of disconnection events.
	 */
	public abstract void registerDisconnectedListener(IDisconnectedListener listener);

	/**
	 * This method provides the connection validator object. You can use it to
	 * ban connection IP.
	 * 
	 * @return validator.
	 */
	public abstract ConnectionValidator getValidator();

	/**
	 * This method blocks until a message is available
	 * 
	 * @return a Message
	 */
	public abstract Message getMessage();

	/**
	 * This method add a message to be delivered to the client the message is
	 * pointed to.
	 * 
	 * @param msg
	 *            the message to be delivered.
	 */
	public abstract void sendMessage(Message msg);

	/**
	 * This method disconnect a client or silently fails if client doesn't
	 * exists.
	 * 
	 * @param channel
	 */
	public abstract void disconnectClient(SocketChannel channel);

	/**
	 * This method inits the active object
	 */
	public abstract void start();

	/**
	 * This method notify the active object to finish it execution
	 */
	public abstract void finish();

}