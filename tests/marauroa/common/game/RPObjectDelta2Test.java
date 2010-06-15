/* $Id: RPObjectDelta2Test.java,v 1.8 2010/06/15 15:25:04 madmetzger Exp $ */
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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import marauroa.common.game.Definition.Type;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test delta² algorithm This test unit needs MarauroaRPZone and RPObject.
 * 
 * @author miguel
 * 
 */
public class RPObjectDelta2Test {

	private RPObject obj;

	private MarauroaRPZone zone;

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

		if (!RPClass.hasRPClass("chat")) {
			RPClass rpclass = new RPClass("chat");
			rpclass.addAttribute("text", Type.STRING);
		}

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

		zone = new MarauroaRPZone("test") {

			@Override
			public void onInit() throws Exception {
				// do nothing, but method is required by interface
			}

			@Override
			public void onFinish() throws Exception {
				// do nothing, but method is required by interface
			}
		};

	}

	/**
	 * Test if adding a object to a zone works, by assigning object a correct id
	 * and retriving object from zone.
	 * 
	 * Also test that modifications to object after just adding it works as
	 * expected, that means that modifications are irrelevant to delta²
	 * 
	 */
	@Test
	public void testAddObjectToZone() {
		/*
		 * Add object to zone Test it has correct attributes values.
		 */
		zone.assignRPObjectID(obj);
		assertTrue(obj.has("id"));
		assertTrue(obj.has("zoneid"));
		assertEquals("test", obj.get("zoneid"));

		/*
		 * Test if first time modification works as expected.
		 */
		zone.add(obj);
		obj.put("b", 9);
		zone.modify(obj);

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertFalse(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
		
		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.addedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("b"), result.get("b"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));
	}

	/**
	 * Test hidden object concept. It hide an object and then add it to zone,
	 * the object must not appear at the perception.
	 * 
	 */
	@Test
	public void testAddHiddenObjectToZone() {
		/*
		 * Add object to zone Test it has correct attributes values.
		 */
		zone.assignRPObjectID(obj);
		assertTrue(obj.has("id"));
		assertTrue(obj.has("zoneid"));
		assertEquals("test", obj.get("zoneid"));

		/*
		 * Hide the object
		 */
		obj.hide();

		/*
		 * Test if first time modification works as expected.
		 */
		zone.add(obj);

		obj.put("b", 9);
		zone.modify(obj);

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test remove object from zone, by adding it and on the next turn removing
	 * it.
	 * 
	 */
	@Test
	public void testRemoveObjectFromZone() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();

		zone.remove(obj.getID());

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertFalse(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.deletedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals("test", result.get("zoneid"));
	}

	/**
	 * Test if adding an attribute to an object works as expected in Delta². The
	 * change must appear at modified added list
	 * 
	 */
	@Test
	public void testAttributeAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object addition.
		 */
		obj.put("bg", "house red");

		zone.modify(obj);

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedAddedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("bg"), result.get("bg"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));
		
	}

	/**
	 * Test attribute removal. It must appear at modified deleted list.
	 * 
	 */
	@Test
	public void testAttributeRemoval() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object removal.
		 */
		obj.remove("b");

		zone.modify(obj);

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedDeletedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertTrue(result.has("b"));
		assertFalse(obj.has("b"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));
	}
	
	/**
	 * Test attribute removal. It must appear at modified deleted list
	 * even in the case the attribute has been modified the same turn.
	 */
	@Test
	public void testAttributeRemoval2() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		obj.put("b", "original value");
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		obj.put("b", "changed value");
		/*
		 * Test Delta^2 on attribute object removal.
		 */
		obj.remove("b");

		zone.modify(obj);

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedDeletedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertTrue(result.has("b"));
		assertFalse(obj.has("b"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));
	}

	/**
	 * Test attribute modification. The change must appear at modified added.
	 * 
	 */
	@Test
	public void testAttributeModification() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object modification.
		 */
		obj.put("b", 19);

		zone.modify(obj);

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedAddedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("b"), result.get("b"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));
	}

	/**
	 * Test that an added event in a object inside a slot works. It must appear
	 * at modified added.
	 * 
	 */
	@Test
	public void testAddSlotEventAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object event addition
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		if (!RPClass.hasRPClass("tax")) {
			RPClass rpclass = new RPClass("tax");
			rpclass.addAttribute("bill", Type.STRING);
		}

		RPEvent tax = new RPEvent("tax");
		tax.put("bill", "10%");
		slotcoin.addEvent(tax);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedAddedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));

		System.out.println(obj);
		System.out.println(result);
		RPEvent resultEvent=result.getSlot("lhand").getFirst().getSlot("container").getFirst().events().get(0);
		assertEquals(tax,resultEvent);
	}

	/**
	 * Test that an modified attribute in a object inside a slot works. It must
	 * appear at modified added.
	 * 
	 */
	@Test
	public void testSlotObjectAttributeModification() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.put("value", 200);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedAddedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));

		RPObject resultEvent=result.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals(slotcoin.get("value"),resultEvent.get("value"));
	}

	/**
	 * Test that an added attribute in a object inside a slot works. It must
	 * appear at modified added.
	 * 
	 */
	@Test
	public void testSlotObjectAttributeAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.put("pesetas", 4000);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedAddedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));

		RPObject resultEvent=result.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals("4000",resultEvent.get("pesetas"));
	}

	/**
	 * Test that an removed attribute in a object inside a slot works. It must
	 * appear at modified deleted.
	 * 
	 */
	@Test
	public void testSlotObjectAttributeRemoval() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.remove("value");

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedDeletedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));

		RPObject resultEvent=result.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertTrue(resultEvent.has("value"));
	}

	/**
	 * Test that an added object in a object inside a slot works. It must appear
	 * at modified added.
	 * 
	 */
	@Test
	public void testSlotObjectAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPSlot slot = obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject anothercoin = new RPObject();
		anothercoin.put("euro", 2);
		anothercoin.put("value", "tomato");
		slot.add(anothercoin);

		zone.modify(anothercoin.getBaseContainer());

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());

		/*
		 * Now we test the resulting object to check everything is ok.
		 */
		RPObject result=expected.modifiedAddedList.get(0);
		assertEquals(obj.get("id"), result.get("id"));
		assertEquals(obj.get("zoneid"), result.get("zoneid"));
		assertEquals("test", result.get("zoneid"));

		RPSlot resultSlot = result.getSlot("lhand").getFirst().getSlot("container");
		/*
		 * Only one modified slot.
		 */
		assertEquals(1,resultSlot.size());
		assertEquals(2,slot.size());
	}

	/**
	 * Test that an removed object in a object inside a slot works. It must
	 * appear at modified deleted.
	 * 
	 */
	@Test
	public void testSlotObjectRemoval() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPSlot slot = obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject coin = slot.remove(slot.getFirst().getID());

		assertNotNull(coin);
		assertEquals(coin, coin.getBaseContainer());

		zone.modify(obj);

		Perception expected = zone.getPerception(obj, Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * This is a KNOWN bug that happens when a object from a visible slot is
	 * removed in the object that has cleared the visible attributes.
	 */
	@Test
	public void testVisibleApplyDifferencesBug() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();

		RPObject result = (RPObject) obj.clone();

		/*
		 * Test Delta^2 on slot object modification.
		 */
		/* Remove coin from slot */
		RPSlot slot = obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject coin = slot.remove(slot.getFirst().getID());

		assertNotNull(coin);
		assertEquals(coin, coin.getBaseContainer());

		obj.clearVisible(false);

		RPObject added = new RPObject();
		RPObject deleted = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		result.clearVisible(false);

		assertEquals(result, obj);
	}

	/**
	 * This test try to show a problem that could happen if you delete and add
	 * an object on the same turn. It should work correctly.
	 */
	@Test
	public void testApplyDifferences() throws Exception {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();

		RPObject result = (RPObject) obj.clone();

		/*
		 * Test Delta^2 on slot object modification.
		 */
		/* Remove coin from slot */
		RPSlot slot = obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject coin = slot.remove(slot.getFirst().getID());

		assertNotNull(coin);
		assertEquals(coin, coin.getBaseContainer());

		/* Added another coin to slot */
		slot = obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject anothercoin = new RPObject();
		anothercoin.put("euro", 2);
		anothercoin.put("value", "tomato");
		slot.add(anothercoin);

		RPObject added = new RPObject();
		RPObject deleted = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		assertEquals(result, obj);
	}
	
	/**
	 * This test try to show a problem that could happen if you delete and add
	 * an object on the same turn. It should work correctly. Different change
	 * order from testApplyDifferences
	 */
	@Test
	public void testApplyDifferences2() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();

		obj.put("d", "original text");
		RPObject result = (RPObject) obj.clone();

		// modify
		obj.put("d", "changed text");
		// remove attribute that was modified
		obj.remove("d");

		RPObject added = new RPObject();
		RPObject deleted = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}


	@Test
	public void testApplyDifferencesOnSlotObjectRemove() throws Exception {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();

		RPObject result = (RPObject) obj.clone();

		/*
		 * Test Delta^2 on slot object modification.
		 */
		/* Remove coin from slot */
		RPSlot slot = obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject coin = slot.remove(slot.getFirst().getID());

		assertNotNull(coin);
		assertEquals(coin, coin.getBaseContainer());

		RPObject added = new RPObject();
		RPObject deleted = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);
		
		assertEquals(result, obj);
	}

	@Test
	public void testRPEvent() throws Exception {
		List<RPEvent> events=obj.events();
		assertEquals("There is two events", 2, events.size());

		zone.assignRPObjectID(obj);
		zone.add(obj);
		
		events=obj.events();	
		assertEquals("There is no event", 0, events.size());

		RPObject result = (RPObject) obj.clone();

		/*
		 * Test adding one rp event
		 */
		RPEvent chat = new RPEvent("chat");
		chat.put("text", "Hi there");
		obj.addEvent(chat);

		RPObject added = new RPObject();
		RPObject deleted = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		events=result.events();	
		assertEquals("There is one event", 1, events.size());

		
		/*
		 * Next turn. We want to clear Delta^2 data.
		 */
		zone.nextTurn();
	}
	
	@Test
	public void testMapDelta() {
		RPClass rpClass = new RPClass("mymapdeltatestclass");
		rpClass.addAttribute("testmap", Type.MAP);
		RPObject newObject = new RPObject();
		newObject.setRPClass(rpClass);
		newObject.setID(RPObject.INVALID_ID);
		RPObject added =  new RPObject();
		RPObject deleted = new RPObject();
		newObject.getDifferences(added, deleted);
		newObject.put("testmap", "testkey", "testvalue");
		newObject.getDifferences(added, deleted);
		assertFalse(newObject.isEmpty());
		assertFalse(added.isEmpty());
		assertThat(added.get("testmap", "testkey"), is("testvalue"));
		assertTrue(deleted.isEmpty());
		newObject.resetAddedAndDeleted();
		newObject.remove("testmap", "testkey");
		RPObject added2 = new RPObject();
		RPObject deleted2 = new RPObject();
		newObject.getDifferences(added2, deleted2);
		assertTrue(added2.isEmpty());
		assertFalse(deleted2.isEmpty());
		assertThat(deleted2.get("testmap", "testkey"), is("0"));
	}
	
}
