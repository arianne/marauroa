/* $Id: Message.java,v 1.9 2007/02/05 17:14:53 arianne_rpg Exp $ */
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * Message is a class to represent all the kind of messages that are possible to
 * exist in marauroa.
 */
public class Message implements Serializable {
	public final static byte CLIENTID_INVALID = -1;

	public enum MessageType {
		C2S_ACTION, 
		C2S_CHOOSECHARACTER, 
		C2S_LOGIN_REQUESTKEY, 
		C2S_LOGIN_SENDNONCENAMEANDPASSWORD, 
		C2S_LOGIN_SENDPROMISE, 
		C2S_LOGOUT, 
		C2S_OUTOFSYNC, 
		C2S_PERCEPTION_ACK, 
		C2S_TRANSFER_ACK, 
		S2C_ACTION_ACK, 
		S2C_CHARACTERLIST, 
		S2C_CHOOSECHARACTER_ACK, 
		S2C_CHOOSECHARACTER_NACK, 
		S2C_INVALIDMESSAGE, 
		S2C_LOGIN_ACK, 
		S2C_LOGIN_NACK, 
		S2C_LOGIN_SENDKEY, 
		S2C_LOGIN_SENDNONCE, 
		S2C_LOGOUT_ACK, 
		S2C_LOGOUT_NACK, 
		S2C_PERCEPTION, 
		S2C_SERVERINFO, 
		S2C_TRANSFER, 
		S2C_TRANSFER_REQ, 
		C2S_CREATEACCOUNT, 
		S2C_CREATEACCOUNT_ACK, 
		S2C_CREATEACCOUNT_NACK
	}

	protected MessageType type;

	protected int clientid;

	protected int timestampMessage;

	protected SocketChannel channel;
	
	/**
	 * Constructor with a TCP/IP source/destination of the message
	 * 
	 * @param source
	 *            The TCP/IP address associated to this message
	 */
	public Message(MessageType type, SocketChannel channel) {
		this.type = type;
		this.clientid = CLIENTID_INVALID;
		this.channel = channel;
		timestampMessage = (int) (System.currentTimeMillis());
	}

	/**
	 * Sets the TCP/IP source/destination of the message
	 * 
	 * @param source
	 *            The TCP/IP socket associated to this message
	 */
	public void setSocketChannel(SocketChannel channel) {
		this.channel = channel;
	}

	/**
	 * Returns the TCP/IP socket associatted with this message
	 * 
	 * @return the TCP/IP socket associatted with this message
	 */
	public SocketChannel getSocketChannel() {
		return channel;
	}
	
	/** Returns the address of the channel associated. */
	public InetSocketAddress getAddress() {
		if(channel==null) {
			return null;
		}
		
		//return channel.socket().getInetAddress();
		Socket socket=channel.socket();
		return new InetSocketAddress(socket.getInetAddress(), socket.getPort());
	}

	/**
	 * Returns the type of the message
	 * 
	 * @return the type of the message
	 */
	public MessageType getType() {
		return type;
	}

	/**
	 * Set the clientID so that we can identify the client to which the message
	 * is target, as only IP is easy to Fake
	 * 
	 * @param clientid
	 *            a int that reprents the client id.
	 */
	public void setClientID(int clientid) {
		this.clientid = clientid;
	}

	/**
	 * Returns the clientID of the Message.
	 * 
	 * @return the ClientID
	 */
	public int getClientID() {
		return clientid;
	}

	/** Returns the timestamp of the message. Usually milliseconds */
	public int getMessageTimestamp() {
		return timestampMessage;
	}

	/**
	 * Serialize the object into an ObjectOutput
	 * 
	 * @exception IOException
	 *                if the serializations fails
	 */
	public void writeObject(OutputSerializer out) throws IOException {
		out.write(NetConst.NETWORK_PROTOCOL_VERSION);
		out.write((byte) type.ordinal());
		out.write(clientid);
		out.write(timestampMessage);
	}

	/**
	 * Serialize the object from an ObjectInput
	 * 
	 * @exception IOException
	 *                if the serializations fails
	 * @exception java.lang.ClassNotFoundException
	 *                if the serialized class doesn't exist.
	 */
	public void readObject(InputSerializer in) throws IOException, java.lang.ClassNotFoundException {
		if (in.readByte() != NetConst.NETWORK_PROTOCOL_VERSION) {
			throw new IOException();
		}

		type = MessageType.values()[in.readByte()];
		clientid = in.readInt();
		timestampMessage = in.readInt();
	}
}
