/***************************************************************************
 *				(C) Copyright 2003-2010 - The Arianne Project			   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/

package marauroa.tools.protocolanalyser;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * This is a faked socket channel which allows the ProtocolAnalyser
 * to call internal marauroa methods that depend on a SocketChannel
 *
 * @author hendrik
 */
class FakeSocketChannel extends SocketChannel {

	/** the internet address to return */
	private InetAddress address;

	/** the tcp port to return */
	private int port;

	/**
	 * creates a new FakeSocketChannel
	 *
	 * @param address internet address to return
	 * @param port port to return
	 */
	public FakeSocketChannel(InetAddress address, int port) {
		super(null);
		this.address = address;
		this.port = port;
	}

	/**
	 * calculates the hashCode based on address and port
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + port;
		return result;
	}

	/**
	 * compares two FakeSocketChannels base on address and port
	 *
	 * @param obj other FakeSocketChannel to compare this one to
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null){
			return false;
		}
		if (!(obj instanceof FakeSocketChannel)) {
			return false;
		}
		FakeSocketChannel other = (FakeSocketChannel) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

	/**
	 * ignored
	 *
	 * @param remote ignored
	 * @return false
	 */
	@Override
	public boolean connect(SocketAddress remote) throws IOException {
		return false;
	}

	/**
	 * ignored
	 *
	 * @return false
	 */
	@Override
	public boolean finishConnect() throws IOException {
		return false;
	}

	/**
	 * ignored
	 *
	 * @return false
	 */
	@Override
	public boolean isConnected() {
		return false;
	}

	/**
	 * ignored
	 *
	 * @return false
	 */
	@Override
	public boolean isConnectionPending() {
		return false;
	}

	/**
	 * returns 0
	 * 
	 * @param dst ignored
	 * @return 0
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException {
		return 0;
	}

	/**
	 * returns 0
	 * 
	 * @param dsts ignored
	 * @param offset ignored
	 * @param length ignored
	 * @return 0
	 */
	@Override
	public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
		return 0;
	}

	/**
	 * creates a new socket
	 * 
	 * @return socket
	 */
	@Override
	public Socket socket() {
		return new Socket();
	}

	/**
	 * ignores
	 *
	 * @param src ignored
	 * @return 0
	 */
	@Override
	public int write(ByteBuffer src) throws IOException {
		return 0;
	}

	/**
	 * ignores
	 *
	 * @param srcs ignored
	 * @param offset ignored
	 * @param length ignored
	 * @return 0
	 */
	@Override
	public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
		return 0;
	}


	/**
	 * ignored
	 */
	@Override
	protected void implCloseSelectableChannel() throws IOException {
		// do nothing
	}


	/**
	 * ignored
	 *
	 * @param block ignored
	 */
	@Override
	protected void implConfigureBlocking(boolean block) throws IOException {
		// do nothing
	}

}
