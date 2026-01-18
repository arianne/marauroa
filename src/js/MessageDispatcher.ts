/***************************************************************************
 *                   (C) Copyright 2010-2026 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

import { ClientFramework } from "./ClientFramework";

export class MessageDispatcher {

	constructor(private clientFramework: ClientFramework) {
		// empty
	}

	dispatchMessage(msg: any) {
		switch (parseInt(msg.t, 10)) {

			case 14: { // Message S2C Login NACK
				this.clientFramework.onLoginFailed(msg["reason"], msg["text"]);
				break;
			}


			case 9: { // Message S2C CharacterList
				this.clientFramework.onAvailableCharacterDetails(msg["characters"]);
				break;
			}


			case 10: { // Message S2C ChoooseCharacterACK
				console.log("Entering world");
				break;
			}


			case 11: { // Message S2C ChoooseCharacterNACK
				console.log("Character selection rejected");
				this.clientFramework.onChooseCharacterNack();
				break;
			}


			case 13: { // Message S2C Login ACK
				this.clientFramework.onPreviousLogins(msg["previousLogins"]);
				break;
			}


			case 15: { // Message S2C Send Key
				let config: Record<string, string> = {};
				for (let entry of msg["config"]) {
					let pos = entry.indexOf("=");
					config[entry.substring(0, pos).trim()] = entry.substring(pos + 1).trim();
				}
				this.clientFramework.onLoginRequired(config);
				break;
			}


			case 19: { // Message S2C Perception
				this.clientFramework.onPerception(msg);
				break;
			}


			case 20: { // Message S2C Server Info
				this.clientFramework.onServerInfo(msg["contents"]);
				break;
			}


			case 21: { // Message S2C Transfer
				this.clientFramework.onTransfer(msg["contents"]);
				break;
			}


			case 22: { // Message S2C TransferREQ
				this.clientFramework.onTransferREQ(msg["contents"]);
				let contents: any = {};
				for (let i in msg["contents"]) {
					if (typeof (msg["contents"][i]["ack"]) != "undefined" && msg["contents"][i]["ack"]) {
						contents[msg["contents"][i]["name"]] = true;
					} else {
						contents[msg["contents"][i]["name"]] = false;
					}
				}
				let msg2 = {
					"t": "7",
					"contents": contents
				}
				this.clientFramework.sendMessage(msg2);
				break;
			}


			case 24: { // Message S2C CreateAccount ACK
				this.clientFramework.onCreateAccountAck(msg["username"]);
				break;
			}


			case 25: { // Message S2C CreateAccount NACK
				this.clientFramework.onCreateAccountNack(msg["username"], msg["reason"]);
				break;
			}


			case 27: { // Message S2C CreateCharacter ACK
				this.clientFramework.onCreateCharacterAck(msg["charname"], msg["template"]);
				break;
			}


			case 28: { // Message S2C CreateCharacter NACK
				this.clientFramework.onCreateCharacterNack(msg["charname"], msg["reason"]);
				break;
			}


			case 35: { // Message S2C Update
				let msg2 = {
					"t": "36",
					"response": (0, eval)(msg["update"])
				}
				this.clientFramework.sendMessage(msg2);
				break;
			}

			// handle unexpected unknown messages
			default: {
				// do nothing
				console.log("Unknown message: " + JSON.stringify(msg));
			}

		}
	}
}
