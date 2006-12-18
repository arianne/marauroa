package marauroa.server.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;

/** 
 * The active thread in charge of recieving messages from the network.
 */
@Deprecated
class TCPReader extends Thread {
	private static Logger logger = Logger.getLogger(TCPReader.class);
	private NetworkServerManagerCallback networkServerManager = null;
	
	private Map<InetSocketAddress, Socket> tcpSockets = null;
	private Map<Socket, Integer> bytesToRead = new WeakHashMap<Socket, Integer>();
	private Statistics stats = null;

	/**
	 * Creates a NetworkServerManagerRead
	 *
	 * @param networkServerManager NetworkServerManager 
	 * @param tcpSockets communication end-points
	 * @param stats Statistics
	 */
	public TCPReader(NetworkServerManagerCallback networkServerManager, Map<InetSocketAddress, Socket> tcpSockets, Statistics stats) {
		super("TCPReader");
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
			
			// clone tcpSockets so that we do not lock it to long
			Map<InetSocketAddress, Socket> temptTcpSockets = cloneMap(tcpSockets);
			
			for (InetSocketAddress inetSocketAddress : temptTcpSockets.keySet()) {
				Socket socket = temptTcpSockets.get(inetSocketAddress);
				if (socket.isClosed()) {
					networkServerManager.internalDisconnectClientNow(inetSocketAddress);
					continue;
				}
				if (!socket.isConnected()) {
					networkServerManager.internalDisconnectClientNow(inetSocketAddress);
					continue;
				}
				try {
					InputStream is = socket.getInputStream();
					
					Integer toReadInt = bytesToRead.get(socket);
					int size = -1;
					if (toReadInt == null) {
						// read size
						if (is.available() >= 4) {
							if (is.read(sizebuffer) < 4) {
								// this should never happen because of the
								// is.available() right above. But to make 
								// findbugs happy i put some error handling here.
								logger.error("Expected 4 bytes but did not get them", new Throwable());
							}
							size = (sizebuffer[0] & 0xFF)
								+ ((sizebuffer[1] & 0xFF) << 8)
								+ ((sizebuffer[2] & 0xFF) << 16)
								+ ((sizebuffer[3] & 0xFF) << 24);
							
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
				} catch (SocketTimeoutException e) {
					logger.warn(e + " (" + socket.getInetAddress() + ")", e);
					networkServerManager.internalDisconnectClientNow(inetSocketAddress);
                } catch (SocketException e) {
                    logger.warn(e + " (" + socket.getInetAddress() + ")", e);
                    networkServerManager.internalDisconnectClientNow(inetSocketAddress);
				} catch (IOException e) {
					logger.warn(e + " (" + socket.getInetAddress() + ")", e);
					networkServerManager.internalDisconnectClientNow(inetSocketAddress);
				} catch (Exception e) {
					/* Report the exception */
					logger.error("error while processing tcp-packets (" + socket.getInetAddress() + ")", e);
                    networkServerManager.internalDisconnectClientNow(inetSocketAddress);
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

	/**
	 * Clones a map. We cannot use .clone() because synchronized maps
	 * do not implement Cloneable :-/
	 *
	 * @param map map to clone
	 * @return cloned map
	 */
	private Map<InetSocketAddress, Socket> cloneMap(Map<InetSocketAddress, Socket> map) {
		Map<InetSocketAddress, Socket> res = new HashMap<InetSocketAddress, Socket>();
		synchronized (map) {
			for (InetSocketAddress key : map.keySet()) {
				res.put(key, map.get(key));
			}
		}
		return res;
	}
}
