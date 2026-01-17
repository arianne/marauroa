
class Marauroa {
	debug: {
		messages: false,
		unknownEvents: true
	}
	currentZone: any;
	perceptionHandler: any;
	perceptionListener: any;
	currentZoneName: string;
	rpobjectFactory: any;
	rpslotFactory: any;
	rpeventFactory: any;
	me: any;
}

export const marauroa = new Marauroa();