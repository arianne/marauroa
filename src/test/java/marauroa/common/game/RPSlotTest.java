/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.RPObject.ID;

import org.junit.Test;

public class RPSlotTest {

	@Test
	public final void testHashCode() {
		RPSlot rpslot1 = new RPSlot("testslot");
		RPSlot rpslot2 = new RPSlot("testslot");
		assertEquals("hashcode match", rpslot1.hashCode(), rpslot2.hashCode());
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
	public final void testAddRPObject() {
		RPSlot slot = new RPSlot("slotname");
		RPObject owner = new RPObject();
		slot.setOwner(owner);
		RPObject object = new RPObject();
		slot.add(object);
		assertEquals(slot, object.getContainerSlot());
		assertEquals(owner, object.getContainer());
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
	public final void testGetCapacity() {
		RPSlot rps = new RPSlot();
		assertEquals(-1,rps.getCapacity());

		// create a class definition
		RPClass entity = new RPClass("corpseCapacity");
		entity.addRPSlot("content", 4);

		// instantiate an object
		RPObject rpo = new RPObject();
		rpo.setRPClass(entity);
		rpo.addSlot(new RPSlot("content"));

		assertEquals(4,rpo.getRPClass().getDefinition(DefinitionClass.RPSLOT, "content").getCapacity());
		rps = rpo.getSlot("content");
		assertEquals(4, rps.getCapacity());

	}

	@Test  (expected=SlotIsFullException.class)
	public final void testIsFull() {
		RPClass entity = new RPClass("corpseException");
		entity.addRPSlot("content", 4);

		// instantiate an object
		RPObject rpo = new RPObject();
		rpo.setRPClass(entity);
		rpo.addSlot(new RPSlot("content"));
		RPSlot rps = rpo.getSlot("content");
		assertEquals(4, rps.getCapacity());

		assertEquals(0,rps.add(new RPObject()));
		assertEquals(1, rps.size());

		assertEquals(1,rps.add(new RPObject()));
		assertEquals(2, rps.size());

		assertEquals(2,rps.add(new RPObject()));
		assertEquals(3, rps.size());

		assertEquals(3,rps.add(new RPObject()));
		assertEquals(4, rps.size());

		rps.add(new RPObject());
	}


	@Test
	public void testRemoveContained() throws Exception {
		RPObject owner = new RPObject();
		
		RPSlot slot = new RPSlot();
		owner.addSlot(slot);
		RPObject object = new RPObject();
		slot.add(object);
		assertTrue(object.isContained());
		
		slot.remove(object.getID());
		assertFalse(object.isContained());
		
		
		
		
	}
	
}
