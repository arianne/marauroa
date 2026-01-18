/***************************************************************************
 *                   (C) Copyright 2003-2026 - Marauroa                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { marauroa } from "./Marauroa";

export class RPZone {
	[key: string]: any;

	clear() {
		for (let i in marauroa.currentZone) {
			if (marauroa.currentZone.hasOwnProperty(i) && typeof(marauroa.currentZone[i]) !== "function") {
				marauroa.currentZone[i].destroy(marauroa.currentZone);
				delete marauroa.currentZone[i];
			}
		}
	}
}