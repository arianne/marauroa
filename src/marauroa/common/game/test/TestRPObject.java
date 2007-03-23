package marauroa.common.game.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Before;
import org.junit.Test;

/**
 * Test unit for RPObject basic functionality
 * @author miguel
 *
 */
public class TestRPObject {

	private RPObject obj;

	/**
	 * Set up method to create an object that contains some attributes, slots and events.
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
	}

	/**
	 * Do some basic test on has and get methods over attributes, slots and events.
	 * This test that adding attributes, slots and events works as expected.
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
		assertEquals(2.0, obj.getDouble("c"));
		assertFalse(obj.has("e"));

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(obj.hasSlot("rhand"));

		for (Iterator<RPEvent> it = obj.eventsIterator(); it.hasNext();) {
			RPEvent event = it.next();
			assertEquals("chat", event.getName());
		}
	}

	/**
	 * Test that RPSlot works by retriving the object added to the container.
	 * It test it by ensuring that the id numering is correct.
	 *
	 */
	@Test
	public void testRPSlots() {
		RPObject expected = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals(1, expected.getInt("id"));
	}

	/** Test serialization of the RPObject by serializing it and then deserializing it
	 * from the stream back again.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(obj);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		RPObject result = (RPObject) is.readObject(new RPObject());

		assertEquals(obj, result);
	}

	/**
	 * Test the base container method that should return the base container of any contained
	 * object.
	 * The base container is the container of a object that is not contained by anyone.
	 *
	 */
	@Test
	public void testBaseContainer() {
		RPObject coin = obj.getSlot("lhand").getFirst().getSlot("container").getFirst();
		assertEquals(obj, coin.getBaseContainer());
	}

	/**
	 * Test clear visible by removing all the visible attributes, slots and events.
	 * The object should be empty afterwards.
	 *
	 */
	@Test
	public void testClearVisible() {
		obj.clearVisible();
		assertTrue(obj.isEmpty());
	}

	/**
	 * Check that clear visible doesn't break Delta² information.
	 *
	 * @throws Exception
	 */
	@Test
	public void testClearVisibleDelta2() throws Exception {
		obj.clearVisible();

		RPObject oadded = new RPObject();
		RPObject odeleted = new RPObject();

		obj.getDifferences(oadded, odeleted);

		System.out.println(oadded);
		System.out.println(odeleted);

		assertTrue(oadded.isEmpty());
		assertTrue(odeleted.isEmpty());
	}

}
