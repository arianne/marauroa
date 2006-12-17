package marauroa.server.net.nio;

import java.nio.channels.SocketChannel;


class DataEvent {
	public SocketChannel channel;
	public byte[] data;
	
	public DataEvent(SocketChannel socket, byte[] data) {
		this.channel = socket;
		this.data = data;
	}
}