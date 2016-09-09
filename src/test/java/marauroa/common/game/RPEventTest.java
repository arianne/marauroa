/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Test;

/**
 * Test unit for RPEvent class
 *
 * @author miguel
 *
 */
public class RPEventTest {

	/**
	 * Test the methods provided by RPEvent
	 *
	 */
	@Test
	public void methods() {
		RPEvent event = new RPEvent("test");
		event.put("value", "val");

		assertEquals("test", event.getName());
		assertTrue(event.has("value"));
		assertEquals("val", event.get("value"));
	}

	/**
	 * Test the clone method.
	 *
	 */
	@Test
	public void testClone() {
		RPEvent event = new RPEvent("test");
		event.put("value", "val");

		assertEquals(event, event.clone());
	}

	/**
	 * Test serialization of a RPEvent by serializing into a stream and
	 * deserializing it bak again. This test uses RPObject as they are needed to
	 * obtain the RPEvent code definition.
	 *
	 * @throws IOException
	 */
	@Test
	public void testSerialization() throws IOException {
		RPObject obj = new RPObject();

		RPClassTestHelper.generateRPClasses();
		RPEvent expected = new RPEvent("test");
		expected.put("value", "work!");
		expected.setOwner(obj);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(expected);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		RPEvent result = (RPEvent) is.readObject(new RPEvent());

		assertEquals(expected, result);
	}

	/**
	 * Test serialization of a RPEvent by serializing into a stream and
	 * deserializing it bak again. This test uses RPObject as they are needed to
	 * obtain the RPEvent code definition. This test also uses RPClass
	 * definition
	 *
	 * @throws IOException
	 */
	@Test
	public void testSerializationWithRPClass() throws IOException {
		RPClass clazz = new RPClass("A");

		clazz.add(DefinitionClass.RPEVENT, "test", Definition.STANDARD);

		RPObject obj = new RPObject();
		obj.setRPClass(clazz);

		RPEvent expected = new RPEvent("test");
		expected.put("value", "work!");
		expected.setOwner(obj);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(expected);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		RPEvent result = new RPEvent();
		result.setOwner(obj);
		result = (RPEvent) is.readObject(result);

		assertEquals(expected, result);
	}
}
