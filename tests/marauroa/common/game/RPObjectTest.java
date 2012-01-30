/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import marauroa.common.game.Definition.Type;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Before;
import org.junit.Test;

/**
 * Test unit for RPObject basic functionality.
 *
 * @author miguel
 *
 */
public class RPObjectTest {

	private RPObject obj;

	/**
	 * Set up method to create an object that contains some attributes, slots
	 * and events.
	 *
	 */
	@Before
	public void createObject() {
		obj = new RPObject();
		obj.setRPClass(RPClass.getBaseRPObjectDefault());

		obj.put("a", 1);
		obj.put("b", "1");
		obj.put("c", 2.0);
		obj.put("d", "string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		RPObject buddy = new RPObject();
		buddy.put("pepe", "");
		buddy.put("john", "");
		buddy.put("anton", "");

		RPClassTestHelper.generateRPClasses();
		obj.addLink("buddy", buddy);

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

		RPSlot container = pocket.getSlot("container");

		RPObject coin = new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);
	}

	/**
	 * Do some basic test on has and get methods over attributes, slots and
	 * events. This test that adding attributes, slots and events works as
	 * expected.
	 *
	 */
	@Test
	public void testRPObject() {
		assertNotNull(obj);

		assertTrue(obj.has("a"));
		assertEquals(1, obj.getInt("a"));
		assertTrue(obj.has("b"));
		assertEquals("1", obj.get("b"));
		assertTrue(obj.has("c"));
		assertEquals(2.0, obj.getDouble("c"), 0.1f);
		assertFalse(obj.has("e"));

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(obj.hasSlot("rhand"));

		for (Iterator<RPEvent> it = obj.eventsIterator(); it.hasNext();) {
			RPEvent event = it.next();
			assertEquals("chat", event.getName());
		}
	}

	/**
	 * Test that RPSlot works by retriving the object added to the container. It
	 * test it by ensuring that the id numering is correct.
	 *
	 */
	@Test
	public void testRPSlots() {
		RPObject expected = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals(1, expected.getInt("id"));
	}
	
	@Test
	public void testHasAsParent() {
		RPObject p = obj.getSlot("lhand").getFirst();
		
		assertTrue(obj.getSlot("lhand").hasAsAncestor(obj));
		assertTrue(p.getSlot("container").hasAsAncestor(obj));		
	}

	@Test(expected = SlotIsFullException.class)
	public void testSlotCapacity() {
		RPClass clazz = new RPClass("object");
		clazz.addAttribute("a", Type.BYTE);
		clazz.addRPSlot("cont", 1);

		obj = new RPObject();
		obj.setRPClass("object");
		obj.addSlot("cont");

		RPSlot s = obj.getSlot("cont");
		s.add(new RPObject());

		s.add(new RPObject());
		fail("Object added");
	}

	/**
	 * Test the rp link feature by adding a link and testing some methods
	 * over it.
	 */
	@Test
	public void testRPLink() {
		assertTrue(obj.hasLink("buddy"));
		assertFalse(obj.hasLink("pals"));

		RPObject buddy = obj.getLinkedObject("buddy");
		assertNotNull(buddy);
		assertEquals(buddy, obj.getLink("buddy").getObject());
		assertEquals("buddy", obj.getLink("buddy").getName());

		assertTrue(buddy.has("pepe"));
		assertFalse(buddy.has("miguel"));
	}

	/**
	 * Tests serialization of the RPObject by serializing it and then
	 * deserializing it from the stream back again.
	 *
	 * @throws IOException
	 */
	@Test
	public void testSerialization() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(obj);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		RPObject result = (RPObject) is.readObject(new RPObject());

		assertEquals(obj, result);
		
		RPSlot inslot = result.getSlot("lhand");
		for ( RPObject contained : inslot) {
			assertTrue(contained.isContained());
		}
	}

	/**
	 * Test the base container method that should return the base container of
	 * any contained object. The base container is the container of a object
	 * that is not contained by anyone.
	 *
	 */
	@Test
	public void testBaseContainer() {
		RPObject coin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals(obj, coin.getBaseContainer());
	}
	
	/**
	 * Test that the getFromSlots method returns the proper object and returns null
	 * when it is not found.
	 *
	 */
	@Test
	public void testGetObjectFromSlots() {
		RPObject coin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		RPObject coinfromslot = obj.getFromSlots(coin.getInt("id"));
		assertEquals(coin, coinfromslot);

		assertNull(obj.getFromSlots(-1));
	}

	/**
	 * Test clear visible by removing all the visible attributes, slots and
	 * events. The object should be empty afterwards.
	 *
	 */
	@Test
	public void testClearVisible() {
		obj.clearVisible(false);
		assertTrue(obj.isEmpty());
	}

	/**
	 * Check that clear visible doesn't break Delta² information.
	 *
	 * @throws Exception
	 */
	@Test
	public void testClearVisibleDelta2() throws Exception {
		obj.clearVisible(false);

		RPObject oadded = new RPObject();
		RPObject odeleted = new RPObject();

		obj.getDifferences(oadded, odeleted);

		System.out.println(oadded);
		System.out.println(odeleted);

		assertTrue(oadded.isEmpty());
		assertTrue(odeleted.isEmpty());
	}
	
	/**
	 * Checks that passing <code>null</code> does not throw a NPE.
	 */
	@Test
	public void testhasSlot() {
		RPObject localobj = new RPObject();
		String slotname = "slotname";
		assertFalse(localobj.hasSlot(slotname));
		assertFalse(localobj.hasSlot(slotname + "blabla"));
		assertFalse(localobj.hasSlot(null));
		
		localobj.addSlot(slotname);
		assertTrue(localobj.hasSlot(slotname));
		assertFalse(localobj.hasSlot(slotname + "blabla"));
		assertFalse(localobj.hasSlot(null));
		
	}

	/**
	 * Test cloning an RPObject
	 */
	@Test
	public void testClone() {
		RPObject obj1 = new RPObject();
		Object obj2 =  obj1.clone();
		assertFalse(obj1==obj2);
		assertTrue(obj2.getClass() == obj1.getClass());
		assertEquals(obj1, obj2);
		
		RPObject subobj1 = new SubRPObject();
		Object subObj2 = subobj1.clone();
		assertFalse(subobj1==subObj2);
		assertTrue(subObj2.getClass() == SubRPObject.class);
		
		assertTrue(subObj2.getClass() == subobj1.getClass());
		assertEquals(subobj1, subObj2);
	
	}
	
	@Test
	public void testPutInMapAttribute() {
		RPClass cls = new RPClass("testmaps");
		cls.addAttribute("testmap", Type.MAP);
		RPObject rpo = new RPObject();
		rpo.setRPClass(cls);
		try {
			rpo.put("testmap", "testvalue");
			fail("An IllegalArgumentException should have been thrown.");
		} catch (IllegalArgumentException e) {
			// should just be caught
		}
	}
	
	@Test
	public void testPutInMapAttributeNotBeingMap() {
		RPClass cls = new RPClass("testmaps-2");
		cls.addAttribute("testmap", Type.STRING);
		RPObject rpo = new RPObject();
		rpo.setRPClass(cls);
		try {
			rpo.put("testmap", "testkey", "testvalue");
			fail("An IllegalArgumentException should have been thrown.");
		} catch (IllegalArgumentException e) {
			// should just be caught
		}
	}
	
	@Test
	public void testMapSerialization() throws IOException {
		RPClass cls = new RPClass("testmaps-serialization");
		cls.addAttribute("map1", Type.MAP);
		RPObject rpo = new RPObject();
		rpo.setRPClass(cls);
		rpo.put("map1", "key11", "value11");
		rpo.put("map1", "key12", "value12");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);
		os.write(rpo);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);
		RPObject result = (RPObject) is.readObject(new RPObject());
		assertEquals(rpo, result);
	}
	
	@Test
	public void testMapSerializationTwoMaps() throws IOException {
		RPClass cls = new RPClass("testmaps-serialization-2");
		cls.addAttribute("map1", Type.MAP);
		cls.addAttribute("map2", Type.MAP);
		RPObject rpo = new RPObject();
		rpo.setRPClass(cls);
		rpo.put("map1", "key11", "value11");
		rpo.put("map1", "key12", "value12");
		rpo.put("map2", "key21", "value21");
		rpo.put("map2", "key22", "value22");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);
		os.write(rpo);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);
		RPObject result = (RPObject) is.readObject(new RPObject());
		assertEquals(rpo, result);
	}
	
	class SubRPObject extends RPObject{
		// just subclass of RPObject used for testing for of the
		// Object.clone() contract
	}
	
}
