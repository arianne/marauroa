/* $Id: BugAtApplyDifferencesTest.java,v 1.7 2010/01/04 08:47:11 nhnb Exp $ */
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
package marauroa.common.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.Before;
import org.junit.Test;


public class BugAtApplyDifferencesTest {

	private RPObject obj;
	private MarauroaRPZone zone;

	private MarauroaRPZone recreatedZone;
	
	/**
	 * Set up an object and create a zone that will contain it. It doesn't add
	 * the object to the zone.
	 */
	@Before
	public void createObject() {
		obj = new RPObject();

		obj.put("a", 1);
		obj.put("b", "1");
		obj.put("c", 2.0);
		obj.put("d", "string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		RPClassTestHelper.generateRPClasses();
		RPEvent chat = new RPEvent("chat");
		chat.put("text", "Hi there");
		obj.addEvent(chat);

		chat = new RPEvent("chat");
		chat.put("text", "Does this work?");
		obj.addEvent(chat);

		RPSlot lhand = obj.getSlot("lhand");

		RPObject pocket = new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		zone = new MarauroaRPZone("test") {
			@Override
			public void onInit() throws Exception {
				// no database access as it is done in the super method
			}

			@Override
			public void onFinish() throws Exception {
				// no database access as it is done in the super method
			}
		};

		recreatedZone = new MarauroaRPZone("test") {
			@Override
			public void onInit() throws Exception {
				// no database access as it is done in the super method
			}

			@Override
			public void onFinish() throws Exception {
				// no database access as it is done in the super method
			}
		};
	}

	@Test
	public void alongtest() throws IOException, InvalidVersionException {
		/*
		 * Add the object
		 */
		zone.assignRPObjectID(obj);
		zone.add(obj);

		assertTrue(obj.has("id"));
		assertTrue(obj.has("zoneid"));
		assertEquals("test", obj.get("zoneid"));
		
		/*
		 * Create the perception and serialize it.
		 */
		Perception p=zone.getPerception(obj, Perception.SYNC);
		MessageS2CPerception msg=new MessageS2CPerception(null, p);
		Encoder enc=Encoder.get();
		byte[] data=enc.encode(msg);
		
		Decoder dec=Decoder.get();
		List<Message> msgs=dec.decode(null, data);
		
		/*
		 * There should only be one message.
		 */
		assertEquals(1, msgs.size());
		
		MessageS2CPerception recv=(MessageS2CPerception)msgs.get(0);
		assertTrue(recv.getModifiedAddedRPObjects().isEmpty());
		assertTrue(recv.getModifiedDeletedRPObjects().isEmpty());
		assertTrue(recv.getDeletedRPObjects().isEmpty());
		
		List<RPObject> added=recv.getAddedRPObjects();
		assertEquals(1, added.size());
		
		RPObject recreated=added.get(0);
		assertNotNull(recreated);
		recreatedZone.add(recreated);
		
		assertEquals(obj, recreated);
		
		/*
		 * Let's move Zone to the next turn.
		 */
		zone.nextTurn();
		recreatedZone.nextTurn();

		/*
		 * Remove object from lhand.
		 */
		RPSlot lhand = obj.getSlot("lhand");
		RPObject pocket=lhand.getFirst();
		RPObject removed=lhand.remove(pocket.getID());
		
		assertNotNull(removed);
		assertEquals(removed,pocket);
		
		zone.modify(obj);
		
		/*
		 * Create the perception and serialize it.
		 */
		p=zone.getPerception(obj, Perception.DELTA);
		msg=new MessageS2CPerception(null, p);
		data=enc.encode(msg);
		
		msgs=dec.decode(null, data);
		
		assertEquals("there should only be one message",1, msgs.size());
		
		recv=(MessageS2CPerception)msgs.get(0);
		assertTrue(recv.getAddedRPObjects().isEmpty());
		assertTrue(recv.getModifiedAddedRPObjects().isEmpty());
		assertFalse(recv.getModifiedDeletedRPObjects().isEmpty());
		assertTrue(recv.getDeletedRPObjects().isEmpty());
		
		List<RPObject> modifiedDeleted=recv.getModifiedDeletedRPObjects();
		assertEquals(1, modifiedDeleted.size());
		
		assertNotNull(modifiedDeleted.get(0));
		recreated.applyDifferences(null, modifiedDeleted.get(0));
		
		System.out.println(obj);
		System.out.println(recreated);
		
		assertEquals(obj, recreated);
	}

}
