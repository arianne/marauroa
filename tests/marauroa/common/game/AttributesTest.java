package marauroa.common.game;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Test;

/**
 * Test case for attributes.
 *
 * @author miguel
 *
 */
public class AttributesTest {

	/**
	 * Test if methods put, has and get of attributes work as expected. It add
	 * and attribute, then assert it is present and finally compare the values.
	 * It also assert that a non added attribute doesn't exists.
	 */
	@Test
	public void testPutHasGet() {
		Attributes attr = new Attributes(null);

		attr.put("a", 1);
		attr.put("b", "2");
		attr.put("c", 3.0);

		assertTrue(attr.has("a"));
		assertTrue(attr.has("b"));
		assertTrue(attr.has("c"));
		assertFalse(attr.has("d"));

		assertEquals(1, attr.getInt("a"));
		assertEquals("2", attr.get("b"));
		assertEquals(3.0, attr.getDouble("c"));
	}

	/**
	 * Test if an attribute is removed when it is removed. assert that the
	 * attribute is not longer there.
	 *
	 */
	@Test
	public void testRemove() {
		Attributes attr = new Attributes(null);

		attr.put("a", 1);

		assertTrue(attr.has("a"));
		assertFalse(attr.has("b"));

		assertEquals("1", attr.remove("a"));

		assertFalse(attr.has("a"));
		assertEquals(null, attr.remove("a"));
	}

	/**
	 * Test the serialization process of an attribute. It serialize the
	 * attribute and then deserialize it and check they are the same.
	 *
	 * @throws IOException
	 *             if there is a problem serializing the data.
	 * @throws ClassNotFoundException
	 */
	@Test
	public void testSerialization() throws IOException {
		Attributes attr = new Attributes(RPClass.getBaseRPObjectDefault());

		attr.put("a", 1);
		attr.put("b", "2");
		attr.put("c", 3.0);
		attr.put("e", "a short string");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(attr);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		Attributes result = (Attributes) is.readObject(new Attributes(null));

		assertEquals(attr, result);
	}

	/**
	 * Test the serialization process of an attribute with a defined RPClass It
	 * serialize the attribute and then deserialize it and check they are the
	 * same.
	 *
	 * @throws IOException
	 *             if there is a problem serializing the data.
	 * @throws ClassNotFoundException
	 */
	@Test
	public void testSerializationWithRPClass() throws IOException {
		RPClass clazz = new RPClass("A");

		clazz.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "b", Type.STRING, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "c", Type.FLOAT, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "d", Type.BYTE, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "e", Type.SHORT, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "f", Type.LONG_STRING, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "g", Type.VERY_LONG_STRING, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "h", Type.FLAG, Definition.STANDARD);

		Attributes attr = new Attributes(clazz);

		attr.put("a", 1);
		attr.put("b", "2");
		attr.put("c", 3.0);
		attr.put("d", 120);
		attr.put("e", 15000);
		attr.put("f",
		                "This is a loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong stream but it think we can make even longer with a biiiiiiiiiiiiiiiiiiiiiiiiiiiiiiit of heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeelp frooooooooooooooooooooooooooom users all around the world");
		attr.put("g", "Toooooooo big to even test the limit");
		attr.put("h", "");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(attr);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		Attributes result = (Attributes) is.readObject(new Attributes(null));

		assertEquals(attr, result);
	}

	/**
	 * Test the serialization process of an attribute with a defined RPClass It
	 * serialize the attribute and then deserialize it and check they are the
	 * same.
	 *
	 * @throws IOException
	 *             if there is a problem serializing the data.
	 * @throws ClassNotFoundException
	 */
	@Test(expected = IOException.class)
	public void testSerializationWithRPClassFailure() throws IOException {
		RPClass clazz = new RPClass("A");

		clazz.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "b", Type.STRING, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "c", Type.FLOAT, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "d", Type.BYTE, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "e", Type.SHORT, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "f", Type.STRING, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "g", Type.VERY_LONG_STRING, Definition.STANDARD);
		clazz.add(DefinitionClass.ATTRIBUTE, "h", Type.FLAG, Definition.STANDARD);

		Attributes attr = new Attributes(clazz);

		attr.put("a", 1);
		attr.put("b", "2");
		attr.put("c", 3.0);
		attr.put("d", 120);
		attr.put("e", 15000);
		attr.put("f",
		                "This is a loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong stream but it think we can make even longer with a biiiiiiiiiiiiiiiiiiiiiiiiiiiiiiit of heeeeeeeeeeeeeeeeeeeeeeeeeeeeeeelp frooooooooooooooooooooooooooom users all around the world");
		attr.put("g", "Toooooooo big to even test the limit");
		attr.put("h", "");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(attr);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		Attributes result = (Attributes) is.readObject(new Attributes(null));

		assertEquals(attr, result);
	}
	
	/**
	 * Assert that no exception is thrown when a long string is added to
	 * to rpclassless attributes.
	 *
	 * @throws IOException in case of an unexspected error
	 */
	public void testSerializationOfClasslessAttributesWithALongString() throws IOException {
		Attributes attr = new Attributes(RPClass.getBaseRPObjectDefault());

		attr.put("a", 1);
		attr.put("b", "2");
		attr.put("c", 3.0);
		attr.put("d", "a long string that I would hardly imagine how to add it " +
				"because no language procesor would be able to handle a " +
				"soooooooooooooooooooooooooooooooooooooooooooooooooooooooo long " +
				"string without having problems with... But as we do have a limit " +
				"of 256 bytes for short strings, we need a way to test it.");
		System.out.println(attr.get("d").length());
		attr.put("e", "a short string");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(attr);
	}

	/**
	 * Test equals method.
	 *
	 */
	@Test
	public void testEquals() {
		Attributes attr = new Attributes(RPClass.getBaseRPObjectDefault());

		attr.put("pepe", "");
		attr.put("john", "");
		attr.put("anton", "");

		Attributes other = new Attributes(RPClass.getBaseRPObjectDefault());

		other.put("pepe", "");
		other.put("anton", "");
		other.put("john", "");

		System.out.println(attr);
		System.out.println(other);

		assertEquals(attr, other);
	}

}
