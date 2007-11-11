package marauroa.common.net.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TransferContentTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testTransferContent() {
		TransferContent tc = new TransferContent();
		assertEquals(false, tc.ack);
		assertEquals(false, tc.cacheable);
		assertEquals(null, tc.data);
		assertEquals(null, tc.name);
		assertEquals(0, tc.timestamp);
	}

	@Test
	public final void testTransferContentStringIntByteArray() {
		byte[] bytearray = new byte[] { 1, 2, 3 };
		int timestamp = 2;
		TransferContent tc = new TransferContent("name", timestamp, bytearray);
		assertEquals(false, tc.ack);
		assertEquals(true, tc.cacheable);
		assertEquals(bytearray, tc.data);
		assertEquals("name", tc.name);
		assertEquals(timestamp, tc.timestamp);
	}

	@Ignore
	@Test
	public final void testReadWriteREQ() throws IOException {
		// TODO: You can't read if there is nothing written.
		TransferContent tcOut = new TransferContent();
		TransferContent tcInn = new TransferContent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		tcInn.writeREQ(new OutputSerializer(out));
		InputSerializer in = new InputSerializer(new ByteArrayInputStream(out.toByteArray()));

		tcOut.readREQ(in);
		assertTrue(tcInn.ack == tcOut.ack);
		assertTrue(tcInn.cacheable == tcOut.cacheable);
		assertTrue(tcInn.data == tcOut.data);
		assertTrue(tcInn.name.equals(tcOut.name));

	}

	@Ignore
	@Test
	public final void testReadWriteACK() throws IOException {
		// TODO: You can't read if there is nothing written.
		TransferContent tcOut = new TransferContent();
		TransferContent tcInn = new TransferContent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		tcInn.writeACK(new OutputSerializer(out));
		InputSerializer in = new InputSerializer(new ByteArrayInputStream(out.toByteArray()));

		tcOut.readACK(in);
		assertTrue(tcInn.ack = tcOut.ack);
		assertTrue(tcInn.cacheable = tcOut.cacheable);
		assertTrue(tcInn.data == tcOut.data);
		assertTrue(tcInn.name.equals(tcOut.name));
	}

	@Ignore
	@Test
	public final void testReadWriteFULL() throws IOException {
		// TODO: You can't read if there is nothing written.
		TransferContent tcOut = new TransferContent();
		TransferContent tcInn = new TransferContent();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		tcInn.writeFULL(new OutputSerializer(out));
		InputSerializer in = new InputSerializer(new ByteArrayInputStream(out.toByteArray()));

		tcOut.readFULL(in);
		assertTrue(tcInn.ack == tcOut.ack);
		assertTrue(tcInn.cacheable == tcOut.cacheable);
		assertTrue(tcInn.data == tcOut.data);
		assertTrue(tcInn.name.equals(tcOut.name));

	}

}
