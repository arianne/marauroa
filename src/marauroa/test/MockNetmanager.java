package marauroa.test;

import java.net.InetSocketAddress;

import marauroa.client.net.INetworkClientManagerInterface;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;

public class MockNetmanager implements INetworkClientManagerInterface {

	public void addMessage(Message msg) {
		// TODO Auto-generated method stub

	}

	public void finish() {
		// TODO Auto-generated method stub

	}

	public InetSocketAddress getAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getConnectionState() {
		// TODO Auto-generated method stub
		return false;
	}

	public Message getMessage(int timeout) throws InvalidVersionException {
		// TODO Auto-generated method stub
		return null;
	}

}
