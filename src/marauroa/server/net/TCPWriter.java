package marauroa.server.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.CRC;
import marauroa.common.Log4J;
import marauroa.common.Pair;
import marauroa.common.net.Message;
import marauroa.common.net.OutputSerializer;
import marauroa.server.game.Statistics;

import org.apache.log4j.Logger;

/** A wrapper class for sending messages to clients */
class TCPWriter {
	private static Logger logger = Logger.getLogger(TCPWriter.class);
	private NetworkServerManagerCallback networkServerManagerCallback = null;
	private Statistics stats = null;
	private List<Pair<Socket, byte[]>> queue = null;

	/**
	 * Creates a NetworkServerManagerWrite
	 *
	 * @param networkServerManager NetworkServerManager
	 * @param stats Statistics
	 */
	public TCPWriter(NetworkServerManagerCallback networkServerManager, Statistics stats) {
		this.networkServerManagerCallback = networkServerManager;
		this.stats = stats;
		this.queue = Collections.synchronizedList(new LinkedList<Pair<Socket, byte[]>>());
		TCPWriterTimeoutThread tcpWriterTimeoutThread = new TCPWriterTimeoutThread(networkServerManagerCallback, queue);
		tcpWriterTimeoutThread.start();
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
        long timeStart = System.currentTimeMillis();
		try {
			if (networkServerManagerCallback.isStillRunning()) {
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
				synchronized (queue) {
					queue.add(new Pair(socket, data));
				}
				logger.debug("Sending packet(" + used_signature + ") " + buffer.length);
				if (logger.isDebugEnabled()) {
					logger.debug("Sending message: " + msg);
				}
			}
			Log4J.finishMethod(logger, "write");
		} catch (Exception e) {
			/* Report the exception */
			logger.info("error while sending a packet (msg=(" + msg + "))", e);
			networkServerManagerCallback.internalDisconnectClientNow(new InetSocketAddress(socket.getInetAddress(), socket.getPort()));
		}
        long timeEnd = System.currentTimeMillis();
        if (timeEnd - timeStart > 1000) {
            logger.warn("TCPWriter.write took " + (timeEnd - timeStart) + " (" + socket.getInetAddress() + ")");
        }
	}

	/**
	 * Thread, which writes the data. Because of some undocumented reason
	 * socket.getOutputStream().write() can block so we put it in its own
	 * thread and have a second one to monitor and restart this.
	 */
	public static class TCPWriterThread extends Thread {
		private Logger logger = Logger.getLogger(TCPWriterThread.class);
		private NetworkServerManagerCallback networkServerManagerCallback = null;
		private List<Pair<Socket, byte[]>> queue = null;
		private long watchdog = System.currentTimeMillis();
		private Socket possibleBadSocket = null;
		private boolean keepRunning = true;

		/**
		 * Creates a TCPWriterThread
		 * 
		 * @param networkServerManagerCallback NetworkServerManagerCallback
		 * @param queue event queue
		 */
		public TCPWriterThread (NetworkServerManagerCallback networkServerManagerCallback, List<Pair<Socket, byte[]>> queue) {
			super("TCPWriterThread");
			super.setDaemon(true);

			this.networkServerManagerCallback = networkServerManagerCallback;
			this.queue = queue;
		}

		/**
		 * Returns the timestamp this thread was last seen alive.
		 *
		 * @return watchdog
		 */
		public long getWatchdogTime() {
			return watchdog;
		}

		/**
		 * Removes the possibl-bad-socket from the queue and closes it.
		 */
		@SuppressWarnings("cast")
		public void kill() {
			Socket mySocket =  possibleBadSocket;
			if (mySocket != null) {
				logger.error("Killing " + mySocket.getInetAddress());
				networkServerManagerCallback.internalDisconnectClientNow(new InetSocketAddress(mySocket.getInetAddress(), mySocket.getPort()));
				synchronized (queue) {
					Iterator<Pair<Socket, byte[]>> itr = queue.iterator();
					while (itr.hasNext()) {
						Pair<Socket, byte[]> pair = (Pair<Socket, byte[]>) itr.next();
						if (pair.first().equals(mySocket)) {
							itr.remove();
						}
					}
				}
			}
			keepRunning = false;
		}

		@Override
		public void run() {
			while (keepRunning) {
				watchdog = System.currentTimeMillis();

				// try to remove the first element
				Pair<Socket, byte[]> pair = null;
				synchronized (queue) {
					if (queue.size() > 0) {
						pair = queue.remove(0);
					}
				}

				// if we removed an item from the queue, we will process it here
				if (pair != null) {
					try  {
						possibleBadSocket = pair.first();
						OutputStream os = pair.first().getOutputStream();
						os.write(pair.second());
						os.flush();
						possibleBadSocket = null;
					} catch (Exception e) {
						/* Report the exception */
						logger.info("error while sending a packet (to " + pair.first().getInetAddress() + ")", e);
						networkServerManagerCallback.internalDisconnectClientNow(new InetSocketAddress(pair.first().getInetAddress(), pair.first().getPort()));
					}

				} else {
					// otherwise we wait
					networkServerManagerCallback.internalDisconnectClientsNow();

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						logger.error(e, e);
					}
				}
			}
		}
	}



	/**
	 * Thread, which writes the data. Because of some undocumented reason
	 * socket.getOutputStream().write() can block so we put it in its own
	 * thread and have a second one to monitor and restart this.
	 */
	public static class TCPWriterTimeoutThread extends Thread {
		private static Logger logger = Logger.getLogger(TCPWriterThread.class);
		private NetworkServerManagerCallback networkServerManagerCallback = null;
		private List<Pair<Socket, byte[]>> queue = null;
		private TCPWriterThread tcpWriterThread = null;

		/**
		 * Creates a TCPWriterTimeoutThread
		 * 
		 * @param networkServerManagerCallback NetworkServerManagerCallback
		 * @param queue event queue
		 */
		public TCPWriterTimeoutThread (NetworkServerManagerCallback networkServerManagerCallback, List<Pair<Socket, byte[]>> queue) {
			super("TCPWriterTimeoutThread");
			super.setDaemon(true);

			this.networkServerManagerCallback = networkServerManagerCallback;
			this.queue = queue;
		}

		private void createTCPWriterThread () {
			tcpWriterThread = new TCPWriterThread(networkServerManagerCallback, queue);
			tcpWriterThread.start();
		}

		@Override
		public void start() {
			createTCPWriterThread();
			super.start();
		}

		@Override
		public void run() {
			while (true) {
				if (System.currentTimeMillis() - tcpWriterThread.getWatchdogTime() >= 2000) {
					logger.error("TCPWriterThread is not responding, killing and restarting it");
					tcpWriterThread.kill();
					try {
						// i am not sure if this works because the blocking is in native code
						tcpWriterThread.stop();
					} catch (Exception e) {
						logger.warn(e, e);
					}
					createTCPWriterThread();
				}
	
				// Wait
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.error(e, e);
				}
			}
		}
	}

}
