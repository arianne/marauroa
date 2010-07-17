/* $Id: SendCharacterListHandler.java,v 1.2 2010/07/17 23:43:27 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.messagehandler;

import java.nio.channels.SocketChannel;
import java.util.Map;

import marauroa.common.game.RPObject;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.server.game.dbcommand.LoadAllActiveCharactersCommand;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.net.INetworkServerManager;

/**
 * sends the character list to the client
 *
 * @author hendrik
 */
public class SendCharacterListHandler implements DelayedEventHandler {

	/** We need network server manager to be able to send messages */
	private INetworkServerManager netMan;
	private int protocolVersion;

	/**
	 * creates a new SendCharacterListhHandler
	 *
	 @param netMan network manager
	 * @param protocolVersion version of protocol
	 */
	public SendCharacterListHandler(INetworkServerManager netMan, int protocolVersion) {
		this.netMan = netMan;
		this.protocolVersion = protocolVersion;
	}

	/**
	 * sends the character list back to the user
	 *
	 * @param rpMan ignored
	 * @param data LoadAllCharactersCommand
	 */
	public void handleDelayedEvent(RPServerManager rpMan, Object data) {
		LoadAllActiveCharactersCommand cmd = (LoadAllActiveCharactersCommand) data;
		Map<String, RPObject> characters = cmd.getCharacters();
		int clientid = cmd.getClientid();
		SocketChannel channel = cmd.getChannel();
		
		MessageS2CCharacterList msg = new MessageS2CCharacterList(channel, characters);
		msg.setProtocolVersion(protocolVersion);
		msg.setClientID(clientid);
		netMan.sendMessage(msg);
	}

}
