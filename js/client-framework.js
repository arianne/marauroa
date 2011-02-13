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
marauroa.clientFramework = {

	/**
	 * connect to the server
	 *
	 * @param host server host name
	 * @param port server port number
	 */
	connect: function(host, port) {
		var options = {};
		if (typeof(port) != "undefined" && port != null) {
			options.port = port;
		}
		var socket = new io.Socket(host, options);
		socket.setMessageParser(socket.JSON_MESSAGE, {
			encode: function(obj) {
				return JSON.stringify(obj);
			},
			decode: function(str) {
				return JSON.parse(str);
			}
		});
		socket.connect();
		var temp = this;
		socket.on('message', function(mtype, obj){
			if (mtype == socket.JSON_MESSAGE) {
				temp.onMessage(obj);
			}
		});
		socket.on('connect', this.onConnect);
		socket.on('disconnect', this.onDisonnect);
		this.socket = socket;
	},

	onConnect: function(reason, error) {
		marauroa.log.debug("onConnect: " + reason + " error: " + error);
	},

	onDisconnect: function(reason, error) {
		marauroa.log.debug("onDisconnect: " + reason + " error: " + error);
	},

	onMessage: function(msg) {
		marauroa.log.debug(JSON.stringify(msg));
		if (msg.t == 9) {
			this.clientid = msg.c;
		}
		marauroa.messageFactory.addDispatchMethod(msg);
		msg.dispatch();
	},

	sendMessage: function(msg) {
		myMessage = {
			"c": this.clientid, 
			"s": "1"
		};
		io.util.merge(myMessage, msg);
		// TODO: is JSON.stringify required here?
		this.socket.send(JSON.stringify(myMessage));
	},

	resync: function() {
		// TODO: send MessageC2SOutOfSync
	},

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
	chooseCharacter: function(character) {
		var msg = {
			"t": "1",
			"character": character
		};
		this.sendMessage(msg);
	},

	onChooseCharacterNack: function() {
		marauroa.log.error("Server rejected your character.");
	},

	/**
	 * Sends a RPAction to server
	 *
	 * @param action
	 *            the action to send to server.
	 */
	sendAction: function(action) {
		var msg = {
			"t": "0",
			"a": action
		};
		this.sendMessage(msg);
	},

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
	logout: function() {
		var msg = {
				"t": "5",
				"a": action
			};
		this.sendMessage(msg);
	},
	
	onLogoutOutAck: function() {
		marauroa.log.debug("Server accepted logout request");
	},

	onLogoutOutNack: function() {
		marauroa.log.debug("Server rejected logout request");
	},

	/**
	 * Disconnect the socket and finish the network communications.
	 */
	close: function() {
		this.socket.close();
	},

	/**
	 * Are we connected to the server?
	 *
	 * @return true unless it is sure that we are disconnected
	 */
	getConnectionState: function() {
		// TODO: return netMan.getConnectionState();
	},

	/**
	 * It is called when a perception arrives so you can choose how to apply the
	 * perception.
	 *
	 * @param message
	 *            the perception message itself.
	 */
	onPerception: function(perceptionMessage) {
		marauroa.perceptionHandler.apply(perceptionMessage);
	},

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
	onTransferREQ: function(items) {
		marauroa.log.debug("onTransferREQ: " + items);
	},

	/**
	 * It is called when we get a transfer of content
	 *
	 * @param items
	 *            the transfered items.
	 */
	onTransfer: function(items) {
		marauroa.log.debug("onTransfer: " + items);
	},


	/**
	 * It is called when we get the list of characters
	 *
	 * @param characters
	 *            the characters we have available at this account.
	 */
	onAvailableCharacterDetails: function(characters) {
		marauroa.log.debug("onAvailableCharacterDetails: " + characters);
	},

	/**
	 * Returns the name of the game that this client implements
	 *
	 * @return the name of the game that this client implements
	 */
	onGameNameRequired: function() {
		return "implement onGameNameRequired()";
	},

	/**
	 * Returns the version number of the game
	 *
	 * @return the version number of the game
	 */
	onVersionNumberRequired: function() {
		return "0.0";
	}
}
