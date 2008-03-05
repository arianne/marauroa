/* $Id: Ping.java,v 1.1 2008/03/05 12:15:07 arianne_rpg Exp $ */
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
package marauroa.test.ping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * System test of a ping client/server deployment using Marauroa.
 * @author miguel
 *
 */
public class Ping {
	private static final int PORT = 5122;
	
	private PingClient client;
	
	@Before
	public void create() {
		client=new PingClient("log4j.properties");
	}
	
	@After
	public void destroy() {
		assertFalse("Client can't be null",client==null);
		client.close();
	}
	
	@Test
	public void run() {
		try {
			/*
			 * Connect to server
			 */
	        client.connect("localhost", PORT);
	        
	        /*
	         * Create an account
	         */
			AccountResult account = client.createAccount("testUsername", "password", "email");
			assertTrue("Account creation must not fail", !account.failed());

			assertEquals("testUsername", account.getUsername());
			
			/*
			 * Create a character for that account.
			 */
			RPObject template = new RPObject();
			template.put("state", "idle");

			CharacterResult character = client.createCharacter("testCharacter", template);
			assertEquals(character.getResult(),Result.OK_CREATED);
			assertEquals("testCharacter", character.getCharacter());

			/*
			 * Choose that character.
			 */
			boolean choosen = client.chooseCharacter("testCharacter");
			assertTrue(choosen);
			
			boolean keepRunning=true;
			
			while(keepRunning) {
				client.loop(0);
			}
			
			
        } catch (Exception e) {
        	fail();
	        e.printStackTrace();
        }
		
	}

}
