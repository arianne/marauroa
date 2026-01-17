import { ClientFramework } from "./ClientFramework";
import { RPObject } from "./RPObject";
import { RPObjectFactory } from "./RPObjectFactory";
import { RPSlotFactory } from "./RPSlotFactory";
import { RPEventFactory } from "./RPEventFactory";


class Marauroa {
	debug = {
		messages: false,
		unknownEvents: true
	};
	clientFramework = new ClientFramework();
	currentZoneName!: string;
	currentZone: any;
	perceptionHandler: any;
	perceptionListener: any;
	rpobjectFactory = new RPObjectFactory();
	rpslotFactory = new RPSlotFactory();
	rpeventFactory = new RPEventFactory();
	me?: RPObject;
}

export const marauroa = new Marauroa();