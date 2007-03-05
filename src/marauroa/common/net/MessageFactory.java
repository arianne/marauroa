/* $Id: MessageFactory.java,v 1.21 2007/03/05 18:18:23 arianne_rpg Exp $ */
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
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.message.MessageC2SLoginSendPromise;
import marauroa.common.net.message.MessageC2SLogout;
import marauroa.common.net.message.MessageC2SOutOfSync;
import marauroa.common.net.message.MessageC2STransferACK;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.common.net.message.MessageS2CChooseCharacterACK;
import marauroa.common.net.message.MessageS2CChooseCharacterNACK;
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CCreateCharacterNACK;
import marauroa.common.net.message.MessageS2CInvalidMessage;
import marauroa.common.net.message.MessageS2CLoginACK;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendKey;
import marauroa.common.net.message.MessageS2CLoginSendNonce;
import marauroa.common.net.message.MessageS2CLogoutACK;
import marauroa.common.net.message.MessageS2CLogoutNACK;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.MessageS2CTransferREQ;

import org.apache.log4j.NDC;

/**
 * MessageFactory is the class that is in charge of building the messages from
 * the stream of bytes.
 */
public class MessageFactory {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(Attributes.class);

	private static Map<Integer, Class> factoryArray;

	private static MessageFactory messageFactory;

	private MessageFactory() {
		factoryArray = new HashMap<Integer, Class>();
		register();
	}

	/**
	 * This method returns an instance of MessageFactory
	 * 
	 * @return A shared instance of MessageFactory
	 */
	public static MessageFactory getFactory() {
		if (messageFactory == null) {
			messageFactory = new MessageFactory();
		}
		return messageFactory;
	}

	private void register() {
		register(Message.MessageType.C2S_ACTION, MessageC2SAction.class);
		register(Message.MessageType.C2S_CHOOSECHARACTER,MessageC2SChooseCharacter.class);
		register(Message.MessageType.C2S_LOGOUT, MessageC2SLogout.class);
		register(Message.MessageType.S2C_CHARACTERLIST,	MessageS2CCharacterList.class);
		register(Message.MessageType.S2C_CHOOSECHARACTER_ACK, MessageS2CChooseCharacterACK.class);
		register(Message.MessageType.S2C_CHOOSECHARACTER_NACK, MessageS2CChooseCharacterNACK.class);
		register(Message.MessageType.S2C_LOGIN_ACK, MessageS2CLoginACK.class);
		register(Message.MessageType.S2C_LOGIN_NACK, MessageS2CLoginNACK.class);
		register(Message.MessageType.S2C_LOGOUT_ACK, MessageS2CLogoutACK.class);
		register(Message.MessageType.S2C_LOGOUT_NACK, MessageS2CLogoutNACK.class);
		register(Message.MessageType.S2C_PERCEPTION, MessageS2CPerception.class);
		register(Message.MessageType.C2S_OUTOFSYNC, MessageC2SOutOfSync.class);
		register(Message.MessageType.S2C_SERVERINFO, MessageS2CServerInfo.class);
		register(Message.MessageType.S2C_INVALIDMESSAGE, MessageS2CInvalidMessage.class);
		register(Message.MessageType.S2C_TRANSFER_REQ, MessageS2CTransferREQ.class);
		register(Message.MessageType.C2S_TRANSFER_ACK, MessageC2STransferACK.class);
		register(Message.MessageType.S2C_TRANSFER, MessageS2CTransfer.class);
		register(Message.MessageType.C2S_LOGIN_REQUESTKEY, MessageC2SLoginRequestKey.class);
		register(Message.MessageType.C2S_LOGIN_SENDNONCENAMEANDPASSWORD, MessageC2SLoginSendNonceNameAndPassword.class);
		register(Message.MessageType.S2C_LOGIN_SENDKEY,	MessageS2CLoginSendKey.class);
		register(Message.MessageType.S2C_LOGIN_SENDNONCE, MessageS2CLoginSendNonce.class);
		register(Message.MessageType.C2S_LOGIN_SENDPROMISE, MessageC2SLoginSendPromise.class);
		register(Message.MessageType.C2S_CREATEACCOUNT, MessageC2SCreateAccount.class);
		register(Message.MessageType.S2C_CREATEACCOUNT_ACK, MessageS2CCreateAccountACK.class);
		register(Message.MessageType.S2C_CREATEACCOUNT_NACK, MessageS2CCreateAccountNACK.class);
		register(Message.MessageType.C2S_CREATECHARACTER, MessageC2SCreateCharacter.class);
		register(Message.MessageType.S2C_CREATECHARACTER_ACK, MessageS2CCreateCharacterACK.class);
		register(Message.MessageType.S2C_CREATECHARACTER_NACK, MessageS2CCreateCharacterNACK.class);
	}

	private void register(Message.MessageType index, Class messageClass) {
		factoryArray.put(new Integer(index.ordinal()), messageClass);
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
	public Message getMessage(byte[] data, SocketChannel channel)
			throws IOException, InvalidVersionException {
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
	public Message getMessage(byte[] data, SocketChannel channel, int offset) throws IOException, InvalidVersionException {
		if (data[offset] == NetConst.NETWORK_PROTOCOL_VERSION) {
			if (factoryArray.containsKey(new Integer(data[1]))) {
				Message tmp = null;
				try {
					Class messageType = factoryArray.get(new Integer(data[offset + 1]));
					tmp = (Message) messageType.newInstance();
					ByteArrayInputStream in = new ByteArrayInputStream(data);
					if (offset > 0) {
						in.skip(offset);
					}
					InputSerializer s = new InputSerializer(in);

					tmp.readObject(s);
					tmp.setSocketChannel(channel);
					return tmp;
				} catch (Exception e) {
					NDC.push("message is [" + tmp + "]\n");
					NDC.push("message dump is [\n"+ Utility.dumpByteArray(data) + "\n] (offset: "+ offset + ")\n");
					logger.error("error in getMessage", e);
					NDC.pop();
					NDC.pop();
					throw new IOException(e.getMessage());
				}
			} else {
				logger.warn("Message type [" + data[1]+ "] is not registered in the MessageFactory");
				throw new IOException("Message type [" + data[1]+ "] is not registered in the MessageFactory");
			}
		} else {
			logger.warn("Message has incorrect protocol version(" + data[0]+ ") expected (" + NetConst.NETWORK_PROTOCOL_VERSION+ ")");
			logger.debug("Message is: " + Utility.dumpByteArray(data));
			throw new InvalidVersionException(data[0]);
		}
	}
};
