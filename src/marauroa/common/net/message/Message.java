/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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
import java.util.Map;

import marauroa.common.net.Channel;
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
		/** client to server: player action */
		C2S_ACTION,
		/** client to server: character pick */
		C2S_CHOOSECHARACTER,
		/** client to server: request key to start login */
		C2S_LOGIN_REQUESTKEY,
		/** client to server: send nonce, username and encrypted password */
		C2S_LOGIN_SENDNONCENAMEANDPASSWORD,
		/** client to server: sends a promise to the server which is used to setup encryption of the passwrd */
		C2S_LOGIN_SENDPROMISE,
		/** client to server: requests a logout */
		C2S_LOGOUT,
		/** client to server: requests a full permission */
		C2S_OUTOFSYNC,
		/** client to server: acknowledge transfer of binary data (e. g. map information) */
		C2S_TRANSFER_ACK,
		/** client to server: tell the server, that we are still alive */
		C2S_KEEPALIVE,
		/** server to client: sends the character list */
		S2C_CHARACTERLIST,
		/** server to client: confirms a successful character choice */
		S2C_CHOOSECHARACTER_ACK,
		/** server to client: rejects a character choice */
		S2C_CHOOSECHARACTER_NACK,
		/** server to client: rejects the last message */
		S2C_INVALIDMESSAGE,
		/** server to client: confirms a successful login */
		S2C_LOGIN_ACK,
		/** server to client: rejects a login attempt */
		S2C_LOGIN_NACK,
		/** server to client: sends the public key */
		S2C_LOGIN_SENDKEY,
		/** server to client: sends a nonce as part of the encryption handshake */
		S2C_LOGIN_SENDNONCE,
		/** server to client: confirms a logout attempt */
		S2C_LOGOUT_ACK,
		/** server to client: rejects a logout attempt */
		S2C_LOGOUT_NACK,
		/** server to client: updates the client view of the world around it */
		S2C_PERCEPTION,
		/** server to client: submits server and RPClass information to the client */
		S2C_SERVERINFO,
		/** server to client: transfers content to the client */
		S2C_TRANSFER,
		/** server to client: offers content to the client */
		S2C_TRANSFER_REQ,
		/** client to server: requests an account creation */
		C2S_CREATEACCOUNT,
		/** server to client: confirms a successful character creation attempt */
		S2C_CREATEACCOUNT_ACK,
		/** server to client: rejects an account cration attempt */
		S2C_CREATEACCOUNT_NACK,
		/** client to server: requests a character creation */
		C2S_CREATECHARACTER,
		/** server to client: confirms a successful character creation attempt */
		S2C_CREATECHARACTER_ACK,
		/** server to client: rejects a character creation attempt */
		S2C_CREATECHARACTER_NACK,
		/** server to client: rejects a connection attempt */
		S2C_CONNECT_NACK,
		/** client to server: sends username, password and a seed (for single sign on) */
		C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED,
		/** server to client: reject a login attempt */
		S2C_LOGIN_MESSAGE_NACK,
		/** proxy to server: creates a character on behalf of a user */
		P2S_CREATECHARACTER,
		/** proxy to server: creates an account on behalf of a user */
		P2S_CREATEACCOUNT,
		/** client to server: sends the username and password */
		C2S_LOGIN_SENDUSERNAMEANDPASSWORD
	}

	/** Type of the message */
	protected MessageType type;

	/** Clientid of the player that generated the message */
	protected int clientid = -1;

	/** Timestamp about when the message was created */
	protected int timestampMessage;
	/** version of this message */
	protected int protocolVersion = NetConst.NETWORK_PROTOCOL_VERSION;

	/**
	 * The socket channel that the message will use to be send or from where it
	 * was received
	 */
	protected Channel channel;

	private InetAddress inetAddres;

	/**
	 * Constructor with a TCP/IP source/destination of the message
	 *
	 * @param type
	 *            the type of the message
	 * @param channel
	 *            The TCP/IP address associated to this message
	 */
	protected Message(MessageType type, Channel channel) {
		this.type = type;
		this.clientid = CLIENTID_INVALID;
		this.channel = channel;
		if (channel != null) {
			inetAddres = channel.getInetAddress();
		}
		timestampMessage = (int) (System.currentTimeMillis());
	}

	/**
	 * Sets the TCP/IP source/destination of the message
	 *
	 * @param channel
	 *            The TCP/IP socket associated to this message
	 */
	public void setChannel(Channel channel) {
		this.channel = channel;
		if (channel != null) {
			inetAddres = channel.getInetAddress();
		} else {
			inetAddres = null;
		}
	}

	/**
	 * Returns the TCP/IP socket associatted with this message
	 *
	 * @return the TCP/IP socket associatted with this message
	 */
	public Channel getChannel() {
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
	 * may this method be skipped?
	 *
	 * @return true, if this method may be skipped; false otherwise.
	 */
	public boolean isSkippable() {
		return false;
	}

	/**
	 * is this message a perception
	 *
	 * @return true, if this message is a perception; false otherwise
	 */
	public boolean isPerception() {
		return false;
	}

	/**
	 * does this message require a perception
	 *
	 * @return true, if this message requires a perception, false otherwise
	 */
	public boolean requiresPerception() {
		return false;
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
		out.write((byte) Math.min(protocolVersion, NetConst.NETWORK_PROTOCOL_VERSION));
		out.write((byte) type.ordinal());
		out.write(clientid);
		out.write(timestampMessage);
	}

	/**
	 * Serialize the object to json
	 *
	 * @param out output buffer
	 */
	public void writeToJson(StringBuilder out) {
		out.append("\"t\": \"");
		out.append(type.ordinal());
		out.append("\",\"c\": \"");
		out.append(clientid);
		out.append("\",\"s\": \"");
		out.append(timestampMessage);
		out.append("\"");
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
		protocolVersion = in.readByte();
		if (protocolVersion < NetConst.NETWORK_PROTOCOL_VERSION_MIN
				|| protocolVersion > NetConst.NETWORK_PROTOCOL_VERSION_MAX) {
			throw new IOException("Unsupported protocol version.");
		}

		type = MessageType.values()[in.readByte()];
		clientid = in.readInt();
		timestampMessage = in.readInt();
	}

	/**
	 * reads a message from a map
	 *
	 * @param in Map to read from
	 * @exception IOException
	 *                if the serializations fails
	 */
	public void readFromMap(Map<String, Object> in) throws IOException {
		this.type = MessageType.values()[Byte.parseByte((String) in.get("t"))];
		this.clientid = Integer.parseInt((String) in.get("c"));
		this.timestampMessage = Integer.parseInt((String) in.get("s"));
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
