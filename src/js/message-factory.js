/***************************************************************************
 *                   (C) Copyright 2010-2015 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

"use strict";

var marauroa = window.marauroa || {};

marauroa.messageFactory = new function() {

	// Message S2C Login NACK
	this.t14 = function() {
		marauroa.clientFramework.onLoginFailed(this.reason, this.text);
	}

	// Message S2C CharacterList
	this.t9 = function() {
		marauroa.clientFramework.onAvailableCharacterDetails(this.characters);
	}

	// Message S2C ChoooseCharacterACK
	this.t10 = function() {
		console.log("Entering world");
	}

	// Message S2C ChoooseCharacterNACK
	this.t11 = function() {
		console.log("Character selection rejected");
		marauroa.clientFramework.onChooseCharacterNack();
	}

	// Message S2C Login ACK
	this.t13 = function() {
		marauroa.clientFramework.onPreviousLogins(this.previousLogins);
	}

	// Message S2C Send Key
	this.t15 = function() {
		console.log("Server send key: ", this);
		marauroa.clientFramework.onLoginRequired();
	}
	
	// Message S2C Perception
	this.t19 = function() {
		marauroa.clientFramework.onPerception(this);
	}

	// Message S2C Server Info
	this.t20 = function() {
		marauroa.clientFramework.onServerInfo(this.contents);
	}

	// Message S2C Transfer
	this.t21 = function() {
		marauroa.clientFramework.onTransfer(this.contents);
	}

	// Message S2C TransferREQ
	this.t22 = function() {
		marauroa.clientFramework.onTransferREQ(this.contents);
		var contents = {};
		for (var i in this.contents) {
			if (typeof(this.contents[i].ack) != "undefined" && this.contents[i].ack) {
				contents[this.contents[i].name] = true;
			} else {
				contents[this.contents[i].name] = false;
			}
		}
		var msg = {
			"t": "7",
			"contents": contents
		}
		marauroa.clientFramework.sendMessage(msg);
	}

	// Message S2C CreateAccount ACK
	this.t24 = function() {
		marauroa.clientFramework.onCreateAccountAck(this.username);
	}

	// Message S2C CreateAccount NACK
	this.t25 = function() {
		marauroa.clientFramework.onCreateAccountNack(this.username, this.reason);
	}

	// Message S2C CreateCharacter ACK
	this.t27 = function() {
		marauroa.clientFramework.onCreateCharacterAck(this.charname, this.template);
	}

	// Message S2C CreateCharacter NACK
	this.t28 = function() {
		marauroa.clientFramework.onCreateCharacterNack(this.charname, this.reason);
	}

	
	// handle unexpected unknown messages
	this.unknownMessage = function() {
		// do nothing
		console.log("Unknown message: " + JSON.stringify(this));
	}

	this.addDispatchMethod = function(msg) {
		if (typeof(marauroa.messageFactory["t" + msg.t]) != "undefined") {
			msg.dispatch = marauroa.messageFactory["t" + msg.t];
		} else {
			msg.dispatch = marauroa.messageFactory.unknownMessage;
		}
	}
}
