/* $Id: Message.java,v 1.19 2010/06/29 21:30:48 nhnb Exp $ */
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import marauroa.common.net.InputSerializer;
import marauroa.common.net.NetConst;
import marauroa.common.net.OutputSerializer;
import marauroa.common.net.Serializable;

/**
 * Message is a class to represent all the kind of messages that are possible to
 * exist in marauroa.
 */
public class Message implements Serializable {

	/** Invalid client identificator constant */
	public final static int CLIENTID_INVALID = -1;

	/** Type of message */
	public enum MessageType {
		C2S_ACTION, 
		C2S_CHOOSECHARACTER, 
		C2S_LOGIN_REQUESTKEY, 
		C2S_LOGIN_SENDNONCENAMEANDPASSWORD, 
		C2S_LOGIN_SENDPROMISE, 
		C2S_LOGOUT, 
		C2S_OUTOFSYNC, 
		C2S_TRANSFER_ACK, 
		C2S_KEEPALIVE,
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
		S2C_CREATEACCOUNT_NACK, 
		C2S_CREATECHARACTER, 
		S2C_CREATECHARACTER_ACK, 
		S2C_CREATECHARACTER_NACK, 
		S2C_CONNECT_NACK,
		C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED, 
		S2C_LOGIN_MESSAGE_NACK 
	}

	/** Type of the message */
	protected MessageType type;

	/** Clientid of the player that generated the message */
	protected int clientid;

	/** Timestamp about when the message was created */
	protected int timestampMessage;
	/** version of this message */
	protected int protocolVersion = NetConst.NETWORK_PROTOCOL_VERSION;

	/**
	 * The socket channel that the message will use to be send or from where it
	 * was received
	 */
	protected SocketChannel channel;

	private InetAddress inetAddres;

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param type
	 *            the type of the message
	 * @param channel
	 *            The TCP/IP address associated to this message
	 */
	protected Message(MessageType type, SocketChannel channel) {
		this.type = type;
		this.clientid = CLIENTID_INVALID;
		this.channel = channel;
		if (channel != null) {
			Socket socket = channel.socket();
			inetAddres = socket.getInetAddress();
		}
		timestampMessage = (int) (System.currentTimeMillis());
	}

	/**
	 * Sets the TCP/IP source/destination of the message
	 *
	 * @param channel
	 *            The TCP/IP socket associated to this message
	 */
	public void setSocketChannel(SocketChannel channel) {
		this.channel = channel;
		if (channel != null) {
			Socket socket = channel.socket();
			inetAddres = socket.getInetAddress();
		} else {
			inetAddres = null;
		}
	}

	/**
	 * Returns the TCP/IP socket associatted with this message
	 *
	 * @return the TCP/IP socket associatted with this message
	 */
	public SocketChannel getSocketChannel() {
		return channel;
	}

	/**
	 * Returns the address of the channel associated.
	 *
	 * @return the address of the channel associated.
	 */
	public InetAddress getAddress() {
		return inetAddres;
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

	/**
	 * Returns the timestamp of the message. Usually milliseconds
	 *
	 * @return the timestamp of the message. Usually milliseconds
	 */
	public int getMessageTimestamp() {
		return timestampMessage;
	}

	/**
	 * gets the protocol version
	 *
	 * @return protocol version
	 */
	public int getProtocolVersion() {
    	return protocolVersion;
    }

	/**
	 * sets the protocol version, limited to the max supported version
	 *
	 * @param protocolVersion protocol versoin
	 */
	public void setProtocolVersion(int protocolVersion) {
    	this.protocolVersion = Math.min(NetConst.NETWORK_PROTOCOL_VERSION, protocolVersion);
    }


	/**
	 * Serialize the object into an ObjectOutput
	 *
	 * @param out
	 *            the output serializer.
	 * @exception IOException
	 *                if the serializations fails
	 */
	public void writeObject(OutputSerializer out) throws IOException {
		out.write((byte)protocolVersion);
		out.write((byte) type.ordinal());
		out.write(clientid);
		out.write(timestampMessage);
	}

	/**
	 * Serialize the object from an ObjectInput
	 *
	 * @param in
	 *            the input serializer
	 * @exception IOException
	 *                if the serializations fails
	 */
	public void readObject(InputSerializer in) throws IOException {
		int protocolVersion = in.readByte();
		if (protocolVersion < NetConst.NETWORK_PROTOCOL_VERSION_MIN
				|| protocolVersion > NetConst.NETWORK_PROTOCOL_VERSION_MAX) {
			throw new IOException("Unsupported protocol version.");
		}

		type = MessageType.values()[in.readByte()];
		clientid = in.readInt();
		timestampMessage = in.readInt();
	}

	/**
	 * generates a list of attributes suitable to be used in toString()
	 *
	 * @param sb StringBuilder to append the attribute string to
	 */
	protected void internalToString(StringBuilder sb) {
		sb.append(", channel=");
		sb.append(channel);
		sb.append(", clientid=");
		sb.append(clientid);
		sb.append(", timestampMessage=");
		sb.append(timestampMessage);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName());
		sb.append("[");
		sb.append("type=");
		sb.append(type);
		internalToString(sb);
		sb.append("]");
		return sb.toString();
	}

	
}
