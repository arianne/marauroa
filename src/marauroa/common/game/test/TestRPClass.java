package marauroa.common.game.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.game.Attributes;
import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

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

		b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
		b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);
		b.add(DefinitionClass.ATTRIBUTE, "c", Type.STRING, Definition.STANDARD);

		short code=b.getCode(DefinitionClass.ATTRIBUTE, "a");
		assertEquals("a",b.getName(DefinitionClass.ATTRIBUTE, code));
	}

	@Test
	public void testDefinitionsMethods() {
		RPClass b=new RPClass("F");
		assertEquals(b, RPClass.getRPClass("F"));

		b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
		b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.HIDDEN);
		b.add(DefinitionClass.ATTRIBUTE, "c", Type.STRING, (byte)(Definition.PRIVATE|Definition.VOLATILE));

		Definition def=b.getDefinition(DefinitionClass.ATTRIBUTE, "a");
		assertEquals(Type.INT,def.getType());
		assertTrue(def.isVisible());
		assertFalse(def.isHidden());
		assertFalse(def.isPrivate());
		assertTrue(def.isStorable());

		def=b.getDefinition(DefinitionClass.ATTRIBUTE, "b");
		assertEquals(Type.FLAG,def.getType());
		assertFalse(def.isVisible());
		assertTrue(def.isHidden());
		assertFalse(def.isPrivate());
		assertTrue(def.isStorable());

		def=b.getDefinition(DefinitionClass.ATTRIBUTE, "c");
		assertEquals(Type.STRING,def.getType());
		assertFalse(def.isVisible());
		assertFalse(def.isHidden());
		assertTrue(def.isPrivate());
		assertFalse(def.isStorable());
	}

	@Test
	public void testGlobalDefinitionBug() {
		RPClass b=new RPClass("G");

		b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
		b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);

		RPClass c=new RPClass("H");

		c.add(DefinitionClass.ATTRIBUTE, "a", Type.STRING, Definition.STANDARD);
		c.add(DefinitionClass.ATTRIBUTE, "b", Type.FLOAT, Definition.HIDDEN);

		Definition defb=b.getDefinition(DefinitionClass.ATTRIBUTE, "a");
		Definition defc=c.getDefinition(DefinitionClass.ATTRIBUTE, "a");

		assertFalse(defb.getType()==defc.getType());
	}

	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		RPClass expected=new RPClass("I");
		assertEquals(expected, RPClass.getRPClass("I"));

		expected.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
		expected.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.HIDDEN);
		expected.add(DefinitionClass.ATTRIBUTE, "c", Type.STRING, (byte)(Definition.PRIVATE|Definition.VOLATILE));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(expected);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		RPClass result=(RPClass) is.readObject(new RPClass());

		assertEquals(expected, result);
	}

	@Test
	public void testStaticAttributes() {
		RPClass b=new RPClass("J");

		b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
		b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);
		b.add(DefinitionClass.STATIC, "c", "test", Definition.STANDARD);

		Attributes attr=new Attributes(b);
		attr.put("a",10);
		assertTrue(attr.has("a"));
		assertFalse(attr.has("b"));
		assertTrue(attr.has("c"));
	}
}
