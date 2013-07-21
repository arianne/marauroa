/***************************************************************************
 *                (C) Copyright 2003-2013 - Faiumoni e. V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLEngine;

/**
 * an ssl attachment
 *
 * @author hendrik
 */
public class SslAttachment {

	private SSLEngine sslEngine;
	private final SocketChannel socketChannel;

	/**
	 * ssl attachment
	 *
	 * @param socketChannel socketChannel
	 */
	public SslAttachment(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	/**
	 * activates SSL
	 *
	 * @param sslHelper SslHelper
	 */
	public void activateSsl(SslHelper sslHelper) {

		try {
			sslEngine = sslHelper.createSSLEngine();


		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * reads from the channel
	 *
	 * @param buffer output buffer
	 * @return number of bytes read
	 * @throws IOException in case of an input/output error
	 */
	public int read(ByteBuffer buffer) throws IOException {
		return socketChannel.read(buffer);
	}

	/**
	 * write to the cannel
	 *
	 * @param data to write
	 * @return number of bytes written
	 * @throws IOException in case of an input/output error
	 */
	int write(ByteBuffer data) throws IOException {
		return socketChannel.write(data);
	}

}
