/***************************************************************************
 *                   (C) Copyright 2011-2026 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

import { MarauroaUtils } from "./MarauroaUtils";
import { MessageDispatcher } from "./MessageDispatcher";
import { marauroa } from "./Marauroa";


/**
 * It is a wrapper over all the things that the client should do. You should
 * extend this class in your game.
 *
 * @author hendrik
 *
 */

export class ClientFramework {
	clientid: "-1"
	username: string;
	socket: WebSocket;
	messageDispatcher: MessageDispatcher;

	constructor() {
		this.messageDispatcher = new MessageDispatcher(this);
	}

	/**
	 * connect to the server
	 *
	 * @param host server host name
	 * @param port server port number
	 */
	connect(host: string, port: string, path: string) {
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
		if (port != "") {
			port = ":" + port;
		}
		if (path === null) {
			path = "ws/"
		}
		var url = protocol + "://" + host + port + "/" + path;
		let socket = new WebSocket(url);
		socket.addEventListener("message", (e) => this.onMessage(e));
		socket.addEventListener("open", (e) => {
			setInterval(function() {
				var msg = {
						"t": "8",
				};
				this.sendMessage(msg);
			}, 10000);
			this.onConnect();
		});
		socket.addEventListener("close", (e) => this.onDisconnect(e.reason, e.code, e.wasClean));
		this.socket = socket;
	}

	onConnect() {
		console.log("connected");
	}

	onDisconnect(reason: string, code: number, wasClean: boolean) {
		console.log("onDisconnect: " + reason + " code: " + code + " wasClean: " + wasClean);
	}

	onLoginRequired(config) {
		console.log("Config", config);
		// a login is required
	}

	login(username, password) {
		var msg = {
			"t": "34",
			"u": username,
			"p": password
		};
		this.username = username;
		this.sendMessage(msg);
	}

	onServerInfo(contents) {
		// console.log("ServerInfo", contents);
	}

	onPreviousLogins(previousLogins) {
		console.log("Previous Logins", previousLogins);
	}

	onLoginFailed(reason, text) {
		console.error("Login failed with reason " + reason + ": " + text);
	}

	onMessage(e) {
		var msg = JSON.parse(e.data);
		if (msg["t"] === "9" || msg["t"] === "15") {
			this.clientid = msg["c"];
		}
		if (typeof(msg) === "string") {
			console.error("JSON error on message: " + msg);
		} else {
			this.messageDispatcher.dispatchMessage(msg);
		}
	}

	sendMessage(msg) {
		var myMessage = {
			"c": this.clientid,
			"s": "1"
		};
		MarauroaUtils.merge(myMessage, msg);
		this.socket.send(JSON.stringify(myMessage));
	}

	resync() {
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
	chooseCharacter(character) {
		var msg = {
			"t": "1",
			"character": character
		};
		this.sendMessage(msg);
	}

	onChooseCharacterNack() {
		console.error("Server rejected your character.");
	}

	/**
	 * Sends a RPAction to server
	 *
	 * @param action
	 *            the action to send to server.
	 */
	sendAction(action) {
		var msg = {
			"t": "0",
			"a": action
		};
		this.sendMessage(msg);
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
	logout() {
		var msg = {
				"t": "5"
			};
		this.sendMessage(msg);
	}

	onLogoutOutAck() {
		console.log("Server accepted logout request");
	}

	onLogoutOutNack() {
		console.log("Server rejected logout request");
	}

	/**
	 * Disconnect the socket and finish the network communications.
	 */
	close() {
		this.socket.close();
	}

	/**
	 * It is called when a perception arrives so you can choose how to apply the
	 * perception.
	 *
	 * @param perceptionMessage
	 *            the perception message itself.
	 */
	onPerception(perceptionMessage) {
		marauroa.perceptionHandler.apply(perceptionMessage);
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
	onTransferREQ(items) {
		console.log("onTransferREQ: ", items);
	}

	/**
	 * It is called when we get a transfer of content
	 *
	 * @param items
	 *            the transfered items.
	 */
	onTransfer(items) {
		console.log("onTransfer: ", items);
	}


	/**
	 * It is called when we get the list of characters
	 *
	 * @param characters
	 *            the characters we have available at this account.
	 */
	onAvailableCharacterDetails(characters) {
		console.log("onAvailableCharacterDetails: ", characters);

		// create a character if there is none
		if (MarauroaUtils.isEmpty(characters)) {
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
	}

	createAccount(username, password, email) {
		var msg = {
				"t": "23",
				"u": username,
				"p": password,
				"e": email
			};
		this.sendMessage(msg);
	}

	onCreateAccountAck(username) {
		console.log("Account \"" + username + "\" created successfully");
	}

	onCreateAccountNack(username, reason) {
		console.log("Creating Account \"" + username + "\" failed: ", reason);
		alert(reason.text);
	}

	createCharacter(charname, template) {
		var msg = {
				"t": "26",
				"charname": charname,
				"template": template
			};
		this.sendMessage(msg);
	}

	onCreateCharacterAck(charname, template) {
		console.log("Character \"" + charname + "\" created successfully", template);
	}

	onCreateCharacterNack(charname, reason) {
		console.log("Creating Character \"" + charname + "\" failed: ", reason);
		alert(reason.text);
	}
};
