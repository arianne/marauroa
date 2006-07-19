package marauroa.server.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;

/** 
 * The active thread in charge of recieving messages from the network.
 */
class TCPReader extends Thread {
	private static Logger logger = Logger.getLogger(TCPReader.class);
	private NetworkServerManagerCallback networkServerManager = null;
	
	private HashMap<InetSocketAddress, Socket> tcpSockets = null;
	private Map<Socket, Integer> bytesToRead = new WeakHashMap<Socket, Integer>();
	private Statistics stats = null;

	/**
	 * Creates a NetworkServerManagerRead
	 *
	 * @param networkServerManager NetworkServerManager 
	 * @param tcpSockets communication end-points
	 * @param stats Statistics
	 */
	public TCPReader(NetworkServerManagerCallback networkServerManager, HashMap<InetSocketAddress, Socket> tcpSockets, Statistics stats) {
		super("NetworkServerManagerRead");
		this.networkServerManager = networkServerManager;
		this.tcpSockets = tcpSockets;
		this.stats = stats;
	}

	/** 
	 * Method that execute the reading. It runs as a active thread forever.
	 */
	@Override
	public void run() {
		logger.debug("run()");
		byte[] sizebuffer = new byte[4];
		
		while (networkServerManager.isStillRunning()) {
			boolean found = false;
			long start = System.currentTimeMillis();
			Map<InetSocketAddress, Socket> temptTcpSockets = (Map<InetSocketAddress, Socket>) tcpSockets.clone();
			for (InetSocketAddress inetSocketAddress : temptTcpSockets.keySet()) {
				Socket socket = temptTcpSockets.get(inetSocketAddress);
				if (socket.isClosed()) {
					networkServerManager.disconnectClient(inetSocketAddress);
					continue;
				}
				if (!socket.isConnected()) {
					networkServerManager.disconnectClient(inetSocketAddress);
					continue;
				}
				try {
					InputStream is = socket.getInputStream();
					
					Integer toReadInt = bytesToRead.get(socket);
					int size = -1;
					if (toReadInt == null) {
						// read size
						if (is.available() >= 4) {
							is.read(sizebuffer);
							size = (sizebuffer[0] & 0xFF)
								+ ((sizebuffer[1] & 0xFF) << 8)
								+ ((sizebuffer[2] & 0xFF) << 16)
								+ ((sizebuffer[3] & 0xFF) << 24);
							System.err.println(size);
							if (size == 542393671) { // "GET "
								// This request was not created by the marauroa-client
								// but it was created by the HTTP-client to compare
								// version numbers. ==> Close it.
								OutputStream os = socket.getOutputStream();
								os.write("500 This is not a webserver\r\n\r\n".getBytes());
								os.flush();
								os.close();
								logger.warn("Closing connection because packet-size is magic-number \"GET \".");
								networkServerManager.disconnectClient(inetSocketAddress);
								continue;
							}
							bytesToRead.put(socket, new Integer(size));
							found = true;
						}
					} else {
						size = toReadInt.intValue();
					}

					if ((size > -1) && (is.available() >= size)) {
						found = true;
						byte[] buffer = new byte[size];
						is.read(buffer);
						logger.debug("Received TCP Packet");
		
						/*** Statistics ***/
						stats.add("Bytes recv", size);
						stats.add("Message recv", 1);
		
						networkServerManager.receiveMessage(buffer, inetSocketAddress);
						bytesToRead.remove(socket);
					}
				} catch (java.net.SocketTimeoutException e) {
					logger.warn(e, e);
					networkServerManager.disconnectClient(inetSocketAddress);
				} catch (IOException e) {
					logger.warn(e, e);
					networkServerManager.disconnectClient(inetSocketAddress);
				} catch (Exception e) {
					/* Report the exception */
					logger.error("error while processing tcp-packets", e);
				}
			}

			if (!found) {
				// sleep
				try {
					long wait = 100 - (System.currentTimeMillis() - start);
					if (wait > 0) {
						Thread.sleep(wait);
					} else {
						wait = wait * -1;
						logger.warn("Turn duration overflow by " + wait + " ms");
					}
				} catch (InterruptedException e) {
					logger.error(e, e);
				}
			}
		}

		networkServerManager.finishedReadThread();
		logger.debug("run() finished");
	}
}
