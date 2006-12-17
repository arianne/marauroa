package marauroa.server.net.nio;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;



public class Worker implements Runnable, IWorker {
	private List<DataEvent> queue = new LinkedList<DataEvent>();
	private NioServer server;
	
	/* (non-Javadoc)
	 * @see IWorker#onConnect(java.nio.channels.SocketChannel)
	 */
	public void onConnect(SocketChannel socket) {		
	}
	
	/* (non-Javadoc)
	 * @see IWorker#onDisconnect(java.nio.channels.SocketChannel)
	 */
	public void onDisconnect(SocketChannel socket) {		
	}
	
	/* (non-Javadoc)
	 * @see IWorker#onData(NioServer, java.nio.channels.SocketChannel, byte[], int)
	 */
	public void onData(NioServer server, SocketChannel socket, byte[] data, int count) {
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0, count);
		synchronized(queue) {
			queue.add(new DataEvent(socket, dataCopy));
			queue.notify();
		}
	}
	
	public void setServer(NioServer server) {
		this.server=server;
	}

	public void run() {
		DataEvent dataEvent;
		
		while(true) {
			// Wait for data to become available
			synchronized(queue) {
				while(queue.isEmpty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
					}
				}
				dataEvent = (DataEvent) queue.remove(0);
			}
			
			// Return to sender
			server.send(dataEvent.channel, dataEvent.data);
		}
	}
}
