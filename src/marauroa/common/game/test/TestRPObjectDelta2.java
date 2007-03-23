package marauroa.common.game.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import marauroa.common.game.Perception;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test delta² algorithm
 * This test unit needs MarauroaRPZone and RPObject.
 * @author miguel
 *
 */
public class TestRPObjectDelta2 {

	private RPObject obj;

	private MarauroaRPZone zone;

	/**
	 * Set up an object and create a zone that will contain it.
	 * It doesn't add the object to the zone.
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

		RPSlot container = pocket.getSlot("container");

		RPObject coin = new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);

		zone = new MarauroaRPZone("test") {

			public void onInit() throws Exception {
			}

			public void onFinish() throws Exception {
			}
		};

	}

	/**
	 * Test if adding a object to a zone works, by assigning object a correct id
	 * and retriving object from zone.
	 *
	 * Also test that modifications to object after just adding it works as expected, that
	 * means that modifications are irrelevant to delta²
	 *
	 */
	@Test
	public void testAddObjectToZone() {
		/*
		 * Add object to zone
		 * Test it has correct attributes values.
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

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertFalse(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test hidden object concept.
	 * It hide an object and then add it to zone, the object must not appear at the perception.
	 *
	 */
	@Test
	public void testAddHiddenObjectToZone() {
		/*
		 * Add object to zone
		 * Test it has correct attributes values.
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

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test remove object from zone, by adding it and on the next turn removing it.
	 *
	 */
	@Test
	public void testRemoveObjectFromZone() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();

		zone.remove(obj.getID());

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertFalse(expected.deletedList.isEmpty());
	}

	/**
	 * Test if adding an attribute to an object works as expected in Delta².
	 * The change must appear at modified added list
	 *
	 */
	@Test
	public void testAttributeAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object addition.
		 */
		obj.put("bg", "house red");

		zone.modify(obj);

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test attribute removal.
	 * It must appear at modified deleted list.
	 *
	 */
	@Test
	public void testAttributeRemoval() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object removal.
		 */
		obj.remove("b");

		zone.modify(obj);

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test attribute modification.
	 * The change must appear at modified added.
	 *
	 */
	@Test
	public void testAttributeModification() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on attribute object modification.
		 */
		obj.put("b", 19);

		zone.modify(obj);

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test that an added event in a object inside a slot works.
	 * It must appear at modified added.
	 *
	 */
	@Test
	public void testAddSlotEventAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object event addition
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		RPEvent tax = new RPEvent("tax");
		tax.put("bill", "10%");
		slotcoin.addEvent(tax);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test that an modified attribute in a object inside a slot works.
	 * It must appear at modified added.
	 *
	 */
	@Test
	public void testSlotObjectAttributeModification() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.put("value", 200);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test that an added attribute in a object inside a slot works.
	 * It must appear at modified added.
	 *
	 */
	@Test
	public void testSlotObjectAttributeAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.put("pesetas", 4000);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test that an removed attribute in a object inside a slot works.
	 * It must appear at modified deleted.
	 *
	 */
	@Test
	public void testSlotObjectAttributeRemoval() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
		 */
		zone.nextTurn();
		/*
		 * Test Delta^2 on slot object modification.
		 */
		RPObject slotcoin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.remove("value");

		zone.modify(slotcoin.getBaseContainer());

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test that an added object in a object inside a slot works.
	 * It must appear at modified added.
	 *
	 */
	@Test
	public void testSlotObjectAddition() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
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

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * Test that an removed object in a object inside a slot works.
	 * It must appear at modified deleted.
	 *
	 */
	@Test
	public void testSlotObjectRemoval() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
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

		Perception expected = zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	/**
	 * This is a KNOWN bug that happens when a object from a visible slot is removed
	 * in the object that has cleared the visible attributes.
	 */
	@Ignore
	@Test
	public void testVisibleApplyDifferencesBug() {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
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

		obj.clearVisible();

		RPObject added = new RPObject();
		RPObject deleted = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		result.clearVisible();

		assertEquals(result, obj);
	}

	/**
	 * This test try to show a problem that could happen if you delete and add an object
	 * on the same turn. It should work correctly.
	 */
	@Test
	public void testApplyDifferences() throws Exception {
		zone.assignRPObjectID(obj);
		zone.add(obj);
		/*
		 * Next turn.
		 * We want to clear Delta^2 data.
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
}
