package marauroa.common.game.test;

import static org.junit.Assert.*;

import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import marauroa.common.game.Definition.Type;

import org.junit.Test;


public class TestRPClass {

	@Test
	public void testHasClass() {
		@SuppressWarnings("unused")
		RPClass a=new RPClass("A");
		assertTrue(RPClass.hasRPClass("A"));		
	}

	@Test
	public void testGetClass() {
		RPClass b=new RPClass("B");
		assertEquals(b, RPClass.getRPClass("B"));		
	}

	@Test
	public void testisAClass() {
		RPClass c=new RPClass("C");
		RPClass d=new RPClass("D");

		d.isA(c);
		
		assertTrue(d.subclassOf(c.getName()));
		
		d.isA("C");

		assertTrue(d.subclassOf(c.getName()));
		}

	@Test
	public void testDefinitions() {
		RPClass b=new RPClass("E");
		assertEquals(b, RPClass.getRPClass("E"));
		
		b.add(Type.ATTRIBUTE, "a", Definition.INT, Definition.STANDARD);
		b.add(Type.ATTRIBUTE, "b", Definition.FLAG, Definition.STANDARD);
		b.add(Type.ATTRIBUTE, "c", Definition.STRING, Definition.STANDARD);
		
		short code=b.getCode(Type.ATTRIBUTE, "a");
		assertEquals("a",b.getName(Type.ATTRIBUTE, code));
	}

	@Test
	public void testDefinitionsMethods() {
		RPClass b=new RPClass("F");
		assertEquals(b, RPClass.getRPClass("F"));

		b.add(Type.ATTRIBUTE, "a", Definition.INT, Definition.STANDARD);
		b.add(Type.ATTRIBUTE, "b", Definition.FLAG, Definition.HIDDEN);
		b.add(Type.ATTRIBUTE, "c", Definition.STRING, (byte)(Definition.PRIVATE|Definition.VOLATILE));

		Definition def=b.getDefinition(Type.ATTRIBUTE, "a");
		assertEquals(Definition.INT,def.getType());
		assertTrue(def.isVisible());
		assertFalse(def.isHidden());
		assertFalse(def.isPrivate());
		assertTrue(def.isStorable());

		def=b.getDefinition(Type.ATTRIBUTE, "b");
		assertEquals(Definition.FLAG,def.getType());
		assertFalse(def.isVisible());
		assertTrue(def.isHidden());
		assertFalse(def.isPrivate());
		assertTrue(def.isStorable());

		def=b.getDefinition(Type.ATTRIBUTE, "c");
		assertEquals(Definition.STRING,def.getType());
		assertFalse(def.isVisible());
		assertFalse(def.isHidden());
		assertTrue(def.isPrivate());
		assertFalse(def.isStorable());
	}
}
