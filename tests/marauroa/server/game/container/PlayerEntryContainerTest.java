package marauroa.server.game.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import marauroa.common.Configuration;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the overall idea of Player Entry container to make sure it works
 * correctly. Database needs to be accesible in order to test it.
 * 
 * @author miguel
 * 
 */
public class PlayerEntryContainerTest {
	static class MockPlayerEntryContainer extends PlayerEntryContainer {
		MockPlayerEntryContainer() {
			super();
		}

		protected void initializeDatabase() {
			/* Do nothing, we don't need database on this test. */
		}
	}

	private static PlayerEntryContainer cont;

	/**
	 * Initialize the container.
	 * 
	 */
	@BeforeClass
	public static void setUp() {
		cont = new MockPlayerEntryContainer();
	}

	/**
	 * Add an entry to the container and test if it is added correctly and if it
	 * can be retrieved using the available methods.
	 * 
	 */
	@Test
	public void testAdd() {
		PlayerEntry entry = cont.add(null);

		entry.username = "test0";

		assertTrue(cont.has(entry.clientid));
		assertFalse(cont.has(entry.clientid + 1));

		assertEquals(entry, cont.get(entry.clientid));
		assertEquals(entry, cont.get(entry.username));
	}

	/**
	 * Test get methods to see if they work as expected.
	 * 
	 */
	@Test
	public void testGet() {
		PlayerEntry entry = cont.add(null);
		entry.username = "test1";

		assertEquals(entry, cont.get(entry.clientid));
		assertEquals(entry, cont.get(entry.username));
		assertNull(cont.get("aDifferentOne"));
	}

	/**
	 * Test remove method.
	 * 
	 */
	@Test
	public void testRemove() {
		PlayerEntry entry = cont.add(null);
		entry.username = "test2";

		PlayerEntry removed = cont.remove(entry.clientid);
		assertNotNull(removed);
		assertEquals(entry, removed);

		removed = cont.remove(entry.clientid);
		assertNull(removed);
	}

}
