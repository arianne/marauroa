/* $Id: ClientFramework.java,v 1.68 2010/11/26 20:07:27 martinfuchs Exp $ */
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

/**
 * It is a wrapper over all the things that the client should do. You should
 * extend this class at your game.
 *
 * @author miguel, hendrik
 *
 */
marauroa.clientFramework = new function() {

	/**
	 * connect to the server
	 *
	 * @param host server host name
	 * @param port server port number
	 */
	function connect(host, port) {
		// TODO: Connect
	}

	function resync() {
		// TODO: send MessageC2SOutOfSync
	}

	/**
	 * After login allows you to choose a character to play
	 *
	 * @param character
	 *            name of the character we want to play with.
	 * @return true if choosing character is successful.
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws BannedAddressException
	 */
	function chooseCharacter(character) {
		// TODO send MessageC2SChooseCharacter(character);
	}

	function onChooseCharacterAck() {
		
	}

	function onChooseCharacterNAck() {
		
	}

	/**
	 * Sends a RPAction to server
	 *
	 * @param action
	 *            the action to send to server.
	 */
	function send(action) {
		// TODO: send new MessageC2SAction(action);
	}

	/**
	 * Request logout of server
	 *
	 * @return true if we have successfully logout or false if server rejects to
	 *         logout our player and maintain it on game world.
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws BannedAddressException
	 */
	function logout() {
		// TODO: send  MessageC2SLogout();
	}
	
	function onLogoutOutAck() {
		
	}
	function onLogoutOutNack() {
		
	}

	/**
	 * Disconnect the socket and finish the network communications.
	 */
	function close() {
		// TODO: close websocket
	}

	/**
	 * Are we connected to the server?
	 *
	 * @return true unless it is sure that we are disconnected
	 */
	function getConnectionState() {
		// TODO: return netMan.getConnectionState();
	}

	/**
	 * It is called when a perception arrives so you can choose how to apply the
	 * perception.
	 *
	 * @param message
	 *            the perception message itself.
	 */
	function onPerception(perceptionMessage) {
	}

	/**
	 * is called before a content transfer is started.
	 * 
	 * <code> items </code> contains a list of names and timestamp. 
	 * That information can be used to decide if a transfer from server is needed. 
	 * By setting attribute ack to true in a TransferContent it will be acknowledged.   
	 * All acknowledges items in the returned List, will be transfered by server.
	 *
	 * @param items
	 *            in this list by default all items.ack attributes are set to false;
	 * @return the list of approved and rejected items.
	 */
	function onTransferREQ(items) {
	}

	/**
	 * It is called when we get a transfer of content
	 *
	 * @param items
	 *            the transfered items.
	 */
	function onTransfer(items) {
	}


	/**
	 * It is called when we get the list of characters
	 *
	 * @param characters
	 *            the characters we have available at this account.
	 */
	function onAvailableCharacterDetails(characters) {
	}
	
	

	/**
	 * Returns the name of the game that this client implements
	 *
	 * @return the name of the game that this client implements
	 */
	function getGameName() {
		return "implement clientframework.getGameName()";
	}

	/**
	 * Returns the version number of the game
	 *
	 * @return the version number of the game
	 */
	function getVersionNumber() {
		return "0.0";
	}
}
