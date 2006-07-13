package marauroa.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * THIS IS A EXPERIMENTAL TEST CLASS
 * to help me understand the strange way non-blocking socket-io is done in Java.
 * 
 * @author hendrik
 */
public class ExperimentalServer {
	private Selector acceptSelector = null;

	private ServerSocketChannel ssc = null;

	private final int MAXIN = 1000;

	private final int MAXOUT = 1000;

	class Acceptor implements Runnable { // inner
		public void run() {
			try {
				SocketChannel c = ssc.accept();
				if (c != null) new Handler(acceptSelector, c);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	final class Handler {
		final SocketChannel socket;
		final SelectionKey sk;
		ByteBuffer input = ByteBuffer.allocate(MAXIN);
		ByteBuffer output = ByteBuffer.allocate(MAXOUT);
		static final int READING = 0, SENDING = 1;
		int state = READING;

		public Handler(Selector sel, SocketChannel c) throws IOException {
			socket = c;
			c.configureBlocking(false);
			// Optionally try first read now
			sk = socket.register(sel, 0);
			sk.attach(this);
			sk.interestOps(SelectionKey.OP_READ);
			sel.wakeup();
		}
		/*boolean inputIsComplete() { }
		 boolean outputIsComplete() { }
		 void process()              {  }*/
	}

	//	 Accept connections for current time. Lazy Exception thrown.
	private void acceptConnections(int port) throws Exception {
		// Selector for incoming time requests
		acceptSelector = SelectorProvider.provider().openSelector();

		// Create a new server socket and set to non blocking mode
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);

		// Bind the server socket to the local host and port

		InetAddress lh = InetAddress.getLocalHost();
		InetSocketAddress isa = new InetSocketAddress(lh, port);
		ssc.socket().bind(isa);

		// Register accepts on the server socket with the selector. This
		// step tells the selector that the socket wants to be put on the
		// ready list when accept operations occur, so allowing multiplexed
		// non-blocking I/O to take place.
		SelectionKey acceptKey = ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
		acceptKey.attach(new Acceptor());

		int keysAdded = 0;

		// Here's where everything happens. The select method will
		// return when any operations registered above have occurred, the
		// thread has been interrupted, etc.
		while ((keysAdded = acceptSelector.select()) > 0) {
			// Someone is ready for I/O, get the ready keys
			Set readyKeys = acceptSelector.selectedKeys();
			Iterator i = readyKeys.iterator();

			// Walk through the ready keys collection and process date requests.
			while (i.hasNext()) {
				SelectionKey sk = (SelectionKey) i.next();
				i.remove();
				// The key indexes into the selector so you
				// can retrieve the socket that's ready for I/O
				ServerSocketChannel nextReady = (ServerSocketChannel) sk.channel();
				// Accept the date request and send back the date string
				SocketChannel socketChannel = nextReady.accept();
				Socket s = socketChannel.socket();
				// Write the current time to the socket
				PrintWriter out = new PrintWriter(s.getOutputStream(), true);
				Date now = new Date();
				out.println(now);
				out.close();
			}
		}
	}

	public void run() { // normally in a new Thread
		try {
			while (!Thread.interrupted()) {
				acceptSelector.select();
				Set selected = acceptSelector.selectedKeys();
				Iterator it = selected.iterator();
				while (it.hasNext()) {
					dispatch((SelectionKey) it.next());
				}
				selected.clear();
			}
		} catch (IOException ex) { /* ... */
			ex.printStackTrace();
		}
	}

	void dispatch(SelectionKey k) {
		Runnable r = (Runnable) (k.attachment());
		if (r != null) r.run();
	}


	// Entry point.
	public static void main(String[] args) {
		// Parse command line arguments and
		// create a new time server (no arguments yet)
		try {
			ExperimentalServer nbt = new ExperimentalServer();
			nbt.acceptConnections(12345);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
