package marauroa.common.game.test;

import static org.junit.Assert.*;
import marauroa.common.game.RPObject;

import org.junit.Test;

/**
 * This class test the getDifferences and applyDifferences methods
 * used at the Delta² algorithm.
 * @author miguel
 *
 */
public class TestGetAndApplyDifferences {
	/**
	 * Test if the getDiferences of an empty object works, by building it again
	 * using applyDifferences.
	 *
	 * @throws Exception
	 */
	@Test
	public void emptyRPObject() throws Exception {
		RPObject obj=new RPObject();
		obj.put("id", 1);

		RPObject deleted=new RPObject();
		RPObject added=new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result=new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an attribute added works, by building it again
	 * using applyDifferences.
	 *
	 * @throws Exception
	 */
	@Test
	public void addedAttributeRPObject() throws Exception {
		RPObject obj=new RPObject();
		obj.put("id", 1);
		obj.put("test", "val");

		RPObject deleted=new RPObject();
		RPObject added=new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result=new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}

	/**
	 * Test if the getDiferences of an object that has an attribute modified works, by building it again
	 * using applyDifferences.
	 *
	 * @throws Exception
	 */
	@Test
	public void modifiedAttributeRPObject() throws Exception {
		RPObject obj=new RPObject();
		obj.put("id", 1);
		obj.put("test", "val");

		RPObject deleted=new RPObject();
		RPObject added=new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result=new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

		obj.put("test", "another val");
		deleted=new RPObject();
		added=new RPObject();

		obj.getDifferences(added, deleted);

		result=new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);
	}
}
