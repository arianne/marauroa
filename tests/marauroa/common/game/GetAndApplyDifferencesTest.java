/* $Id: GetAndApplyDifferencesTest.java,v 1.3 2008/02/22 10:28:35 arianne_rpg Exp $ */
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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * This class test the getDifferences and applyDifferences methods used at the
 * Delta² algorithm.
 * 
 * @author miguel
 * 
 */
public class GetAndApplyDifferencesTest {

	/**
	 * Test if the getDiferences of an empty object works, by building it again
	 * using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an attribute added works,
	 * by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addedAttributeRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.put("test", "val");

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an attribute modified
	 * works, by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void modifiedAttributeRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.put("test", "val");

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		obj.put("test", "another val");
		deleted = new RPObject();
		added = new RPObject();

		obj.getDifferences(added, deleted);

		result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an attribute removed
	 * works, by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void removedAttributeRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.put("test", "val");

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		obj.remove("test");
		deleted = new RPObject();
		added = new RPObject();

		obj.getDifferences(added, deleted);

		result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an empty slot added
	 * works, by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addedEmptyRPSlotRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.addSlot("lhand");

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an empty slot removed
	 * works, by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void removedEmptyRPSlotRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.addSlot("lhand");

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(result.hasSlot("lhand"));

		/* Clear the delta² data */
		obj.resetAddedAndDeleted();

		obj.removeSlot("lhand");
		deleted = new RPObject();
		added = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an empty slot added
	 * works, by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addedRPSlotWithRPObjectOnRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.addSlot("lhand");

		RPObject sword = new RPObject();
		sword.put("type", "huge sword");
		obj.getSlot("lhand").add(sword);

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an slot with an object
	 * and has another new object added works, by building it again using
	 * applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void addedRPSlotWithTwoRPObjectOnRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.addSlot("lhand");

		RPObject sword = new RPObject();
		sword.put("type", "huge sword");
		obj.getSlot("lhand").add(sword);

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(result.hasSlot("lhand"));

		/* Clear the delta² data */
		obj.resetAddedAndDeleted();

		RPObject abiggersword = new RPObject();
		abiggersword.put("type", "very huge sword");
		obj.getSlot("lhand").add(abiggersword);

		deleted = new RPObject();
		added = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an slot with an object
	 * and it is modified works, by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void modifiedRPSlotWithRPObjectOnRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.addSlot("lhand");

		RPObject sword = new RPObject();
		sword.put("type", "huge sword");
		obj.getSlot("lhand").add(sword);

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(result.hasSlot("lhand"));

		/* Clear the delta² data */
		obj.resetAddedAndDeleted();

		RPObject abiggersword = obj.getSlot("lhand").getFirst();
		abiggersword.put("weight", 16);

		deleted = new RPObject();
		added = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an slot with an object
	 * and we remove an attribute of latter, works, by building it again using
	 * applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void removeAttributeRPObjectOnRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.addSlot("lhand");

		RPObject sword = new RPObject();
		sword.put("type", "huge sword");
		obj.getSlot("lhand").add(sword);

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(result.hasSlot("lhand"));

		/* Clear the delta² data */
		obj.resetAddedAndDeleted();

		RPObject abiggersword = obj.getSlot("lhand").getFirst();
		abiggersword.remove("type");

		deleted = new RPObject();
		added = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an empty slot removed
	 * works, by building it again using applyDifferences.
	 * 
	 * @throws Exception
	 */
	@Test
	public void removedRPSlotWithRPObjectOnRPObject() throws Exception {
		RPObject obj = new RPObject();
		obj.put("id", 1);
		obj.addSlot("lhand");

		RPObject sword = new RPObject();
		sword.put("type", "huge sword");
		obj.getSlot("lhand").add(sword);

		RPObject deleted = new RPObject();
		RPObject added = new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result = new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		assertTrue(obj.hasSlot("lhand"));
		assertTrue(result.hasSlot("lhand"));

		/* Clear the delta² data */
		obj.resetAddedAndDeleted();

		obj.removeSlot("lhand");
		deleted = new RPObject();
		added = new RPObject();

		obj.getDifferences(added, deleted);

		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

}
