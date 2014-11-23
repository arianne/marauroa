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
	clientid: "-1",

	/**
	 * connect to the server
	 *
	 * @param host server host name
	 * @param port server port number
	 */
	connect: function(host, port) {
		var protocol = "ws"
		if (window.location.protocol === "https:") {
			protocol = "wss";
		}
		if (host === null) {
			host = window.location.hostname;
		}
		if (port === null) {
			port = window.location.port;
		}
		var url = protocol + "://" + host + ":" + port + "/ws/";
		var socket = new WebSocket(url);
		socket.onmessage = this.onMessage;
		socket.onopen = this.onConnect;
		socket.onclose = this.onDisonnect;
		this.socket = socket;
	},

	onConnect: function(reason, error) {
		if (typeof(error) == "undefined") {
			marauroa.log.debug("connected");
		} else {
			marauroa.log.error("onConnect: " + reason + " error: " + error);
		}
	},

	onDisconnect: function(reason, error) {
		marauroa.log.debug("onDisconnect: " + reason + " error: " + error);
	},

	onLoginRequired: function() {
		// a login is required
	},

	login: function(username, password) {
		var msg = {
			"t": "34",
			"u": username,
			"p": password
		};
		this.username = username;
		this.sendMessage(msg);
	},

	onServerInfo: function(contents) {
		marauroa.log.debug("ServerInfo", contents);
	},

	onPreviousLogins: function(previousLogins) {
		marauroa.log.debug("Previous Logins", previousLogins);
	},

	onLoginFailed: function(reason, text) {
		marauroa.log.error("Login failed with reason " + reason + ": " + text);
	},

	onMessage: function(e) {
		var msg = JSON.parse(e.data);
		if (marauroa.debug.messages) {
			marauroa.log.debug("<--: ", msg);
		}
		if (msg.t == 9 || msg.t == 15) {
			this.clientid = msg.c;
		}
		if (typeof(msg) == "string") {
			marauroa.log.error("JSON error on message: " + msg);
		} else {
			marauroa.messageFactory.addDispatchMethod(msg);
			msg.dispatch();
		}
	},

	sendMessage: function(msg) {
		var myMessage = {
			"c": this.clientid, 
			"s": "1"
		};
		marauroa.util.merge(myMessage, msg);
		if (marauroa.debug.messages) {
			marauroa.log.debug("-->: ", msg);
		}
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
				"t": "5"
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

	/*
	 * Are we connected to the server?
	 *
	 * @return true unless it is sure that we are disconnected
	 */
	/*getConnectionState: function() {
		// TODO: return netMan.getConnectionState();
	},*/

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
		marauroa.log.debug("onTransferREQ: ", items);
	},

	/**
	 * It is called when we get a transfer of content
	 *
	 * @param items
	 *            the transfered items.
	 */
	onTransfer: function(items) {
		marauroa.log.debug("onTransfer: ", items);
	},


	/**
	 * It is called when we get the list of characters
	 *
	 * @param characters
	 *            the characters we have available at this account.
	 */
	onAvailableCharacterDetails: function(characters) {
		marauroa.log.debug("onAvailableCharacterDetails: ", characters);

		// create a character if there is none
		if (marauroa.util.isEmpty(characters)) {
			marauroa.log.debug("No character found, creating a character with the username (redefine onAvailableCharacterDetails to prevent this).");
			this.createCharacter(this.username, {});
			return;
		}

		// automatically select the first one
		for (key in characters) {
			if (characters.hasOwnProperty(key)) {
				this.chooseCharacter(key);
			}
		}
	},

	createAccount: function(username, password, email) {
		var msg = {
				"t": "23",
				"u": username,
				"p": password,
				"e": email
			};
		this.sendMessage(msg);
	},

	onCreateAccountAck: function(username) {
		marauroa.log.debug("Account \"" + username + "\" created successfully.");
	},

	onCreateAccountNack: function(username, reason) {
		marauroa.log.debug("Creating Account \"" + username + "\" failed: ", reason);
		alert(reason.text);
	},

	createCharacter: function(charname, template) {
		var msg = {
				"t": "26",
				"charname": charname,
				"template": template
			};
		this.sendMessage(msg);
	},

	onCreateCharacterAck: function(charname, template) {
		marauroa.log.debug("Character \"" + charname + "\" created successfully.");
	},

	onCreateCharacterNack: function(charname, reason) {
		marauroa.log.debug("Creating Character \"" + charname + "\" failed: ", reason);
		alert(reason.text);
	}
}
