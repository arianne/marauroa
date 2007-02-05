/* $Id: MessageS2CTransferREQ.java,v 1.1 2007/02/05 18:37:42 arianne_rpg Exp $ */
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
package marauroa.common.net.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.net.TransferContent;

public class MessageS2CTransferREQ extends Message {
	private List<TransferContent> contents;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CTransferREQ() {
		super(MessageType.S2C_TRANSFER_REQ, null);
	}

	public MessageS2CTransferREQ(SocketChannel source, List<TransferContent> contents) {
		super(MessageType.S2C_TRANSFER_REQ, source);

		this.contents = contents;
	}

	public List<TransferContent> getContents() {
		return contents;
	}

	@Override
	public String toString() {
		StringBuffer st = new StringBuffer("Message (S2C Transfer REQ) from ("
				+ getAddress() + ") CONTENTS: (");
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

		int size = contents.size();
		out.write(size);

		for (TransferContent content : contents) {
			content.writeREQ(out);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in)
			throws IOException, ClassNotFoundException {
		super.readObject(in);

		int size = in.readInt();
		contents = new LinkedList<TransferContent>();

		for (int i = 0; i < size; i++) {
			TransferContent content = new TransferContent();
			content.readREQ(in);
			contents.add(content);
		}

		if (type != MessageType.S2C_TRANSFER_REQ) {
			throw new java.lang.ClassNotFoundException();
		}
	}
}
