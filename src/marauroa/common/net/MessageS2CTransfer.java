/* $Id: MessageS2CTransfer.java,v 1.6 2006/08/26 20:00:30 nhnb Exp $ */
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

public class MessageS2CTransfer extends Message {
	/** TODO: Compress all the data that we send */
	private List<TransferContent> contents;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CTransfer() {
		super(MessageType.S2C_TRANSFER, null);
	}

	public MessageS2CTransfer(InetSocketAddress source, TransferContent content) {
		super(MessageType.S2C_TRANSFER, source);

		this.contents = new LinkedList<TransferContent>();
		contents.add(content);
	}

	public List<TransferContent> getContents() {
		return contents;
	}

	@Override
	public String toString() {
		StringBuffer st = new StringBuffer("Message (S2C Transfer) from ("
				+ source.getAddress().getHostAddress() + ") CONTENTS: (");
		for (TransferContent content : contents) {
			st.append("[");
			st.append(content.name);
			st.append(":");
			st.append(content.timestamp);
			st.append("]");
		}
		st.append(")");

		return st.toString();
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out)
			throws IOException {
		super.writeObject(out);

		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer serializer = new OutputSerializer(out_stream);

		int size = contents.size();
		serializer.write(size);

		for (TransferContent content : contents) {
			content.writeFULL(serializer);
		}

		out_stream.close();

		out.write(array.toByteArray());
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, ClassNotFoundException {
		super.readObject(in);

		ByteArrayInputStream array = new ByteArrayInputStream(in
				.readByteArray());
		java.util.zip.InflaterInputStream szlib = new java.util.zip.InflaterInputStream(
				array, new java.util.zip.Inflater());
		InputSerializer serializer = new InputSerializer(szlib);

		int size = serializer.readInt();
		contents = new LinkedList<TransferContent>();

		for (int i = 0; i < size; i++) {
			TransferContent content = new TransferContent();
			content.readFULL(serializer);
			contents.add(content);
		}

		if (type != MessageType.S2C_TRANSFER) {
			throw new java.lang.ClassNotFoundException();
		}
	}
}
