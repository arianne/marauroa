package marauroa.common.game.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import marauroa.common.game.Perception;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.game.rp.MarauroaRPZone;

import org.junit.Before;
import org.junit.Test;


public class TestRPObjectDelta2 {
	private RPObject obj;
	private MarauroaRPZone zone;

	@Before
	public void createObject() {
		obj=new RPObject();

		obj.put("a",1);
		obj.put("b","1");
		obj.put("c",2.0);
		obj.put("d","string of text");

		obj.addSlot("lhand");
		obj.addSlot("rhand");

		obj.addEvent("chat", "Hi there!");
		obj.addEvent("chat", "Does this work?");

		RPSlot lhand=obj.getSlot("lhand");

		RPObject pocket=new RPObject();
		pocket.put("size", 1);
		pocket.addSlot("container");
		lhand.add(pocket);

		RPSlot container=pocket.getSlot("container");

		RPObject coin=new RPObject();
		coin.put("euro", 100);
		coin.put("value", 100);
		container.add(coin);

		zone=new MarauroaRPZone("test") {
			public void onInit() throws Exception {
			}

			public void onFinish() throws Exception {
			}
		};

	}

	@Test
	public void testAddObjectToZone() {
		/*
		 * Add object to zone
		 * Test it has correct attributes values.
		 */
		zone.assignRPObjectID(obj);
		assertTrue(obj.has("id"));
		assertTrue(obj.has("zoneid"));
		assertEquals("test",obj.get("zoneid"));

		/*
		 * Test if first time modification works as expected.
		 */
		zone.add(obj);
		obj.put("b",9);
		zone.modify(obj);

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertFalse(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

	@Test
	public void testAddHiddenObjectToZone() {
		/*
		 * Add object to zone
		 * Test it has correct attributes values.
		 */
		zone.assignRPObjectID(obj);
		assertTrue(obj.has("id"));
		assertTrue(obj.has("zoneid"));
		assertEquals("test",obj.get("zoneid"));

		/*
		 * Hide the object
		 */
		obj.hide();

		/*
		 * Test if first time modification works as expected.
		 */
		zone.add(obj);

		obj.put("b",9);
		zone.modify(obj);

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertFalse(expected.deletedList.isEmpty());
	}

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
		obj.put("bg","house red");

		zone.modify(obj);

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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
		obj.put("b",19);

		zone.modify(obj);

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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
		RPObject slotcoin=obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.addEvent("tax","10%");

		zone.modify(slotcoin.getBaseContainer());

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}


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
		RPObject slotcoin=obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.put("value",200);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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
		RPObject slotcoin=obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.put("pesetas",4000);

		zone.modify(slotcoin.getBaseContainer());

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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
		RPObject slotcoin=obj.getSlot("lhand").getFirst().getSlot("container").getFirst();

		slotcoin.remove("value");

		zone.modify(slotcoin.getBaseContainer());

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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
		RPSlot slot=obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject anothercoin=new RPObject();
		anothercoin.put("euro", 2);
		anothercoin.put("value", "tomato");
		slot.add(anothercoin);

		zone.modify(anothercoin.getBaseContainer());

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertFalse(expected.modifiedAddedList.isEmpty());
		assertTrue(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}

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
		RPSlot slot=obj.getSlot("lhand").getFirst().getSlot("container");
		RPObject coin=slot.remove(slot.getFirst().getID());

		assertNotNull(coin);
		assertEquals(coin, coin.getBaseContainer());

		zone.modify(obj);

		Perception expected=zone.getPerception(obj.getID(), Perception.DELTA);
		assertTrue(expected.addedList.isEmpty());
		assertTrue(expected.modifiedAddedList.isEmpty());
		assertFalse(expected.modifiedDeletedList.isEmpty());
		assertTrue(expected.deletedList.isEmpty());
	}
}
