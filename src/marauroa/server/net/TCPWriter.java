// ********************************************************
//                   EXPERIMENTAL TCP-SUPPORT 
// ********************************************************
package marauroa.server.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import marauroa.common.CRC;
import marauroa.common.Log4J;
import marauroa.common.net.Message;
import marauroa.common.net.OutputSerializer;
import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;

/** A wrapper class for sending messages to clients */
class TCPWriter {
	private static Logger logger = Logger.getLogger(TCPWriter.class);
	private NetworkServerManagerCallback networkServerManager = null;
	private Statistics stats = null;

	/**
	 * Creates a NetworkServerManagerWrite
	 *
	 * @param networkServerManager NetworkServerManager
	 * @param stats Statistics
	 */
	public TCPWriter(NetworkServerManagerCallback networkServerManager, Statistics stats) {
		this.networkServerManager = networkServerManager;
		this.stats = stats;
	}

	private byte[] serializeMessage(Message msg) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer s = new OutputSerializer(out);

		s.write(msg);
		return out.toByteArray();
	}

	final private int PACKET_SIGNATURE_SIZE = 4;
	final private int PACKET_LENGTH_SIZE = 4;

	/**
	 * Method that execute the writting
	 *
	 * @param msg Message to write
	 * @param socket Socket
	 */
	public void write(Message msg, Socket socket) {
		Log4J.startMethod(logger, "write");
		try {
			if (networkServerManager.isStillRunning()) {
				byte[] buffer = serializeMessage(msg);
				short used_signature;

				/*** Statistics ***/
				used_signature = CRC.cmpCRC(buffer); //++last_signature;

				stats.add("Bytes send", buffer.length);
				stats.add("Message send", 1);

				logger.debug("Message(" + msg.getType() + ") size in bytes: " + buffer.length);

				byte[] data = new byte[PACKET_LENGTH_SIZE + PACKET_SIGNATURE_SIZE + buffer.length];
				int size = buffer.length + PACKET_SIGNATURE_SIZE;
				data[0] = (byte) (size & 255);
				data[1] = (byte) ((size >>  8) & 255);
				data[2] = (byte) ((size >> 16) & 255);
				data[3] = (byte) ((size >> 24) & 255);
				data[4] = (byte) 1;
				data[5] = (byte) 0;
				data[6] = (byte) (used_signature & 255);
				data[7] = (byte) ((used_signature >> 8) & 255);
				logger.debug("data size: " + buffer.length);

				// don't use multiple os.write calls because we have
				// disabled Nagel's algorithm.
				System.arraycopy(buffer, 0, data, PACKET_LENGTH_SIZE + PACKET_SIGNATURE_SIZE, buffer.length);
				
				OutputStream os = socket.getOutputStream();
				os.write(data);
				os.flush();
				logger.debug("Sent packet(" + used_signature + ") " + buffer.length);

				if (logger.isDebugEnabled()) {
					logger.debug("Sent message: " + msg);
				}
			}
			Log4J.finishMethod(logger, "write");
		} catch (IOException e) {
			/* Report the exception */
			logger.error("error while sending a packet (msg=(" + msg + "))", e);
			networkServerManager.disconnectClient(new InetSocketAddress(socket.getInetAddress(), socket.getPort()));
		}
	}
}
