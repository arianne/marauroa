package marauroa.functional;

import marauroa.common.net.NetConst;
import marauroa.server.marauroad;

public class MarauroadLauncher extends marauroad {

	public MarauroadLauncher(int port) {
		super();
		NetConst.tcpPort=port;
	}

}
