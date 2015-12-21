/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net;

import marauroa.common.Configuration;
import marauroa.server.net.nio.NIONetworkConnectionManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test the character related methods of database access.
 *
 * @author miguel
 *
 */
public class NetworkManagerTest {
	private static NIONetworkConnectionManager netMan;

	private static final int PORT = 3215;

	@BeforeClass
	public static void createNetworkManager() throws Exception {
		Configuration.getConfiguration().set("tcp_port", Integer.toString(PORT));

		netMan = new NIONetworkConnectionManager(null);
		netMan.start();

		Thread.sleep(2000);
	}

	@AfterClass
	public static void destroyNetworkManager() {
		if (netMan != null)
			netMan.finish();
	}

	/**
	 * Test that message sent from client to server are received correctly.
	 * This test the structure from end to end. Client -> serialize -> net -> deserialize -> Server
	 */
/* TODO
	@Test
	public void sendMessageC2S() throws IOException {
		TCPNetworkClientManager clientNet = new TCPNetworkClientManager("localhost", PORT);

		RPAction action = new RPAction();
		action.put("test", "hello world");
		MessageC2SAction msg = new MessageC2SAction(null, action);

		clientNet.addMessage(msg);

		MessageC2SAction recv = (MessageC2SAction)netMan.getMessage();

		assertEquals(msg.getRPAction(), recv.getRPAction());
	}
*/

	/**
	 * Test that message sent from client to server and server to client are received correctly.
	 * This test the structure from end to end. Client -> serialize -> net -> deserialize -> Server
	 * @throws InvalidVersionException
	 */
/* TODO
	@Test
	public void sendMessageS2C() throws IOException, InvalidVersionException {
		TCPNetworkClientManager clientNet=new TCPNetworkClientManager("localhost", PORT);

		RPAction action=new RPAction();
		action.put("test","hello world");
		MessageC2SAction msg=new MessageC2SAction(null, action);

		clientNet.addMessage(msg);

		MessageC2SAction recv=(MessageC2SAction)netMan.getMessage();

		assertEquals(msg.getRPAction(), recv.getRPAction());

		RPAction reply=new RPAction();
		reply.put("test","world ok");

		netMan.sendMessage(new MessageC2SAction(recv.getChannel(), reply));

		MessageC2SAction msgReply=null;
		while(msgReply==null) {
			msgReply=(MessageC2SAction) clientNet.getMessage(100);
		}

		assertNotNull(msgReply);
		assertEquals(reply, msgReply.getRPAction());
	}
*/

	/**
	 * Testing what happens when client send nothing to server, but in fact send a TCP packet.
	 */
/* TODO
	@Test
	public void sendMessageNull() throws IOException {
		Socket socket=new Socket("localhost", PORT);
		OutputStream out=socket.getOutputStream();

		out.write(new byte[0]);
		out.flush();
		socket.close();

		sendMessageC2S();
	}
*/

	/**
	 * Testing what happens when client send just one byte to server, but in fact send a TCP packet.
	 */
/* TODO
	@Test
	public void sendMessageOneByte() throws IOException {
		Socket socket=new Socket("localhost", PORT);
		OutputStream out=socket.getOutputStream();

		out.write(new byte[1]);
		out.flush();
		socket.close();

		sendMessageC2S();
	}
*/
	/**
	 * Testing what happens when client send rubbish to server.
	 */
/* TODO
	@Test
	public void sendMessageRubish() throws IOException {
		// Send 256 rubbish messages, just to make sure.
		for (int j = 0; j < 256; j++) {
			Socket socket = new Socket("localhost", PORT);
			OutputStream out = socket.getOutputStream();

			byte[] tmp = new byte[1024];
			for (int i = 0; i < 1024; i++) {
				tmp[i] = (byte) (255 * Math.random());
			}

			out.write(tmp);
			out.flush();
			socket.close();

			sendMessageC2S();
		}
	}
*/
}
