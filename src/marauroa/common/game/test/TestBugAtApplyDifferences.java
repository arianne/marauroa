package marauroa.common.game.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import marauroa.common.game.Perception;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.Before;
import org.junit.Test;


public class TestBugAtApplyDifferences {


	private RPObject obj;
	private MarauroaRPZone zone;

	private MarauroaRPZone recreatedZone;
	
	/**
	 * Set up an object and create a zone that will contain it. It doesn't add
	 * the object to the zone.
	 * 
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
			public void onInit() throws Exception {
			}

			public void onFinish() throws Exception {
			}
		};

		recreatedZone = new MarauroaRPZone("test") {
			public void onInit() throws Exception {
			}

			public void onFinish() throws Exception {
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
		
		/*
		 * There should only be one message.
		 */
		assertEquals(1, msgs.size());
		
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
