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
	@Test
	public void emptyRPObject() throws Exception {
		RPObject obj=new RPObject();

		RPObject deleted=new RPObject();
		RPObject added=new RPObject();

		obj.getDifferences(added, deleted);

		RPObject result=new RPObject();
		result.applyDifferences(added, deleted);

		assertEquals(obj, result);

	}

}
