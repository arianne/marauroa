package marauroa.common.game.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.game.Attributes;
import marauroa.common.game.RPClass;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Test;


public class TestAttributes {
	
	@Test
	public void testPutHasGet() {
		Attributes attr=new Attributes(null);
		
		attr.put("a",1);
		attr.put("b","2");
		attr.put("c",3.0);
		
		assertTrue(attr.has("a"));
		assertTrue(attr.has("b"));
		assertTrue(attr.has("c"));
		assertFalse(attr.has("d"));
		
		assertEquals(1,attr.getInt("a"));
		assertEquals("2",attr.get("b"));
		assertEquals(3.0,attr.getDouble("c"));		
	}
	
	@Test
	public void testRemove() {
		Attributes attr=new Attributes(null);
		
		attr.put("a",1);
		
		assertTrue(attr.has("a"));
		assertFalse(attr.has("b"));
		
		assertEquals("1", attr.remove("a"));

		assertFalse(attr.has("a"));
		assertEquals(null, attr.remove("a"));
	}

	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		Attributes attr=new Attributes(RPClass.getBaseRPObjectDefault());
		
		attr.put("a",1);
		attr.put("b","2");
		attr.put("c",3.0);
		attr.put("e","a short string");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);
		
		os.write(attr);
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);
		
		Attributes result=(Attributes) is.readObject(new Attributes(null));
		
		assertEquals(attr, result);		
	}

	@Test(expected=IOException.class)
	public void testSerializationException() throws IOException, ClassNotFoundException {
		Attributes attr=new Attributes(RPClass.getBaseRPObjectDefault());
		
		attr.put("a",1);
		attr.put("b","2");
		attr.put("c",3.0);
		attr.put("d","a long string that I would hardly imagine how to add it because no language procesor would be able to handle a soooo long string without having problems with...");
		attr.put("e","a short string");
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);
		
		os.write(attr);
	}
}
