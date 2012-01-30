/***************************************************************************
 *                   (C) Copyright 2003-2008 - Marauroa                    *
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Test;

/**
 * This test unit run test over RPClass and make some usage of Attributes too.
 * 
 * @author miguel, durkham
 * 
 */
public class RPClassTest {

    /**
     * This test check that has class works and that RPClass constructor works
     * as expected by adding the class to a global definition.
     */
    @Test
    public void testHasClass() {
        new RPClass("RPClassTest::A");
        assertTrue(RPClass.hasRPClass("RPClassTest::A"));
    }

    /**
     * In the same way that has test, we test get method so that is must return
     * the correct instance of RPClass.
     * 
     */
    @Test
    public void testGetClass() {
        RPClass b = new RPClass("RPClassTest::B");
        assertEquals(b, RPClass.getRPClass("RPClassTest::B"));
    }

    /**
     * This test is method isA works, by defining two classes and create one as
     * subclass of the other. It test both isA methods: String and RPClass.
     */
    @Test
    public void testisAClass() {
        RPClass c = new RPClass("RPClassTest::C");
        RPClass d = new RPClass("RPClassTest::D");

        d.isA(c);

        assertTrue(d.subclassOf(c.getName()));

        d.isA("RPClassTest::C");

        assertTrue(d.subclassOf(c.getName()));
    }

    /**
     * Create some definitions for data in a RPClass and check them.
     * 
     */
    @Test
    public void testDefinitions() {
        RPClass b = new RPClass("RPClassTest::E");
        assertEquals(b, RPClass.getRPClass("RPClassTest::E"));

        b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
        b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);
        b.add(DefinitionClass.ATTRIBUTE, "c", Type.STRING, Definition.STANDARD);

        short code = b.getCode(DefinitionClass.ATTRIBUTE, "a");
        assertEquals("a", b.getName(DefinitionClass.ATTRIBUTE, code));
    }

    /**
     * Check if definition methods: isVisible, isHidden, isPrivate and
     * isStorable works as expected by defining some of them.
     * 
     */
    @Test
    public void testDefinitionsMethods() {
        RPClass b = new RPClass("RPClassTest::F");
        assertEquals(b, RPClass.getRPClass("RPClassTest::F"));

        b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
        b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.HIDDEN);
        b.add(DefinitionClass.ATTRIBUTE, "c", Type.STRING,
                (byte) (Definition.PRIVATE | Definition.VOLATILE));

        Definition def = b.getDefinition(DefinitionClass.ATTRIBUTE, "a");
        assertEquals(Type.INT, def.getType());
        assertTrue(def.isVisible());
        assertFalse(def.isHidden());
        assertFalse(def.isPrivate());
        assertTrue(def.isStorable());

        def = b.getDefinition(DefinitionClass.ATTRIBUTE, "b");
        assertEquals(Type.FLAG, def.getType());
        assertFalse(def.isVisible());
        assertTrue(def.isHidden());
        assertFalse(def.isPrivate());
        assertTrue(def.isStorable());

        def = b.getDefinition(DefinitionClass.ATTRIBUTE, "c");
        assertEquals(Type.STRING, def.getType());
        assertFalse(def.isVisible());
        assertFalse(def.isHidden());
        assertTrue(def.isPrivate());
        assertFalse(def.isStorable());
    }

    /**
     * This test case shows a bug fix for a Marauroa 1.3x bug where two
     * attributes definition even in diferent classes where created as the same
     * one ( ignoring the second definition ).
     * 
     * For example A ( id string ) B ( id int )
     * 
     * They are different attributes and of different type. Check that it is
     * true.
     * 
     */
    @Test
    public void testGlobalDefinitionBug() {
        RPClass b = new RPClass("RPClassTest::G");

        b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
        b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);

        RPClass c = new RPClass("H");

        c.add(DefinitionClass.ATTRIBUTE, "a", Type.STRING, Definition.STANDARD);
        c.add(DefinitionClass.ATTRIBUTE, "b", Type.FLOAT, Definition.HIDDEN);

        Definition defb = b.getDefinition(DefinitionClass.ATTRIBUTE, "a");
        Definition defc = c.getDefinition(DefinitionClass.ATTRIBUTE, "a");

        assertFalse(defb.getType() == defc.getType());
    }

    /**
     * Test serialization code of RPClass by serializing it and then
     * deserializing the stream again and checking that it is the same RPClass.
     * 
     * @throws IOException
     */
    @Test
    public void testSerialization() throws IOException {
        RPClass expected = new RPClass("RPClassTest::I");
        assertEquals(expected, RPClass.getRPClass("RPClassTest::I"));

        expected.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
        expected.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.HIDDEN);
        expected.add(DefinitionClass.ATTRIBUTE, "c", Type.STRING, (byte) (Definition.PRIVATE | Definition.VOLATILE));
        expected.add(DefinitionClass.RPSLOT, "d", 1, Definition.HIDDEN);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputSerializer os = new OutputSerializer(out);

        os.write(expected);

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        InputSerializer is = new InputSerializer(in);

        RPClass result = (RPClass) is.readObject(new RPClass());

        assertEquals(expected, result);
    }

    /**
     * Test the creation of static attributes for Marauroa 2.0. Check they work
     * by creating an attribute object.
     * 
     */
    @Test
    public void testStaticAttributes() {
        RPClass b = new RPClass("RPClassTest::J");

        b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
        b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);
        b.add(DefinitionClass.STATIC, "c", "test", Definition.STANDARD);

        Attributes attr = new Attributes(b);
        attr.put("a", 10);
        assertTrue(attr.has("a"));
        assertFalse(attr.has("b"));
        assertTrue(attr.has("c"));
    }

    /**
     * Test the hierachy process.
     * This test verify bug, that caused codes to be badly resolve, is fixed.
     * 
     */
    @Test
    public void testHierachyBug() {
        RPClass b = new RPClass("RPClassTest::K");
        b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
        b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);
        b.add(DefinitionClass.STATIC, "c", "test", Definition.STANDARD);

        RPClass c = new RPClass("RPClassTest::M");
        c.isA(b);
        c.add(DefinitionClass.ATTRIBUTE, "a", Type.STRING, Definition.STANDARD);
        c.add(DefinitionClass.STATIC, "c", "subclass", Definition.STANDARD);
        

        Attributes attr = new Attributes(c);
        attr.put("a", 10);
        
        assertTrue(attr.has("a"));
        assertFalse(attr.has("b"));
        assertTrue(attr.has("c"));
        assertEquals("subclass",attr.get("c"));

        Definition def=c.getDefinition(DefinitionClass.ATTRIBUTE, "a");
        assertEquals(Type.STRING,def.getType());
        short code=def.getCode();
        
        assertEquals("a",c.getName(DefinitionClass.ATTRIBUTE, code));
    }

	@Test
	public void testEqualsNewObjectsName() throws Exception {
        RPClass b = new RPClass("RPClassTest::L");
        b.add(DefinitionClass.ATTRIBUTE, "a", Type.INT, Definition.STANDARD);
        b.add(DefinitionClass.ATTRIBUTE, "b", Type.FLAG, Definition.STANDARD);
        b.add(DefinitionClass.STATIC, "c", "test", Definition.STANDARD);

        assertEquals("Check equals method",
				b,
				b);
	}
	
}
