package marauroa.server.net.nio;

import java.nio.channels.SocketChannel;


public interface IWorker {
	public abstract void setServer(NioServer server);

	public abstract void onConnect(SocketChannel socket);

	public abstract void onDisconnect(SocketChannel socket);

	public abstract void onData(NioServer server, SocketChannel socket,
			byte[] data, int count);

}