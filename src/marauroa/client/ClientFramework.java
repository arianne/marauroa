/* $Id: ClientFramework.java,v 1.22 2007/03/12 19:31:16 arianne_rpg Exp $ */
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import marauroa.client.net.INetworkClientManagerInterface;
import marauroa.client.net.TCPNetworkClientManager;
import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAPublicKey;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;
import marauroa.common.net.InvalidVersionException;
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
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendKey;
import marauroa.common.net.message.MessageS2CLoginSendNonce;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.MessageS2CTransferREQ;
import marauroa.common.net.message.TransferContent;

/**
 * It is a wrapper over all the things that the client should do.
 * You should extend this class at your game.
 * @author miguel
 *
 */
public abstract class ClientFramework {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(ClientFramework.class);

	/** How long we should wait for connect. */
	public final static int TIMEOUT = 10000;

	/** We keep an instance of network manager to be able to comunicate with server. */
	protected INetworkClientManagerInterface netMan;

	/** We keep a list of all messages waiting for being processed. */
	private List<Message> messages;

	/**
	 * Constructor.
	 *
	 * @param loggingProperties contains the name of the file that configure the logging system.
	 */
	public ClientFramework(String loggingProperties) {
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
	 * @throws IOException
	 *             if connection is not possible
	 */
	public void connect(String host, int port) throws IOException {
		netMan = new TCPNetworkClientManager(host, port);
	}

	/**
	 * Retrieves a message from network manager.
	 * @return a message
	 * @throws InvalidVersionException
	 * @throws TimeoutException if there is no message available in TIMEOUT miliseconds.
	 */
	private Message getMessage() throws InvalidVersionException, TimeoutException {
		Message msg=null;

		if (!messages.isEmpty()) {
			msg=messages.remove(0);
		} else {
			msg=netMan.getMessage(TIMEOUT);

			if(msg==null) {
				throw new TimeoutException();
			}
		}

		logger.debug("CF getMessage: "+ msg);
		return msg;
	}

	/**
	 * Request a synchronization with server.
	 * It shouldn't be needed now that we are using TCP.
	 */
	@Deprecated
	public void resync() {
		MessageC2SOutOfSync msg = new MessageC2SOutOfSync();
		netMan.addMessage(msg);
	}

	/**
	 * Login to server using the given username and password.
	 *
	 * @param username Player username
	 * @param password Player password
	 * @throws InvalidVersionException if we are not using a compatible version
	 * @throws TimeoutException  if timeout happens while waiting for the message.
	 * @throws LoginFailedException if login is rejected
	 */
	public synchronized void login(String username, String password) throws InvalidVersionException, TimeoutException, LoginFailedException {
		int received = 0;
		RSAPublicKey key = null;
		byte[] clientNonce = null;
		byte[] serverNonce = null;

		/* Send to server a login request and indicate the game name and version */
		netMan.addMessage(new MessageC2SLoginRequestKey(null, getGameName(), getVersionNumber()));

		while (received < 3) {
			Message msg = getMessage();

			switch (msg.getType()) {
			/* Server sends its public RSA key */
			case S2C_LOGIN_SENDKEY: {
				logger.debug("Recieved Key");
				key = ((MessageS2CLoginSendKey) msg).getKey();

				clientNonce = Hash.random(Hash.hashLength());
				netMan.addMessage(new MessageC2SLoginSendPromise(null, Hash.hash(clientNonce)));
				break;
			}
			/* Server sends a random big integer */
			case S2C_LOGIN_SENDNONCE: {
				logger.debug("Recieved Server Nonce");
				if (serverNonce != null) {
					throw new LoginFailedException("Already recieved a serverNonce");
				}

				serverNonce = ((MessageS2CLoginSendNonce) msg).getHash();
				byte[] b1 = Hash.xor(clientNonce, serverNonce);
				if (b1 == null) {
					throw new LoginFailedException("Incorrect hash b1");
				}

				byte[] b2 = Hash.xor(b1, Hash.hash(password));
				if (b2 == null) {
					throw new LoginFailedException("Incorrect hash b2");
				}

				byte[] cryptedPassword = key.encodeByteArray(b2);
				netMan.addMessage(new MessageC2SLoginSendNonceNameAndPassword(null, clientNonce, username, cryptedPassword));
				break;
			}
			/* Server replied with ACK to login operation */
			case S2C_LOGIN_ACK:
				logger.debug("Login correct");
				received++;
				break;
			/* Server send the character list */
			case S2C_CHARACTERLIST:
				logger.debug("Recieved Character list");
				String[] characters = ((MessageS2CCharacterList) msg).getCharacters();

				/* We notify client of characters by calling the callback method. */
				onAvailableCharacters(characters);
				received++;
				break;
			/* Server sends the server info message with information about versions, homepage, etc... */
			case S2C_SERVERINFO:
				logger.debug("Recieved Server info");
				String[] info = ((MessageS2CServerInfo) msg).getContents();

				/* We notify client of this info by calling the callback method. */
				onServerInfo(info);
				received++;
				break;
			/* Login failed, explain reason on event */
			case S2C_LOGIN_NACK:
				MessageS2CLoginNACK msgNACK = (MessageS2CLoginNACK) msg;
				logger.debug("Login failed. Reason: "+ msgNACK.getResolution());

				throw new LoginFailedException(msgNACK.getResolution());
			/* If message doesn't match, store it, someone will need it. */
			default:
				messages.add(msg);
			}
		}
	}

	/**
	 * After login allows you to choose a character to play
	 *
	 * @param character
	 *            name of the character we want to play with.
	 * @return true if choosing character is successful.
	 * @throws InvalidVersionException if we are not using a compatible version
	 * @throws TimeoutException  if timeout happens while waiting for the message.
	 */
	public synchronized boolean chooseCharacter(String character) throws TimeoutException, InvalidVersionException {
		Message msgCC = new MessageC2SChooseCharacter(null, character);
		netMan.addMessage(msgCC);

		int recieved = 0;

		while (recieved != 1) {
			Message msg = getMessage();

			switch (msg.getType()) {
			/* Server accepted the character we choosed */
			case S2C_CHOOSECHARACTER_ACK:
				logger.debug("Choose Character ACK");
				return true;
			/* Server rejected the character we choosed. No reason */
			case S2C_CHOOSECHARACTER_NACK:
				logger.debug("Choose Character NACK");
				return false;
			default:
				messages.add(msg);
			}
		}

		// Unreachable, but makes javac happy
		return false;
	}

	/**
	 * Request server to create an account on server.
	 *
	 * @param username the player desired username
	 * @param password the player password
	 * @param email player's email for notifications and/or password reset.
	 *
	 * @throws InvalidVersionException if we are not using a compatible version
	 * @throws TimeoutException  if timeout happens while waiting for the message.
	 * @throws CreateAccountFailedException
	 */
	public synchronized AccountResult createAccount(String username, String password, String email) throws TimeoutException, InvalidVersionException, CreateAccountFailedException {
		Message msgCA = new MessageC2SCreateAccount(null, username, password, email);

		netMan.addMessage(msgCA);

		int recieved = 0;

		AccountResult result=null;

		while (recieved != 1) {
			Message msg = getMessage();

			switch (msg.getType()) {
			/* Account was created */
			case S2C_CREATEACCOUNT_ACK:
				logger.debug("Create account ACK");

				MessageS2CCreateAccountACK msgack=(MessageS2CCreateAccountACK)msg;
				result=new AccountResult(Result.OK_CREATED, msgack.getUsername());

				recieved++;
				break;

			/* Account was not created. Reason explained on event. */
			case S2C_CREATEACCOUNT_NACK:
				logger.debug("Create account NACK");
				throw new CreateAccountFailedException(((MessageS2CCreateAccountNACK) msg).getResolution());
			}
		}

		return result;
	}

	/**
	 * Request server to create a character on server.
	 * You must have successfully logged into server before invoking this method.
	 *
	 * @param character the character to create
	 * @param template an object template to create the player avatar.
	 *
	 * @throws InvalidVersionException if we are not using a compatible version
	 * @throws TimeoutException  if timeout happens while waiting for the message.
	 * @throws CreateCharacterFailedException
	 */
	public synchronized CharacterResult createCharacter(String character, RPObject template) throws TimeoutException, InvalidVersionException, CreateCharacterFailedException {
		Message msgCA = new MessageC2SCreateCharacter(null, character, template);

		netMan.addMessage(msgCA);

		int received = 0;

		CharacterResult result=null;

		while (received != 2) {
			Message msg = getMessage();

			switch (msg.getType()) {
			/* Account was created */
			case S2C_CREATECHARACTER_ACK:
				logger.debug("Create character ACK");

				MessageS2CCreateCharacterACK msgack=(MessageS2CCreateCharacterACK)msg;

				result=new CharacterResult(Result.OK_CREATED, msgack.getCharacter(), msgack.getTemplate());
				received++;
				break;

				/* Server send the character list */
			case S2C_CHARACTERLIST:
				logger.debug("Recieved Character list");
				String[] characters = ((MessageS2CCharacterList) msg).getCharacters();

				/* We notify client of characters by calling the callback method. */
				onAvailableCharacters(characters);
				received++;
				break;

			/* Account was not created. Reason explained on event. */
			case S2C_CREATECHARACTER_NACK:
				logger.debug("Create character NACK");
				throw new CreateCharacterFailedException(((MessageS2CCreateAccountNACK) msg).getResolution());
			}
		}

		return result;
	}


	/**
	 * Sends a RPAction to server
	 * @param action the action to send to server.
	 */
	public void send(RPAction action) {
		MessageC2SAction msgAction = new MessageC2SAction(null, action);
		netMan.addMessage(msgAction);
	}

	/**
	 * Request logout of server
	 *
	 * @return true if we have successfully logout or false if server rejects to logout our player
	 * and maintain it on game world.
	 * @throws InvalidVersionException if we are not using a compatible version
	 * @throws TimeoutException  if timeout happens while waiting for the message.
	 */
	public synchronized boolean logout() throws InvalidVersionException, TimeoutException {
		Message msgL = new MessageC2SLogout(null);

		netMan.addMessage(msgL);
		int received = 0;

		while (received != 1) {
			Message msg = getMessage();
			switch (msg.getType()) {
			case S2C_LOGOUT_ACK:
				logger.debug("Logout ACK");
				return true;
			case S2C_LOGOUT_NACK:
				logger.debug("Logout NACK");
				return false;
			}
		}

		return false;
	}

	/**
	 * Disconnect the socket and finish the network communications.
	 *
	 */
	public void close() {
		if(netMan!=null) {
			/*
			 * Netman is null while we don't call connect method.
			 */
			netMan.finish();
		}
	}

	/**
	 * Call this method to get and apply messages
	 *
	 * @param delta unused
	 */
	public synchronized boolean loop(int delta) {
		boolean receivedMessages = false;

		/* Check network for new messages. */
		Message newmsg=null;
		try {
			newmsg = netMan.getMessage(30);
		} catch (InvalidVersionException e) {
			logger.warn("This should never happen: ",e);
		}

		if (newmsg != null) {
			messages.add(newmsg);
		}

		/* For all the received messages do */
		for (Message msg : messages) {
			receivedMessages = true;

			switch (msg.getType()) {
			/* It can be a perception message */
			case S2C_PERCEPTION: {
				logger.debug("Processing Message Perception");
				MessageS2CPerception msgPer = (MessageS2CPerception) msg;
				onPerception(msgPer);

				break;
			}

			/* or it can be a transfer request message */
			case S2C_TRANSFER_REQ: {
				logger.debug("Processing Content Transfer Request");
				List<TransferContent> items = ((MessageS2CTransferREQ) msg).getContents();

				items = onTransferREQ(items);

				MessageC2STransferACK reply = new MessageC2STransferACK(null, items);
				netMan.addMessage(reply);

				break;
			}

			/* or it can be the data tranfer itself */
			case S2C_TRANSFER: {
				logger.debug("Processing Content Transfer");
				List<TransferContent> items = ((MessageS2CTransfer) msg).getContents();
				onTransfer(items);

				break;
			}
			}
		}

		messages.clear();

		return receivedMessages;
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
	 * perception.
	 * @param message the perception message itself.
	 */
	abstract protected void onPerception(MessageS2CPerception message);

	/**
	 * It is called on a transfer request so you can choose what items to
	 * approve or reject
	 * @param items the items to approve or reject the transmision.
	 * @return the list of approved and rejected items.
	 */
	abstract protected List<TransferContent> onTransferREQ(List<TransferContent> items);

	/**
	 * It is called when we get a transfer of content
	 * @param items the transfered items.
	 */
	abstract protected void onTransfer(List<TransferContent> items);

	/**
	 * It is called when we get the list of characters
	 * @param characters the characters we have available at this account.
	 */
	abstract protected void onAvailableCharacters(String[] characters);

	/**
	 * It is called when we get the list of server information strings
	 * @param info the list of server strings with information.
	 */
	abstract protected void onServerInfo(String[] info);

	/**
	 * Returns the name of the game that this client implements
	 * @return the name of the game that this client implements
	 */
	abstract protected String getGameName();

	/**
	 * Returns the version number of the game
	 * @return the version number of the game
	 */
	abstract protected String getVersionNumber();
}
