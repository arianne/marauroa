package marauroa.functional;

import java.io.IOException;

import marauroa.common.Configuration;
import marauroa.server.marauroad;

public class MarauroadLauncher extends marauroad {

	public MarauroadLauncher(int port) {
		super();
		try {
			Configuration.getConfiguration().set("tcp_port", Integer.toString(port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
