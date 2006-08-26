/* $Id: TransferContent.java,v 1.5 2006/08/26 20:00:30 nhnb Exp $ */
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

public class TransferContent {
	public String name;

	public int timestamp;

	public byte[] data;

	public boolean cacheable;

	public boolean ack;

	public TransferContent() {
	}

	@Override
	public String toString() {
		StringBuffer sstr = new StringBuffer();

		sstr.append("TransferContent: [name=\"");
		sstr.append(name);
		sstr.append("\" timestamp=");
		sstr.append(timestamp);
		sstr.append(" data=\"");
		sstr.append(data);
		sstr.append("\"]");

		return sstr.toString();
	}

	public TransferContent(String name, int timestamp, byte[] data) {
		this.name = name;
		this.timestamp = timestamp;
		this.data = data;
		cacheable = true;
		ack = false;
	}

	public void writeREQ(marauroa.common.net.OutputSerializer out)
			throws IOException {
		out.write(name);
		out.write(timestamp);
		out.write((byte) (cacheable ? 1 : 0));
	}

	public void readREQ(marauroa.common.net.InputSerializer in)
			throws IOException, ClassNotFoundException {
		name = in.readString();
		timestamp = in.readInt();
		cacheable = (in.readByte() == 1);
	}

	public void writeACK(marauroa.common.net.OutputSerializer out)
			throws IOException {
		out.write(name);
		out.write((byte) (ack ? 1 : 0));
	}

	public void readACK(marauroa.common.net.InputSerializer in)
			throws IOException, ClassNotFoundException {
		name = in.readString();
		ack = (in.readByte() == 1);
	}

	public void writeFULL(marauroa.common.net.OutputSerializer out)
			throws IOException {
		out.write(name);
		out.write(data);
		out.write(timestamp);
		out.write((byte) (cacheable ? 1 : 0));
	}

	public void readFULL(marauroa.common.net.InputSerializer in)
			throws IOException, ClassNotFoundException {
		name = in.readString();
		data = in.readByteArray();
		timestamp = in.readInt();
		cacheable = (in.readByte() == 1);
	}
}
