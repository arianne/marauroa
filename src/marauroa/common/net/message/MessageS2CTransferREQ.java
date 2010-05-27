/* $Id: MessageS2CTransferREQ.java,v 1.6 2010/05/27 19:13:32 nhnb Exp $ */
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

/**
 * Prior to transfer we send client a transfer offer so it can decide whenever to ACK it and have it
 * transfered or rejected it and use a local cache instead.
 *
 * @author miguel
 *
 */
public class MessageS2CTransferREQ extends Message {

	private List<TransferContent> contents;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CTransferREQ() {
		super(MessageType.S2C_TRANSFER_REQ, null);
	}

	/**
	 * creates a new MessageS2CTransferREQ
	 * @param source   socket channel
	 * @param contents content offered for transfer
	 */
	public MessageS2CTransferREQ(SocketChannel source, List<TransferContent> contents) {
		super(MessageType.S2C_TRANSFER_REQ, source);

		this.contents = contents;
	}

	/**
	 * gets the content
	 *
	 * @return content
	 */
	public List<TransferContent> getContents() {
		return contents;
	}

	@Override
	public String toString() {
		StringBuffer st = new StringBuffer("Message (S2C Transfer REQ) from (" + getAddress()
		        + ") CONTENTS: (");
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
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);

		int size = contents.size();
		out.write(size);

		for (TransferContent content : contents) {
			content.writeREQ(out);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		int size = in.readInt();
		contents = new LinkedList<TransferContent>();

		for (int i = 0; i < size; i++) {
			TransferContent content = new TransferContent();
			content.readREQ(in);
			contents.add(content);
		}

		if (type != MessageType.S2C_TRANSFER_REQ) {
			throw new IOException();
		}
	}
}
