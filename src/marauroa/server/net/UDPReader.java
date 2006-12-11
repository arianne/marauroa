package marauroa.server.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import marauroa.common.net.NetConst;
import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;

/**
 * The active thread in charge of recieving messages from the network.
 */
class UDPReader extends Thread {
	private static Logger logger = Logger.getLogger(UDPReader.class);

	private NetworkServerManagerCallback networkServerManager = null;

	private DatagramSocket socket = null;

	private Statistics stats = null;

	/**
	 * Creates a NetworkServerManagerRead
	 * 
	 * @param networkServerManager
	 *            NetworkServerManager
	 * @param socket
	 *            communication end-point
	 * @param stats
	 *            Statistics
	 */
	public UDPReader(NetworkServerManagerCallback networkServerManager,	DatagramSocket socket, Statistics stats) {
		super("UDPReader");
		this.networkServerManager = networkServerManager;
		this.socket = socket;
		this.stats = stats;
	}

	/**
	 * Method that execute the reading. It runs as a active thread forever.
	 */
	@Override
	public void run() {
		logger.debug("run()");
		while (networkServerManager.isStillRunning()) {
			byte[] buffer = new byte[NetConst.UDP_PACKET_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			try {
				socket.receive(packet);
				logger.debug("Received UDP Packet");

				/** * Statistics ** */
				stats.add("Bytes recv", packet.getLength());
				stats.add("Message recv", 1);

				networkServerManager.receiveMessage(packet.getData(),(InetSocketAddress) packet.getSocketAddress());
			} catch (java.net.SocketTimeoutException e) {
				/*
				 * We need the thread to check from time to time if user has
				 * requested an exit
				 */
			} catch (Throwable e) {
				/* Report the exception */
				logger.error("error while processing udp-packets", e);
			}
		}

		networkServerManager.finishedReadThread();
		logger.debug("run() finished");
	}
}
