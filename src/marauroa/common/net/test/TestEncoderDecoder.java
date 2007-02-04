package marauroa.common.net.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import marauroa.common.net.Decoder;
import marauroa.common.net.Encoder;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.Message;
import marauroa.common.net.MessageC2SAction;


public class TestEncoderDecoder {
	@BeforeClass
	public static void initialize() throws Exception {
		Log4J.init("marauroa/server/log4j.properties");
	}
	
	@Test
	public void testEncoderDecoder_single() throws IOException, InvalidVersionException {
		Encoder enc=Encoder.get();
		
		RPAction action=new RPAction();
		action.put("one",1);
		action.put("two","2");
		
		MessageC2SAction message=new MessageC2SAction(null,action);
		
		byte[] result=enc.encode(message);
		
		Decoder dec=Decoder.get();
		
		Message decoded=dec.decode(null,result);
		byte[] reencoded=enc.encode(decoded);
		
		System.out.println(reencoded.length);
		
		assertEquals(result.length, reencoded.length);
		
		/** We verify the assertion by re encoding again the message.
		 *  Message.equals(Object ) is NOT implemented. */
		for(int i=0;i<result.length;i++) {
			assertEquals(result[i], reencoded[i]);
		}
	}

	@Test	
	public void testEncoderDecoder_multiple() throws IOException, InvalidVersionException {
		Encoder enc=Encoder.get();
		
		RPAction action=new RPAction();
		action.put("one",1);
		action.put("two","2");
		
		MessageC2SAction message=new MessageC2SAction(null,action);
		
		byte[] result=enc.encode(message);
		
		Decoder dec=Decoder.get();

		int split=new Random().nextInt(result.length);
		byte[] part1=new byte[split];
		System.arraycopy(result, 0 , part1, 0, split);

		byte[] part2=new byte[result.length-split];
		System.arraycopy(result, split , part2, 0, result.length-split);
		
		assertEquals(result.length, part1.length+part2.length);
		
		Message decoded=null;
		decoded=dec.decode(null,part1);
		assertNull(decoded);
		decoded=dec.decode(null,part2);
		assertNotNull(decoded);
		
		byte[] reencoded=enc.encode(decoded);
		
		assertEquals(result.length, reencoded.length);
		
		/** We verify the assertion by re encoding again the message.
		 *  Message.equals(Object ) is NOT implemented. */
		for(int i=0;i<result.length;i++) {
			assertEquals(result[i], reencoded[i]);
		}
	}
}
