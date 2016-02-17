/***************************************************************************
 *                   (C) Copyright 2011-2016 - Marauroa                    *
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
 * @author hendrik
 *
 */
"use strict";
var marauroa = window.marauroa || {};

marauroa.clientFramework = {
	clientid: "-1",

	/**
	 * connect to the server
	 *
	 * @param host server host name
	 * @param port server port number
	 */
	connect: function(host, port) {
		var protocol = "ws";
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
			console.log("connected");
		} else {
			console.error("onConnect: " + reason + " error: " + error);
		}
	},

	onDisconnect: function(reason, error) {
		console.log("onDisconnect: " + reason + " error: " + error);
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
		console.log("ServerInfo", contents);
	},

	onPreviousLogins: function(previousLogins) {
		console.log("Previous Logins", previousLogins);
	},

	onLoginFailed: function(reason, text) {
		console.error("Login failed with reason " + reason + ": " + text);
	},

	onMessage: function(e) {
		var msg = JSON.parse(e.data);
		if (marauroa.debug.messages) {
			console.log("<--: ", msg);
		}
		if (msg.t === "9" || msg.t === "15") {
			marauroa.clientFramework.clientid = msg.c;
		}
		if (typeof(msg) === "string") {
			console.error("JSON error on message: " + msg);
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
			console.log("-->: ", msg);
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
		console.error("Server rejected your character.");
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
		console.log("Server accepted logout request");
	},

	onLogoutOutNack: function() {
		console.log("Server rejected logout request");
	},

	/**
	 * Disconnect the socket and finish the network communications.
	 */
	close: function() {
		this.socket.close();
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
		console.log("onTransferREQ: ", items);
	},

	/**
	 * It is called when we get a transfer of content
	 *
	 * @param items
	 *            the transfered items.
	 */
	onTransfer: function(items) {
		console.log("onTransfer: ", items);
	},


	/**
	 * It is called when we get the list of characters
	 *
	 * @param characters
	 *            the characters we have available at this account.
	 */
	onAvailableCharacterDetails: function(characters) {
		console.log("onAvailableCharacterDetails: ", characters);

		// create a character if there is none
		if (marauroa.util.isEmpty(characters)) {
			console.log("No character found, creating a character with the username (redefine onAvailableCharacterDetails to prevent this).");
			this.createCharacter(this.username, {});
			return;
		}

		// automatically select the first one
		for (var key in characters) {
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
		console.log("Account \"" + username + "\" created successfully");
	},

	onCreateAccountNack: function(username, reason) {
		console.log("Creating Account \"" + username + "\" failed: ", reason);
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
		console.log("Character \"" + charname + "\" created successfully", template);
	},

	onCreateCharacterNack: function(charname, reason) {
		console.log("Creating Character \"" + charname + "\" failed: ", reason);
		alert(reason.text);
	}
}
