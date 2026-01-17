import { ClientFramework } from "./ClientFramework";

class Marauroa {
	debug!: {
		messages: false,
		unknownEvents: true
	}
	clientFramework!: ClientFramework;
	currentZoneName!: string;
	currentZone: any;
	perceptionHandler: any;
	perceptionListener: any;
	rpobjectFactory: any;
	rpslotFactory: any;
	rpeventFactory: any;
	me: any;
}

export const marauroa = new Marauroa();