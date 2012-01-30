/***************************************************************************
 *                   (C) Copyright 2003-2007 - Marauroa                    *
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
 * This message is for confirming server the content we want to be transfered to
 * us. This way client can implement a cache system to save bandwidth.
 *
 * @author miguel
 *
 */
public class MessageC2STransferACK extends Message {

	private List<TransferContent> contents;

	/** Constructor for allowing creation of an empty message */
	public MessageC2STransferACK() {
		super(MessageType.C2S_TRANSFER_ACK, null);
	}

	/**
	 * Constructor with a TCP/IP source/destination of the message and a list of the contents
	 * to confirm to server.
	 *
	 * @param source
	 *            The TCP/IP address associated to this message
	 * @param content
	 * 			  the list of contents to confirm to server.
	 */
	public MessageC2STransferACK(SocketChannel source, List<TransferContent> content) {
		super(MessageType.C2S_TRANSFER_ACK, source);

		this.contents = content;
	}

	/**
	 * Returns the list of contents that have been confirmed.
	 * @return the list of contents that have been confirmed.
	 */
	public List<TransferContent> getContents() {
		return contents;
	}

	@Override
	public String toString() {
		return "Message (C2S Transfer ACK) from (" + getAddress() + ") CONTENTS: ("
		        + contents.size() + ")";
	}

	@Override
	public void writeObject(marauroa.common.net.OutputSerializer out) throws IOException {
		super.writeObject(out);

		int size = contents.size();
		out.write(size);

		for (TransferContent content : contents) {
			content.writeACK(out);
		}
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		int size = in.readInt();
		contents = new LinkedList<TransferContent>();

		for (int i = 0; i < size; i++) {
			TransferContent content = new TransferContent();
			content.readACK(in);
			contents.add(content);
		}

		if (type != MessageType.C2S_TRANSFER_ACK) {
			throw new IOException();
		}
	}
}
