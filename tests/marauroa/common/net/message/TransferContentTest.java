/* $Id: TransferContentTest.java,v 1.7 2010/01/04 08:47:11 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.common.net.message;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

import org.junit.Test;

public class TransferContentTest {

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
		assertArrayEquals(bytearray, tc.data);
		assertEquals("name", tc.name);
		assertEquals(timestamp, tc.timestamp);
	}

	@Test
	public final void testReadWriteREQ() throws IOException {
		TransferContent tcInn = new TransferContent();
		tcInn.name="Test";
		tcInn.timestamp=123123;
		tcInn.cacheable=true;
		tcInn.data=new byte[64];
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		tcInn.writeREQ(new OutputSerializer(out));
		InputSerializer in = new InputSerializer(new ByteArrayInputStream(out.toByteArray()));

		TransferContent tcOut = new TransferContent();
		tcOut.readREQ(in);
		assertTrue(tcInn.ack == tcOut.ack);
		assertTrue(tcInn.cacheable == tcOut.cacheable);
		assertTrue(tcInn.timestamp == tcOut.timestamp);
		assertTrue(tcInn.name.equals(tcOut.name));

	}

	@Test
	public final void testReadWriteACK() throws IOException {
		TransferContent tcInn = new TransferContent();
		tcInn.name="Test";
		tcInn.timestamp=123123;
		tcInn.cacheable=true;
		tcInn.data=new byte[64];

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		tcInn.writeACK(new OutputSerializer(out));
		InputSerializer in = new InputSerializer(new ByteArrayInputStream(out.toByteArray()));

		TransferContent tcOut = new TransferContent();
		tcOut.readACK(in);
		assertTrue(tcInn.ack == tcOut.ack);
		assertTrue(tcInn.name.equals(tcOut.name));
	}

	@Test
	public final void testReadWriteFULL() throws IOException {
		TransferContent tcInn = new TransferContent();
		tcInn.name="Test";
		tcInn.timestamp=123123;
		tcInn.cacheable=true;
		tcInn.data=new byte[64];
		
		for(int i=0;i<64;i++) {
			tcInn.data[i]=(byte)i;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		tcInn.writeFULL(new OutputSerializer(out));
		InputSerializer in = new InputSerializer(new ByteArrayInputStream(out.toByteArray()));

		TransferContent tcOut = new TransferContent();
		tcOut.readFULL(in);
		assertEquals(tcInn.ack,tcOut.ack);
		assertEquals(tcInn.cacheable,tcOut.cacheable);
		for(int i=0;i<64;i++) {
		  assertEquals(tcInn.data[i],tcOut.data[i]);
		}
		assertEquals(tcInn.name,tcOut.name);

	}

}
