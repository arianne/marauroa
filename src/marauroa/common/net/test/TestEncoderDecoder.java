package marauroa.common.net.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Random;

import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the basic serialization schema.
 *
 * @author miguel
 *
 */
public class TestEncoderDecoder {

	/**
	 * Setup for class.
	 * It initialize the logger instance
	 * @throws Exception
	 */
	@BeforeClass
	public static void initialize() throws Exception {
		Log4J.init("marauroa/server/log4j.properties");
	}

	/**
	 * Test encoding and decoding works when we use the data as a single chunk.
	 * @throws IOException
	 * @throws InvalidVersionException
	 */
	@Test
	public void testEncoderDecoderSingle() throws IOException, InvalidVersionException {
		Encoder enc = Encoder.get();

		RPAction action = new RPAction();
		action.put("one", 1);
		action.put("two", "2");

		MessageC2SAction message = new MessageC2SAction(null, action);

		byte[] result = enc.encode(message);

		Decoder dec = Decoder.get();

		Message decoded = dec.decode(null, result);
		byte[] reencoded = enc.encode(decoded);

		assertEquals(result.length, reencoded.length);

		/** We verify the assertion by re encoding again the message.
		 *  Message.equals(Object ) is NOT implemented. */
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], reencoded[i]);
		}
	}

	/**
	 * Test that encoder and decoder works when we use several chunks of data.
	 * @throws IOException
	 * @throws InvalidVersionException
	 */
	@Test
	public void testEncoderDecoderMultiple() throws IOException, InvalidVersionException {
		Encoder enc = Encoder.get();

		RPAction action = new RPAction();
		action.put("one", 1);
		action.put("two", "2");

		MessageC2SAction message = new MessageC2SAction(null, action);

		byte[] result = enc.encode(message);

		Decoder dec = Decoder.get();

		int split = new Random().nextInt(result.length - 4) + 4;
		byte[] part1 = new byte[split];
		System.arraycopy(result, 0, part1, 0, split);

		byte[] part2 = new byte[result.length - split];
		System.arraycopy(result, split, part2, 0, result.length - split);

		assertEquals(result.length, part1.length + part2.length);

		Message decoded = null;
		decoded = dec.decode(null, part1);
		assertNull(decoded);
		decoded = dec.decode(null, part2);
		assertNotNull(decoded);

		byte[] reencoded = enc.encode(decoded);

		assertEquals(result.length, reencoded.length);

		/** We verify the assertion by re encoding again the message.
		 *  Message.equals(Object ) is NOT implemented. */
		for (int i = 0; i < result.length; i++) {
			assertEquals(result[i], reencoded[i]);
		}
	}
}
