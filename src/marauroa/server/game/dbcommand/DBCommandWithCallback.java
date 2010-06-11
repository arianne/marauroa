/***************************************************************************
 *                      (C) Copyright 2010 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.dbcommand;

import java.nio.channels.SocketChannel;

import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.messagehandler.DelayedEventHandler;

/**
 * A database command with callback support.
 *
 * @author hendrik
 */
public abstract class DBCommandWithCallback extends AbstractDBCommand {
	private int clientid;
	private SocketChannel channel;
	private int protocolVersion;
	protected DelayedEventHandler callback;

	/**
	 * Creates a new LoadCharacterCommand
	 */
	protected DBCommandWithCallback() {
		// default constructor
	}

	/**
	 * Creates a new LoadCharacterCommand
	 *
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion protocolVersion
	 */
	protected DBCommandWithCallback(DelayedEventHandler callback, int clientid, SocketChannel channel, int protocolVersion) {
		this.callback = callback;
		this.clientid = clientid;
		this.channel = channel;
		this.protocolVersion = protocolVersion;
	}

	/**
	 * gets the clientid
	 *
	 * @return clientid
	 */
	public int getClientid() {
		return clientid;
	}

	/**
	 * gets the SocketChannel
	 *
	 * @return SocketChannel
	 */
	public SocketChannel getChannel() {
		return channel;
	}

	/**
	 * gets the protocol version
	 * 
	 * @return protocolVersion
	 */
	public int getProtocolVersion() {
		return protocolVersion;
	}

}
