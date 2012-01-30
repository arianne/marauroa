/***************************************************************************
 *                   (C) Copyright 2007-2012 - Marauroa                    *
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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RPObjectIDTest {

	@Test
	public void testHashCode() {
		RPObject.ID id3  = new RPObject.ID(13 , new String("testzone"));
		assertEquals(id3.getObjectID(),13);
		assertEquals(id3.hashCode(),13);
		
	}

	@Test
	public void testIDIntString() {
		int idNumber = 4;
		String idName = "testzone";
		RPObject.ID id3  = new RPObject.ID(idNumber , idName);
		assertEquals(id3.getObjectID(),idNumber);
		assertEquals(id3.getZoneID(), idName);
	}

	@Test
	public void testIDIntID() {
		int idNumber = 4;
		String idName = "testzone";
		IRPZone.ID zoneID = new IRPZone.ID(idName);
		RPObject.ID id3  = new RPObject.ID(idNumber , zoneID);
		assertEquals(id3.getObjectID(),idNumber);
		assertEquals(id3.getZoneID(), idName);

	}

	@Test
	public void testIDRPObject() {
		int idNumber = 4;
		String idName = "testzone";
		RPObject.ID initID  = new RPObject.ID(idNumber , idName);

		RPObject withzone = new RPObject(initID);
		assertEquals(initID, withzone.getID());
		
		
		
		RPObject.ID initID2  = new RPObject.ID(idNumber , (String) null);

		RPObject withoutZone = new RPObject(initID2);
		assertEquals(initID2, withoutZone.getID());
	}

	@Test
	public void testIDRPAction() {
		RPAction action = new RPAction();
		action.put("sourceid", 1);
		action.put("zoneid", "testzone");
		
		RPObject.ID withzone = new RPObject.ID(action);
		RPObject.ID initID = new RPObject.ID(1,"testzone");
		assertEquals(initID , withzone);
		action.remove("zoneid");
		
		RPObject.ID initID2 = new RPObject.ID(1,(String) null);
	
		RPObject.ID withoutZone = new RPObject.ID(action);
		assertFalse(initID2.equals(withzone));
		assertEquals(initID2, withoutZone);
	}

	@Test
	public void testGetObjectID() {
		RPObject.ID id3  = new RPObject.ID(0 , new String("testzone"));
		assertEquals(id3.getObjectID(),0);
	}

	@Test
	public void testGetZoneID() {
		RPObject.ID id3  = new RPObject.ID(0 , new String("testzone"));
		assertEquals(id3.getZoneID(), "testzone");

	}

	@Test
	public void testEqualsObject() {
		RPObject.ID id3  = new RPObject.ID(0 , new String("testzone"));
		RPObject.ID id4  = new RPObject.ID(0 , new String("testzone"));
		
		assertTrue(id3.equals(id4));
		assertTrue(id4.equals(id3));
		RPObject.ID id5  = new RPObject.ID(1 , new String("testzone"));
		assertFalse(id3.equals(id5));
		assertFalse(id4.equals(id5));
		assertFalse(id5.equals(id4));
		assertFalse(id5.equals(id3));
		
		RPObject.ID id  = new RPObject.ID(0 , (String) null);
		RPObject.ID id2  = new RPObject.ID(0 , (String) null);
		
		assertTrue(id.equals(id));
		assertTrue(id2.equals(id));
	
		RPObject.ID id6  = new RPObject.ID(0 , "notnull");
		
		assertFalse(id.equals(id6));
		assertFalse(id6.equals(id));
		
	}

	@Test
	public void testToString() {
		RPObject.ID id6  = new RPObject.ID(0 , "notnull");
		assertEquals("RPObject.ID [id=0 zoneid=notnull]", id6.toString());
		
		RPObject.ID id2  = new RPObject.ID(0 , (String) null);
		assertEquals("RPObject.ID [id=0 zoneid=null]", id2.toString());
		
	}

}
