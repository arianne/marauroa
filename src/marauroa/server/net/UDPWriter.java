package marauroa.server.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import marauroa.common.CRC;
import marauroa.common.Log4J;
import marauroa.common.net.Message;
import marauroa.common.net.NetConst;
import marauroa.common.net.OutputSerializer;
import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;

/** A wrapper class for sending messages to clients */
@Deprecated
class UDPWriter {
	private static Logger logger = Logger.getLogger(UDPWriter.class);

	private NetworkServerManagerCallback networkServerManager = null;

	private DatagramSocket socket = null;

	private Statistics stats = null;

	/**
	 * Creates a NetworkServerManagerWrite
	 * 
	 * @param networkServerManager
	 *            NetworkServerManager
	 * @param socket
	 *            communication end-point
	 * @param stats
	 *            Statistics
	 */
	public UDPWriter(NetworkServerManagerCallback networkServerManager,
			DatagramSocket socket, Statistics stats) {
		this.networkServerManager = networkServerManager;
		this.socket = socket;
		this.stats = stats;
	}

	private byte[] serializeMessage(Message msg) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer s = new OutputSerializer(out);

		s.write(msg);
		return out.toByteArray();
	}

	final private int PACKET_SIGNATURE_SIZE = 4;

	final private int CONTENT_PACKET_SIZE = NetConst.UDP_PACKET_SIZE
			- PACKET_SIGNATURE_SIZE;

	/**
	 * Method that execute the writting
	 * 
	 * @param msg
	 *            Message to write
	 */
	public void write(Message msg) {
		Log4J.startMethod(logger, "write");
		try {
			/* TODO: Looks like hardcoded, write it in a better way */
			if (networkServerManager.isStillRunning()) {
				byte[] buffer = serializeMessage(msg);
				short used_signature;

				/** * Statistics ** */
				used_signature = CRC.cmpCRC(buffer); // ++last_signature;

				stats.add("Bytes send", buffer.length);
				stats.add("Message send", 1);

				logger.debug("Message(" + msg.getType() + ") size in bytes: "
						+ buffer.length);
				int totalNumberOfPackets = (buffer.length / CONTENT_PACKET_SIZE) + 1;
				int bytesRemaining = buffer.length;

				byte[] data = new byte[CONTENT_PACKET_SIZE
						+ PACKET_SIGNATURE_SIZE];

				for (int i = 0; i < totalNumberOfPackets; ++i) {
					int packetSize = CONTENT_PACKET_SIZE;

					if ((CONTENT_PACKET_SIZE) > bytesRemaining) {
						packetSize = bytesRemaining;
					}

					bytesRemaining -= packetSize;

					logger.debug("Packet size: " + packetSize);
					logger.debug("Bytes remaining: " + bytesRemaining);

					data[0] = (byte) totalNumberOfPackets;
					data[1] = (byte) i;
					data[2] = (byte) (used_signature & 255);
					data[3] = (byte) ((used_signature >> 8) & 255);

					System.arraycopy(buffer, CONTENT_PACKET_SIZE * i, data,
							PACKET_SIGNATURE_SIZE, packetSize);

					DatagramPacket pkt = new DatagramPacket(data, packetSize
							+ PACKET_SIGNATURE_SIZE, msg.getAddress());

					socket.send(pkt);
					logger.debug("Sent packet(" + used_signature + ") "
							+ (i + 1) + " of " + totalNumberOfPackets);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Sent message: " + msg);
				}
			}
			Log4J.finishMethod(logger, "write");
		} catch (IOException e) {
			/* Report the exception */
			logger.error("error while sending a packet (msg=(" + msg + "))", e);
		}
	}
}
