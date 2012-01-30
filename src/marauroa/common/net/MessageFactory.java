/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
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
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.Utility;
import marauroa.common.game.Attributes;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;
import marauroa.common.net.message.MessageC2SChooseCharacter;
import marauroa.common.net.message.MessageC2SCreateAccount;
import marauroa.common.net.message.MessageC2SCreateCharacter;
import marauroa.common.net.message.MessageC2SKeepAlive;
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.message.MessageC2SLoginSendNonceNamePasswordAndSeed;
import marauroa.common.net.message.MessageC2SLoginSendPromise;
import marauroa.common.net.message.MessageC2SLogout;
import marauroa.common.net.message.MessageC2SOutOfSync;
import marauroa.common.net.message.MessageC2STransferACK;
import marauroa.common.net.message.MessageP2SCreateAccount;
import marauroa.common.net.message.MessageP2SCreateCharacter;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.common.net.message.MessageS2CChooseCharacterACK;
import marauroa.common.net.message.MessageS2CChooseCharacterNACK;
import marauroa.common.net.message.MessageS2CConnectNACK;
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CCreateCharacterNACK;
import marauroa.common.net.message.MessageS2CInvalidMessage;
import marauroa.common.net.message.MessageS2CLoginACK;
import marauroa.common.net.message.MessageS2CLoginMessageNACK;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendKey;
import marauroa.common.net.message.MessageS2CLoginSendNonce;
import marauroa.common.net.message.MessageS2CLogoutACK;
import marauroa.common.net.message.MessageS2CLogoutNACK;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.MessageS2CTransferREQ;

/**
 * MessageFactory is the class that is in charge of building the messages from
 * the stream of bytes.
 *
 * MessageFactory follows the singleton pattern.
 *
 */
public class MessageFactory {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(Attributes.class);

	/** A factory to create messages instance from an integer code. */
	private static Map<Integer, Class<?>> factoryArray;

	/**
	 * Singleton instance
	 */
	private static MessageFactory messageFactory;

	/**
	 * Constructor
	 *
	 */
	private MessageFactory() {
		register();
	}

	/**
	 * This method returns an instance of MessageFactory
	 *
	 * @return A shared instance of MessageFactory
	 */
	public static MessageFactory getFactory() {
		if (messageFactory == null) {
			factoryArray = new HashMap<Integer, Class<?>>();
			messageFactory = new MessageFactory();
		}
		return messageFactory;
	}

	/**
	 * Register messages with its code.
	 *
	 */
	private void register() {
		register(Message.MessageType.C2S_ACTION, MessageC2SAction.class);
		register(Message.MessageType.C2S_CHOOSECHARACTER, MessageC2SChooseCharacter.class);
		register(Message.MessageType.C2S_LOGOUT, MessageC2SLogout.class);
		register(Message.MessageType.S2C_CHARACTERLIST, MessageS2CCharacterList.class);
		register(Message.MessageType.S2C_CHOOSECHARACTER_ACK, MessageS2CChooseCharacterACK.class);
		register(Message.MessageType.S2C_CHOOSECHARACTER_NACK, MessageS2CChooseCharacterNACK.class);
		register(Message.MessageType.S2C_LOGIN_ACK, MessageS2CLoginACK.class);
		register(Message.MessageType.S2C_LOGIN_NACK, MessageS2CLoginNACK.class);
		register(Message.MessageType.S2C_LOGOUT_ACK, MessageS2CLogoutACK.class);
		register(Message.MessageType.S2C_LOGOUT_NACK, MessageS2CLogoutNACK.class);
		register(Message.MessageType.S2C_PERCEPTION, MessageS2CPerception.class);
		register(Message.MessageType.C2S_OUTOFSYNC, MessageC2SOutOfSync.class);
		register(Message.MessageType.C2S_KEEPALIVE, MessageC2SKeepAlive.class);
		register(Message.MessageType.S2C_SERVERINFO, MessageS2CServerInfo.class);
		register(Message.MessageType.S2C_INVALIDMESSAGE, MessageS2CInvalidMessage.class);
		register(Message.MessageType.S2C_TRANSFER_REQ, MessageS2CTransferREQ.class);
		register(Message.MessageType.C2S_TRANSFER_ACK, MessageC2STransferACK.class);
		register(Message.MessageType.S2C_TRANSFER, MessageS2CTransfer.class);
		register(Message.MessageType.C2S_LOGIN_REQUESTKEY, MessageC2SLoginRequestKey.class);
		register(Message.MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD,
		        MessageC2SLoginSendNonceNameAndPassword.class);
		register(Message.MessageType.S2C_LOGIN_SENDKEY, MessageS2CLoginSendKey.class);
		register(Message.MessageType.S2C_LOGIN_SENDNONCE, MessageS2CLoginSendNonce.class);
		register(Message.MessageType.C2S_LOGIN_SENDPROMISE, MessageC2SLoginSendPromise.class);
		register(Message.MessageType.C2S_CREATEACCOUNT, MessageC2SCreateAccount.class);
		register(Message.MessageType.S2C_CREATEACCOUNT_ACK, MessageS2CCreateAccountACK.class);
		register(Message.MessageType.S2C_CREATEACCOUNT_NACK, MessageS2CCreateAccountNACK.class);
		register(Message.MessageType.C2S_CREATECHARACTER, MessageC2SCreateCharacter.class);
		register(Message.MessageType.S2C_CREATECHARACTER_ACK, MessageS2CCreateCharacterACK.class);
		register(Message.MessageType.S2C_CREATECHARACTER_NACK, MessageS2CCreateCharacterNACK.class);
		register(Message.MessageType.S2C_CONNECT_NACK, MessageS2CConnectNACK.class);
		register(Message.MessageType.C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED,
				MessageC2SLoginSendNonceNamePasswordAndSeed.class);
		register(Message.MessageType.S2C_LOGIN_MESSAGE_NACK, MessageS2CLoginMessageNACK.class);
		register(Message.MessageType.P2S_CREATECHARACTER, MessageP2SCreateCharacter.class);
		register(Message.MessageType.P2S_CREATEACCOUNT, MessageP2SCreateAccount.class);
	}

	/**
	 * Adds a new message class to the factory and link it with a code.
	 * @param index the code to use.
	 * @param messageClass the class of the message to add.
	 */
	private void register(Message.MessageType index, Class<?> messageClass) {
		factoryArray.put(index.ordinal(), messageClass);
	}

	/**
	 * Returns a object of the right class from a stream of serialized data.
	 *
	 * @param data
	 *            the serialized data
	 * @param channel
	 *            the source of the message needed to build the object.
	 * @return a message of the right class
	 * @throws IOException
	 *             in case of problems with the message
	 * @throws InvalidVersionException
	 *             if the message version doesn't match
	 */
	public Message getMessage(byte[] data, SocketChannel channel) throws IOException,
	        InvalidVersionException {
		return getMessage(data, channel, 0);
	}

	/**
	 * Returns a object of the right class from a stream of serialized data.
	 *
	 * @param data
	 *            the serialized data
	 * @param channel
	 *            the source of the message needed to build the object.
	 * @param offset
	 *            where to start reading in the data-array.
	 * @return a message of the right class
	 * @throws IOException
	 *             in case of problems with the message
	 * @throws InvalidVersionException
	 *             if the message version doesn't match
	 */
	public Message getMessage(byte[] data, SocketChannel channel, int offset) throws IOException,
	        InvalidVersionException {
		/*
		 * Check the version of the network protocol.
		 */
		int networkProtocolVersion = data[offset];
		if (networkProtocolVersion < NetConst.NETWORK_PROTOCOL_VERSION_MIN
				|| networkProtocolVersion > NetConst.NETWORK_PROTOCOL_VERSION_MAX) {
			logger.error("Message has incorrect protocol version (" + networkProtocolVersion + ") expected (" + NetConst.NETWORK_PROTOCOL_VERSION_MIN + " < x < " + NetConst.NETWORK_PROTOCOL_VERSION_MAX + ")");
			logger.error("Message is: " + Utility.dumpByteArray(data));
			throw new InvalidVersionException(data[offset]);
		}


		int messageTypeIndex = data[offset + 1];
		/*
		 * Now we check if we have this message class implemented.
		 */
		if (factoryArray.containsKey(messageTypeIndex)) {
			Message tmp = null;
			try {
				Class<?> messageType = factoryArray.get(messageTypeIndex);
				tmp = (Message) messageType.newInstance();
				tmp.setProtocolVersion(networkProtocolVersion);
				ByteArrayInputStream in = new ByteArrayInputStream(data);
				if (offset > 0) {
					in.skip(offset);
				}
				InputSerializer s = new InputSerializer(in);
				s.setProtocolVersion(networkProtocolVersion);

				tmp.readObject(s);
				tmp.setSocketChannel(channel);
				s.close();
				return tmp;
			} catch (Exception e) {
				logger.error("error in getMessage", e);
				throw new IOException(e.getMessage());
			}
		} else {
			logger.warn("Message type [" + messageTypeIndex + "] is not registered in the MessageFactory");
			throw new IOException("Message type [" + messageTypeIndex + "] is not registered in the MessageFactory");
		}
	}
}
