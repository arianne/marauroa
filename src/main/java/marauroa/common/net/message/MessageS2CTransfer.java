/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import marauroa.common.net.Channel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/**
 * This message is used to transfer the client a list of contents that has already being
 * approved by it.
 *
 * @author miguel
 *
 */
public class MessageS2CTransfer extends Message {

	/** A list of the contents to transfer */
	private List<TransferContent> contents;

	/** Constructor for allowing creation of an empty message */
	public MessageS2CTransfer() {
		super(MessageType.S2C_TRANSFER, null);
	}

	/**
	 * Creates a new MessageS2CTransfer
	 *
	 * @param source  socket channel
	 */
	public MessageS2CTransfer(Channel source) {
		super(MessageType.S2C_TRANSFER, source);

		this.contents = new LinkedList<TransferContent>();
	}


	/**
	 * Creates a new MessageS2CTransfer
	 *
	 * @param source  socket channel
	 * @param content content to transfer
	 */
	public MessageS2CTransfer(Channel source, TransferContent content) {
		super(MessageType.S2C_TRANSFER, source);

		this.contents = new LinkedList<TransferContent>();
		contents.add(content);
	}

	/**
	 * adds content to the Message
	 *
	 * @param content TransferContent
	 */
	public void addContent(TransferContent content) {
		contents.add(content);
	}

	/**
	 * does this method contain no content?
	 *
	 * @return true, if there is no content; false otherwise
	 */
	public boolean isEmpty() {
		return contents.isEmpty();
	}
	
	/**
	 * The list if contents to transfer.
	 * @return The list if contents to transfer.
	 */
	public List<TransferContent> getContents() {
		return new LinkedList<TransferContent>(contents);
	}

	/**
	 * does this message require a perception
	 *
	 * @return true, if this message requires a perception, false otherwise
	 */
	@Override
	public boolean requiresPerception() {
		return true;
	}

	@Override
	public String toString() {
		StringBuilder st = new StringBuilder("Message (S2C Transfer) from (" + getAddress()
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

		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer serializer = new OutputSerializer(out_stream);
		serializer.setProtocolVersion(out.getProtocolVersion());

		int size = contents.size();
		serializer.write(size);

		for (TransferContent content : contents) {
			content.writeFULL(serializer);
		}

		out_stream.close();

		out.write(array.toByteArray());
	}

	@Override
	public void writeToJson(StringBuilder out) {
		super.writeToJson(out);
		out.append(",\"contents\":[");
		boolean first = true;
		for (TransferContent content : contents) {
			if (first) {
				first = false;
			} else {
				out.append(",");
			}
			content.writeFullToJson(out);
		}
		out.append("]");
	}

	@Override
	public void readObject(marauroa.common.net.InputSerializer in) throws IOException {
		super.readObject(in);

		ByteArrayInputStream array = new ByteArrayInputStream(in.readByteArray());
		java.util.zip.InflaterInputStream szlib = new java.util.zip.InflaterInputStream(array,
		        new java.util.zip.Inflater());
		InputSerializer serializer = new InputSerializer(szlib);
		serializer.setProtocolVersion(protocolVersion);

		int size = serializer.readInt();
		contents = new LinkedList<TransferContent>();

		for (int i = 0; i < size; i++) {
			TransferContent content = new TransferContent();
			content.readFULL(serializer);
			contents.add(content);
		}

		if (type != MessageType.S2C_TRANSFER) {
			throw new IOException();
		}
	}
}
