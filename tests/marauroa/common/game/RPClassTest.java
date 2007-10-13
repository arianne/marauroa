package marauroa.common.game;

import static org.junit.Assert.*;

import org.junit.Test;

public class RPClassTest {

	@Test
	public void testEqualsNewObjects() throws Exception {
		assertEquals("two new Objects should be equal", new RPClass(),
				new RPClass());

	}

	@Test
	public void testEqualsNewObjectsName() throws Exception {
		assertEquals("two new Objects should be equal if Name is Equal",
				new RPClass(new String("name")),
				new RPClass(new String("name")));
		assertFalse("two new Objects should not be equal if Name is not Equal",
				new RPClass("name").equals(new RPClass("name2")));
	}

	@Test
	public void testRPClass() throws Exception {
		RPClass rp = new RPClass("schnipp");
		RPClass rp2 = new RPClass("schnipp");
		RPClass childRp = new RPClass(""); //not intializing name causes NPE in subclassOf
		RPClass childRp2 = new RPClass("");
		childRp.isA(rp);
		childRp2.isA(childRp);
		assertTrue(childRp.subclassOf(rp.getName()));
		assertTrue(childRp.subclassOf(rp2.getName()));
		assertTrue(childRp2.subclassOf(rp.getName()));
	}

}
