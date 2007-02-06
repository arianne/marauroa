/* $Id: ariannexp.java,v 1.35 2007/02/06 18:25:00 arianne_rpg Exp $ */
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
package marauroa.client;

import java.net.SocketException;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;

import marauroa.client.net.INetworkClientManagerInterface;
import marauroa.client.net.TCPThreadedNetworkClientManager;
import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAPublicKey;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;
import marauroa.common.net.message.MessageC2SChooseCharacter;
import marauroa.common.net.message.MessageC2SCreateAccount;
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.message.MessageC2SLoginSendPromise;
import marauroa.common.net.message.MessageC2SLogout;
import marauroa.common.net.message.MessageC2SOutOfSync;
import marauroa.common.net.message.MessageC2STransferACK;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendKey;
import marauroa.common.net.message.MessageS2CLoginSendNonce;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.MessageS2CTransferREQ;
import marauroa.common.net.message.TransferContent;

import org.apache.log4j.Logger;

public abstract class ariannexp {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(ariannexp.class);

	public final static long TIMEOUT = 10000;

	private INetworkClientManagerInterface netMan;

	private List<Message> messages;

	/**
	 * Constructor.
	 * 
	 * @param logging
	 *            ariannexp will write to a file if this is true.
	 */
	public ariannexp(String loggingProperties) {
		Log4J.init(loggingProperties);

		messages = new LinkedList<Message>();
	}

	/**
	 * Call this method to connect to server. This method just configure the
	 * connection, it doesn't send anything
	 * 
	 * @param host
	 *            server host name
	 * @param port
	 *            server port number
	 * @throws SocketException
	 *             if connection is not possible
	 */
	public void connect(String host, int port)
			throws SocketException {
		netMan = new TCPThreadedNetworkClientManager(host, port);
	}

	private Message getMessage() throws InvalidVersionException,
			ariannexpTimeoutException {
		Message msg = null;
		long delta = System.currentTimeMillis();

		while (msg == null) {
			msg = netMan.getMessage(TimeoutConf.SOCKET_TIMEOUT);

			if (msg == null && System.currentTimeMillis() - delta > TIMEOUT) {
				throw new ariannexpTimeoutException();
			}
		}

		return msg;
	}

	public void resync() {
		MessageC2SOutOfSync msg = new MessageC2SOutOfSync();
		netMan.addMessage(msg);
	}

	/**
	 * Login to server using the given username and password.
	 * 
	 * @param username
	 *            Player username
	 * @param password
	 *            Player password
	 * @return true if login is successful.
	 */
	public synchronized boolean login(String username, String password)
			throws ariannexpTimeoutException {
		try {
			int received = 0;
			RSAPublicKey key = null;
			byte[] clientNonce = null;
			byte[] serverNonce = null;

			netMan.addMessage(new MessageC2SLoginRequestKey(null,
					getGameName(), getVersionNumber()));

			while (received < 3) {
				Message msg;
				if (messages.size() > 0) {
					msg = messages.remove(0);
				} else {
					msg = getMessage();
				}

				switch (msg.getType()) {
				case S2C_LOGIN_SENDKEY: {
					logger.debug("Recieved Key");
					key = ((MessageS2CLoginSendKey) msg).getKey();

					clientNonce = Hash.random(Hash.hashLength());
					netMan.addMessage(new MessageC2SLoginSendPromise(null, Hash.hash(clientNonce)));
					break;
				}
				case S2C_LOGIN_SENDNONCE: {
					logger.debug("Recieved Server Nonce");
					if (serverNonce != null) {
						return false;
					}

					serverNonce = ((MessageS2CLoginSendNonce) msg).getHash();
					byte[] b1 = Hash.xor(clientNonce, serverNonce);
					if (b1 == null) {
						return false;
					}

					byte[] b2 = Hash.xor(b1, Hash.hash(password));
					if (b2 == null) {
						return false;
					}

					byte[] cryptedPassword = key.encodeByteArray(b2);
					netMan.addMessage(new MessageC2SLoginSendNonceNameAndPassword(
									null, clientNonce, username,
									cryptedPassword));
					break;
				}
				case S2C_LOGIN_ACK:
					logger.debug("Login correct");
					received++;
					break;
				case S2C_CHARACTERLIST:
					logger.debug("Recieved Character list");
					String[] characters = ((MessageS2CCharacterList) msg)
							.getCharacters();
					onAvailableCharacters(characters);
					received++;
					break;
				case S2C_SERVERINFO:
					logger.debug("Recieved Server info");
					String[] info = ((MessageS2CServerInfo) msg).getContents();
					onServerInfo(info);
					received++;
					break;
				case S2C_LOGIN_NACK:
					MessageS2CLoginNACK msgNACK = (MessageS2CLoginNACK) msg;
					logger.debug("Login failed. Reason: "
							+ msgNACK.getResolution());
					event = msgNACK.getResolution();
					return false;
				default:
					messages.add(msg);
				}
			}
			return true;
		} catch (InvalidVersionException e) {
			event = "Invalid client version to connect to this server.";
			onError(1, "Invalid client version to connect to this server.");
			return false;
		}
	}

	private String event;

	public String getEvent() {
		return event;
	}

	/**
	 * After login allows you to choose a character to play
	 * 
	 * @param character
	 *            name of the character we want to play with.
	 * @return true if choosing character is successful.
	 */
	public synchronized boolean chooseCharacter(String character)
			throws ariannexpTimeoutException {
		try {
			Message msgCC = new MessageC2SChooseCharacter(null, character);

			netMan.addMessage(msgCC);

			int recieved = 0;

			while (recieved != 1) {
				Message msg = getMessage();
				switch (msg.getType()) {
				case S2C_CHOOSECHARACTER_ACK:
					logger.debug("Choose Character ACK");
					return true;
				case S2C_CHOOSECHARACTER_NACK:
					logger.debug("Choose Character NACK");
					return false;
				default:
					messages.add(msg);
				}
			}

			return false;
		} catch (InvalidVersionException e) {
			logger
					.error("Invalid client version to connect to this server.",
							e);
			onError(1, "Invalid client version to connect to this server.");
			return false;
		}
	}

	public synchronized boolean createAccount(String username, String password,
			String email, RPObject template) throws ariannexpTimeoutException {
		try {
			Message msgCA = new MessageC2SCreateAccount(null, username, password, email, template);

			netMan.addMessage(msgCA);

			int recieved = 0;

			while (recieved != 1) {
				Message msg = getMessage();
				switch (msg.getType()) {
				case S2C_CREATEACCOUNT_ACK:
					logger.debug("Create account ACK");
					return true;
				case S2C_CREATEACCOUNT_NACK:
					logger.debug("Create account NACK");
					event = ((MessageS2CCreateAccountNACK) msg).getResolution();
					return false;
				default:
					messages.add(msg);
				}
			}

			return false;
		} catch (InvalidVersionException e) {
			logger
					.error("Invalid client version to connect to this server.",
							e);
			onError(1, "Invalid client version to connect to this server.");
			return false;
		}
	}

	/** Sends a RPAction to server */
	public void send(RPAction action) throws ariannexpTimeoutException {
		MessageC2SAction msgAction = new MessageC2SAction(null, action);
		netMan.addMessage(msgAction);
	}

	/**
	 * Request logout of server
	 * 
	 * @return true if we have successfully logout.
	 */
	public synchronized boolean logout() {
		try {
			Message msgL = new MessageC2SLogout(null);

			netMan.addMessage(msgL);
			int recieved = 0;

			while (recieved != 1) {
				Message msg = getMessage();
				switch (msg.getType()) {
				case S2C_LOGOUT_ACK:
					logger.debug("Logout ACK");
					return true;
				case S2C_LOGOUT_NACK:
					logger.debug("Logout NACK");
					return false;
				default:
					messages.add(msg);
				}
			}

			return false;
		} catch (InvalidVersionException e) {
			logger
					.error("Invalid client version to connect to this server.",
							e);
			onError(1, "Invalid client version to connect to this server.");
			return false;
		} catch (ariannexpTimeoutException e) {
			onError(1, "ariannexp can't connect to server. Server down?");
			return false;
		}
	}

	/** Call this method to get and apply messages */
	public synchronized boolean loop(int delta) {
		boolean recievedMessages = false;

		try {
			Message newmsg = netMan.getMessage(30);
			if (newmsg != null) {
				messages.add(newmsg);
			}

			logger
					.debug("getMessage returned " + messages.size()
							+ " messages");

			for (Message msg : messages) {
				recievedMessages = true;

				switch (msg.getType()) {
				case S2C_PERCEPTION: {
					logger.debug("Processing Message Perception");
					MessageS2CPerception msgPer = (MessageS2CPerception) msg;
					onPerception(msgPer);

					break;
				}

				case S2C_TRANSFER_REQ: {
					logger.debug("Processing Content Transfer Request");
					List<TransferContent> items = ((MessageS2CTransferREQ) msg)
							.getContents();

					items = onTransferREQ(items);

					MessageC2STransferACK reply = new MessageC2STransferACK(null, items);
					netMan.addMessage(reply);

					break;
				}

				case S2C_TRANSFER: {
					logger.debug("Processing Content Transfer");
					List<TransferContent> items = ((MessageS2CTransfer) msg)
							.getContents();
					onTransfer(items);

					break;
				}
				}
			}

			messages.clear();
		} catch (ConcurrentModificationException e) {
			logger.warn(e);
		}

		return recievedMessages;
	}

	/**
	 * Are we connected to the server?
	 *
	 * @return true unless it is sure that we are disconnected
	 */
	public boolean getConnectionState() {
		return netMan.getConnectionState();
	}

	/**
	 * It is called when a perception arrives so you can choose how to apply the
	 * perception
	 */
	abstract protected void onPerception(MessageS2CPerception message);

	/**
	 * It is called on a transfer request so you can choose what items to
	 * approve or reject
	 */
	abstract protected List<TransferContent> onTransferREQ(
			List<TransferContent> items);

	/** It is called when we get a transfer of content */
	abstract protected void onTransfer(List<TransferContent> items);

	/** It is called when we get the list of characters */
	abstract protected void onAvailableCharacters(String[] characters);

	/** It is called when we get the list of server information strings */
	abstract protected void onServerInfo(String[] info);

	/**
	 * It is called on error conditions so you can improve the handling of the
	 * error
	 */
	abstract protected void onError(int code, String reason);

	abstract protected String getGameName();

	abstract protected String getVersionNumber();
}
