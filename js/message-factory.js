/* $Id: INetworkServerManager.java,v 1.12 2007/12/04 20:00:10 martinfuchs Exp $ */
/***************************************************************************
 *                   (C) Copyright 2010-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
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
		marauroa.log.debug("Entering world");
	}

	// Message S2C ChoooseCharacterNACK
	this.t11 = function() {
		marauroa.log.debug("Character selection rejected");
		marauroa.clientFramework.onChooseCharacterNack();
	}

	// Message S2C Perception
	this.t19 = function() {
		marauroa.clientFramework.onPerception(this);
	}

	// Message S2C Transfer
	this.t21 = function() {
		alert(this);
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

	// handle unexpected unknown messages
	this.unknownMessage = function() {
		// do nothing
		marauroa.log.debug("Unknown message: " + JSON.stringify(this));
	}

	this.addDispatchMethod = function(msg) {
		if (typeof(marauroa.messageFactory["t" + msg.t]) != "undefined") {
			msg.dispatch = marauroa.messageFactory["t" + msg.t];
		} else {
			msg.dispatch = marauroa.messageFactory.unknownMessage;
		}
	}
}
