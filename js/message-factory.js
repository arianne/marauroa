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
		temp = "Character List: ";
		for (i in this.characters) {
			temp += this.characters[i].nick;
		}
		return temp;
	}

	// Message S2C ChoooseCharacterACK
	this.t10 = function() {
		return "Entering world";
	}

	// Message S2C Perception
	this.t19 = function() {
		var temp = "";
		if (this.aO) {
			for (i in this.aO) {
				if (this.aO[i].from) {
					temp += this.aO[i].from + ": " + this.aO[i].text + "<br>"
				}
			}
		}
		return temp;
	}
}
