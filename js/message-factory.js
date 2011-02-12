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

	// Message S2C CharacterList
	this.t9 = function() {
		marauroa.clientFramework.onAvailableCharacterDetails(this.characters);
	}

	// Message S2C ChoooseCharacterACK
	this.t10 = function() {
		marauroa.clientFramework.debug("Entering world");
	}

	// Message S2C ChoooseCharacterNACK
	this.t10 = function() {
		marauroa.clientFramework.debug("Character selection rejected");
		marauroa.clientFramework.onChooseCharacterNack();
	}

	// Message S2C Perception
	this.t19 = function() {
		marauroa.clientFramework.onPerception(this);
	}

	function unknownMessage() {
		// do nothing
		debug("Unknown message: " + JSON.stringify(this));
	}

	function addDispatchMethod(msg) {
		if (typeof(messageFactory["t" + msg.t]) != "undefined") {
			msg.dispatch = messageFactory["t" + msg.t];
		} else {
			msg.dispatch = messageFacotry.unknownMessage;
		}
	}
}
