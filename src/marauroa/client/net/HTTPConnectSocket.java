/* $Id: HTTPConnectSocket.java,v 1.6 2009/12/29 00:12:48 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;

/**
 * See http://www.web-cache.com/Writings/Internet-Drafts/draft-luotonen-web-proxy-tunneling-01.txt
 *
 * @author hendrik
 */
public class HTTPConnectSocket extends Socket {
	private SocketAddress proxy = null;
	private static final int TIMEOUT = 60;
	private static final String HTTP_PREFIX = "HTTP/1.0 xxx";
	private static final byte[] HTTP_SUFFIX = new byte[] {13, 10, 13, 10}; // CR LF CR LF


	/**
	 * Creates a new HTTPConnectSocket
	 *
	 * @param proxy address of http-connect proxy
	 */
	public HTTPConnectSocket(SocketAddress proxy) {
		this.proxy = proxy;
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		super.connect(proxy, timeout);
		setupHttpConnect(endpoint);
	}

	private void setupHttpConnect(SocketAddress endpoint) throws IOException {
		sendConnect(endpoint);
		verifyConnection();
	}

	private void sendConnect(SocketAddress endpoint) throws IOException {
		if (!(endpoint instanceof InetSocketAddress)) {
			throw new IOException("Unkown endpoint object. Exspected InetSocketAddress but got " + endpoint.getClass());
		}
		InetSocketAddress server = (InetSocketAddress) endpoint;

		/*
	 	CONNECT home.netscape.com:443 HTTP/1.0
        User-agent: Mozilla/4.0
        Proxy-authorization: basic dGVzdDp0ZXN0
		*/

		OutputStream os = super.getOutputStream();
		StringBuffer connect = new StringBuffer("CONNECT ");
		connect.append(server.getHostName());
		connect.append(":");
		connect.append(server.getPort());
		connect.append(" HTTP/1.1\r\n");
		
		connect.append("Accept: text/plain,text/html;q=0.9,*/*;q=0.5\r\n");

		// TODO: add proxy authentication here

		connect.append("\r\n");
		os.write(connect.toString().getBytes("UTF-8"));
	}

	/*
	 HTTP/1.0 200 Connection established
    Proxy-agent: Netscape-Proxy/1.1
	 */
	// TODO: methods needs cleanup / rewrite
	private void verifyConnection() throws IOException {
		InputStream is = super.getInputStream();

		// read the status code
		byte[] data = new byte[HTTP_PREFIX.length()];
		if (is.read(data) < data.length) {
			throw new IOException("Unexpected end of stream while reading proxy answer.");
		}

		// verify the server response part 1: Was it a http-connect proxy?
		String answer = new String(data).toUpperCase(Locale.ENGLISH);
		if (!answer.startsWith("HTTP/")) {
			data = new byte[4096];
			int size = is.read(data);
			String error = answer + new String(data, 0, size);
			throw new IOException("Proxy connection failed. It does not seem to be a valid http-proxy because I did not understand this response: " + error);
		}

		// verify the server response part 2: Did the connection succeed?
		if (!answer.endsWith(" 200")) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String error = answer.substring(answer.length() - 3) + br.readLine();
			br.close();
			throw new IOException("Proxy connection failed: " + error);
		}

		// OK, the status code of the proxy was 200 Connection established
		// so now we have to skip until the end of the proxy header (CR LF CR LF)
		long startTime = System.currentTimeMillis();
		int expected = 0;
		while (startTime + TIMEOUT * 1000 > System.currentTimeMillis()) {
			int b = is.read();
			if (b < 0) {
				throw new IOException("Unexpected end of stream while reading proxy preload.");
			}

			// Check whether this byte is expected in the byte sequence
			if (b == HTTP_SUFFIX[expected]) {
				expected++;
			} else {
				expected = 0;
			}

			// Did we find the complete byte sequence?
			if (expected == HTTP_SUFFIX.length) {
				return;
			}
		}

		// TODO: include server response here
		throw new IOException("Timeout while reading proxy preload");
	}

	
}
