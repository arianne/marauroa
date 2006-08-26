/* $Id: ByteCounterOutputStream.java,v 1.4 2006/08/26 20:00:30 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net;

import java.io.IOException;
import java.io.OutputStream;

/** This class just counts the bytes written into underlaying outputstream */
public class ByteCounterOutputStream extends OutputStream {
	OutputStream os;

	long bytesWritten;

	public ByteCounterOutputStream(OutputStream os) {
		if (os == null) {
			throw new NullPointerException("OutputStream is null!!!");
		}
		this.os = os;
		bytesWritten = 0;
	}

	@Override
	public void write(int b) throws IOException {
		os.write(b);
		bytesWritten++;
	}

	@Override
	public void write(byte[] b) throws IOException {
		os.write(b);
		bytesWritten += b.length;
	}

	public long getBytesWritten() {
		return (bytesWritten);
	}

	@Override
	public void flush() throws IOException {
		os.flush();
	}

	@Override
	public void close() throws IOException {
		os.close();
	}
}
