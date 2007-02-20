package marauroa.common.game.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Test;


public class TestRPEvent {
	@Test
	public void testSerialization() throws IOException, ClassNotFoundException {
		RPObject obj=new RPObject();

		RPEvent expected=new RPEvent(obj, "test", "work!");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputSerializer os = new OutputSerializer(out);

		os.write(expected);

		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		InputSerializer is = new InputSerializer(in);

		RPEvent result=(RPEvent) is.readObject(new RPEvent(obj));

		assertEquals(expected, result);
	}
}
