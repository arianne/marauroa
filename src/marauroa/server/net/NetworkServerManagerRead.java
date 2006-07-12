package marauroa.server.net;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.Message;
import marauroa.common.net.MessageS2CInvalidMessage;
import marauroa.common.net.NetConst;

/** 
 * The active thread in charge of recieving messages from the network.
 */
class NetworkServerManagerRead extends Thread {
	private static Logger logger = Logger.getLogger(NetworkServerManagerRead.class);
	private NetworkServerManager networkServerManager = null;

	/**
	 * Creates a NetworkServerManagerRead
	 *
	 * @param networkServerManager NetworkServerManager 
	 */
	public NetworkServerManagerRead(NetworkServerManager networkServerManager) {
		super("NetworkServerManagerRead");
		this.networkServerManager = networkServerManager;
	}

	/** 
	 * Method that execute the reading. It runs as a active thread forever.
	 */
	@Override
	public void run() {
		logger.debug("run()");
		while (networkServerManager.keepRunning) {
			byte[] buffer = new byte[NetConst.UDP_PACKET_SIZE];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			try {
				networkServerManager.socket.receive(packet);
				logger.debug("Received UDP Packet");

				/*** Statistics ***/
				networkServerManager.stats.add("Bytes recv", packet.getLength());
				networkServerManager.stats.add("Message recv", 1);

				if (!networkServerManager.packetValidator.checkBanned(packet)) {
					try {
						Message msg = networkServerManager.msgFactory.getMessage(packet.getData(), (InetSocketAddress) packet.getSocketAddress());
						logger.debug("Received message: " + msg.toString());
						networkServerManager.messages.add(msg);
						networkServerManager.newMessageArrived();
					} catch (InvalidVersionException e) {
						networkServerManager.stats.add("Message invalid version", 1);
						MessageS2CInvalidMessage msg = new MessageS2CInvalidMessage((InetSocketAddress) packet.getSocketAddress(), "Invalid client version: Update client");
						networkServerManager.addMessage(msg);
					}
				} else {
					logger.debug("UDP Packet discarded - client(" + packet + ") is banned.");
				}
			} catch (java.net.SocketTimeoutException e) {
				/* We need the thread to check from time to time if user has requested
				 * an exit */
			} catch (Throwable e) {
				/* Report the exception */
				logger.error("error while processing udp-packets", e);
			}
		}

		networkServerManager.isfinished = true;
		logger.debug("run() finished");
	}
}
