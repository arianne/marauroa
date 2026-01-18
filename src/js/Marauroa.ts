import { ClientFramework } from "./ClientFramework";
import { RPObject } from "./RPObject";
import { RPObjectFactory } from "./RPObjectFactory";
import { RPSlotFactory } from "./RPSlotFactory";
import { RPEventFactory } from "./RPEventFactory";
import { PerceptionHandler, PerceptionListener } from "./Perception";
import { RPZone } from "./RPZone";


class Marauroa {
	debug = {
		messages: false,
		unknownEvents: true
	};
	clientFramework = new ClientFramework();
	currentZoneName!: string;
	currentZone = new RPZone();
	perceptionHandler = new PerceptionHandler();
	perceptionListener = new PerceptionListener();
	rpobjectFactory = new RPObjectFactory();
	rpslotFactory = new RPSlotFactory();
	rpeventFactory = new RPEventFactory();
	me!: RPObject; // TODO: This should be optional "me?"
}

export const marauroa = new Marauroa();