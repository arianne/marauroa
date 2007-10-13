package marauroa.common.game;

import static org.junit.Assert.*;

import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.RPObject.ID;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RPSlotTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testHashCode() {
		RPSlot rpslot1 = new RPSlot();
		RPSlot rpslot2 = new RPSlot();
		assertTrue(rpslot1.hashCode()==rpslot2.hashCode());

	}


	@Test
	public final void testGetSetOwner() {
		RPSlot rps = new RPSlot();
		rps.setOwner(new RPObject());
		assertEquals(new RPObject(), rps.getOwner());
		RPObject blarpo = new RPObject();
		blarpo.add("bla", 1);
		RPObject rpo2 = new RPObject();
		assertFalse(blarpo.equals(rpo2));
		assertFalse(blarpo.equals(rps.getOwner()));
		assertEquals(rpo2, rps.getOwner());
	}


	@Test
	@Ignore
	public final void testSetGetName() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testAddRPObject() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testAddPreservingId() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testAddRPObjectBoolean() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testGet() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testGetFirst() {
		fail("Not yet implemented");
	}

	@Test
	public final void testRemove() {
		ID id = new ID(0,"");
		RPSlot rps = new RPSlot();
		assertNull(rps.remove(null));
		assertNull(rps.remove(new ID(0, "")));
		rps.setOwner(new RPObject());
		RPObject rpoWithID = new RPObject(id);
		rps.add(rpoWithID );
		assertSame(rpoWithID,rps.remove(id));

	}

	@Test
	@Ignore
	public final void testClear() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testHas() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testHasAsAncestor() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testSize() {
		fail("Not yet implemented");
	}

	@Test
	public final void testGetCapacity() {
		RPSlot rps = new RPSlot();
		assertEquals(-1,rps.getCapacity());

		// create a class definition
		RPClass entity = new RPClass("corpse");
		entity.addRPSlot("content", 4);

		// instantiate an object
		RPObject rpo = new RPObject();
		rpo.setRPClass(entity);
		rpo.addSlot(new RPSlot("content"));

		assertEquals(4,rpo.getRPClass().getDefinition(DefinitionClass.RPSLOT, "content").getCapacity());
		assertNotNull("we should be able to access it ?", rpo.getSlot("content"));
	}

//	@Test
//	public final void testIsFull() {
//		RPObject rpo = new RPObject();
//
//		RPClass entity = new RPClass("corpse");
//		entity.addRPSlot("content", 4);
//
//
//		rpo.setRPClass(entity);
//		RPSlot rps = entity.getSlot("content");
//		assertEquals(4, rps.getCapacity());
//
//	}

	@Test
	@Ignore
	public final void testIterator() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testEqualsObject() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testToString() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testWriteObjectOutputSerializer() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testWriteObjectOutputSerializerDetailLevel() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testReadObject() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testClone() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testResetAddedAndDeletedRPObjects() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testSetAddedRPObject() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testSetDeletedRPObject() {
		fail("Not yet implemented");
	}

	@Test
	@Ignore
	public final void testClearVisible() {
		fail("Not yet implemented");
	}

}
