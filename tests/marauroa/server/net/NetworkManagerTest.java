/* $Id: NetworkManagerTest.java,v 1.1 2007/10/28 17:59:47 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import marauroa.client.net.TCPNetworkClientManager;
import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.NetConst;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;
import marauroa.server.game.db.JDBCDatabase;
import marauroa.server.game.db.Transaction;
import marauroa.server.net.nio.NIONetworkServerManager;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the character related methods of database access.
 * 
 * @author miguel
 * 
 */
public class NetworkManagerTest {
	private static NIONetworkServerManager netMan;

	private static final int PORT = 3218;

	@BeforeClass	
	public static void createNetworkManager() throws Exception {
		NetConst.tcpPort=PORT;
		
		netMan=new NIONetworkServerManager();
		netMan.start();

		Thread.sleep(2000);
	}
	
	@AfterClass 
	public static void destroyNetworkManager() {
		netMan.finish();
	}
	
	/**
	 * Test that message sent from client to server are recieved correctly.
	 * This test the structure from end to end. Client -> serialize -> net -> deserialize -> Server 
	 */
	@Test
	public void sendMessageC2S() throws IOException {
		TCPNetworkClientManager clientNet=new TCPNetworkClientManager("localhost", PORT);

		RPAction action=new RPAction();
		action.put("test","hello world");
		MessageC2SAction msg=new MessageC2SAction(null, action);
		
		clientNet.addMessage(msg);
		
		MessageC2SAction recv=(MessageC2SAction)netMan.getMessage();
		
		assertEquals(msg.getRPAction(), recv.getRPAction());
	}

}
