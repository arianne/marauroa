/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.client.net;

import java.util.TimerTask;

import marauroa.common.net.message.MessageC2SKeepAlive;

/**
 * sends keep alive messages regularly.
 *
 * @author hendrik
 */
public class KeepAliveSender extends TimerTask {
	private INetworkClientManagerInterface netMan;

	/**
	 * creates a new KeepAliveSender.
	 *
	 * @param netMan INetworkClientManagerInterface
	 */
	public KeepAliveSender(INetworkClientManagerInterface netMan) {
		this.netMan = netMan;
	}

	/**
	 * sends a keep alive message.
	 */
	@Override
	public void run() {
		MessageC2SKeepAlive msgAlive = new MessageC2SKeepAlive();
		netMan.addMessage(msgAlive);
	}

}
