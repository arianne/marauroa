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
package marauroa.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import marauroa.client.net.INetworkClientManagerInterface;
import marauroa.client.net.KeepAliveSender;
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
import marauroa.common.net.message.MessageC2SKeepAlive;
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.message.MessageC2SLoginSendNonceNamePasswordAndSeed;
import marauroa.common.net.message.MessageC2SLoginSendPromise;
import marauroa.common.net.message.MessageC2SLogout;
import marauroa.common.net.message.MessageC2SOutOfSync;
import marauroa.common.net.message.MessageC2STransferACK;
import marauroa.common.net.message.MessageS2CCharacterList;
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
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.MessageS2CTransferREQ;
import marauroa.common.net.message.TransferContent;

/**
 * It is a wrapper over all the things that the client should do. You should
 * extend this class at your game.
 *
 * @author miguel
 *
 */
public abstract class ClientFramework {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(ClientFramework.class);

	/** How long we should wait for connect. */
	public final static int TIMEOUT = 10000;

	/** wait longer for an login to compensate for slow database operation */
	private final static int TIMEOUT_EXTENDED = 300000;

	/** a timer for keep a live messages */
	private Timer keepAliveTimer = null;

	/**
	 * We keep an instance of network manager to be able to communicate with
	 * server.
	 */
	protected INetworkClientManagerInterface netMan;

	/** We keep a list of all messages waiting for being processed. */
	private final List<Message> messages;

	/**
	 * Constructor.
	 *
	 * @param loggingProperties
	 *            contains the name of the file that configure the logging
	 *            system.
	 */
	public ClientFramework(String loggingProperties) {
		Log4J.init(loggingProperties);
		messages = new LinkedList<Message>();
	}

	/**
	 * Constructor.
	 *
	 */
	public ClientFramework() {
		Log4J.init();
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
		InetSocketAddress address = new InetSocketAddress(host, port);
		IOException originalException = null;
		boolean connected = false;

		// if a SOCKS-proxy is specified, try it first
		try {
			Proxy proxy = discoverProxy(host, port);
			if (proxy != Proxy.NO_PROXY) {
				connect(proxy, address);
				connected = true;
			}
		} catch (IOException e) {
			originalException = e;
		} catch (IllegalArgumentException e) {
			// workaround for javaws bug in discoverProxy (ProxySelector)
			// http://icedtea.classpath.org/bugzilla/show_bug.cgi?id=1055
			originalException = new IOException(e);
		}

		// if no proxy is specified, or it failed, try without proxy
		if (!connected) {
			try {
				connect(Proxy.NO_PROXY, address);
			} catch (IOException e) {
				// if this is only the fallback, throw the original exception instead
				if (originalException != null) {
					throw originalException;
				}
				throw e;
			}
		}
	}

	/**
	 * discovers a SOCKS proxy, ignores all other proxies
	 *
	 * @param host name or ip-address of server
	 * @param port port of server
	 * @return a Proxy object
	 * @throws IOException in case of an unexpected error
	 */
	private Proxy discoverProxy(String host, int port) throws IOException {
		ProxySelector selector = ProxySelector.getDefault();
		if (selector == null) {
			return Proxy.NO_PROXY;
		}

		String quotedHost = host;
		URI uri = null;
		if (!host.startsWith("[") && (host.indexOf(":") >= 0)) {
			quotedHost = "[" + host + "]";
		}
		try {
			uri = new URI("socket://" + quotedHost + ":" + port);
		} catch (URISyntaxException e) {
			IOException ioE = new IOException("Error, while discovering proxyserver: ");
			ioE.initCause(e);
			throw ioE;
		}

		List<Proxy> proxies = selector.select(uri);
		if (proxies == null) {
			return Proxy.NO_PROXY;
		}
		for (Proxy proxy : proxies) {
			if (proxy.type() == Proxy.Type.SOCKS) {
				return proxy;
			}
		}
		return Proxy.NO_PROXY;
	}

	/**
	 * Call this method to connect to server using a proxy-server inbetween.
	 * This method just configure the connection, it doesn't send anything.
	 *
	 * @param proxy proxy server to use for the connection
	 * @param serverAddress marauroa server (final destination)
	 * @throws IOException if connection is not possible
	 */
	public void connect(Proxy proxy, InetSocketAddress serverAddress) throws IOException {
		netMan = new TCPNetworkClientManager(proxy, serverAddress);
	}

	/**
	 * Retrieves a message from network manager.
	 *
	 * @return a message
	 * @throws InvalidVersionException
	 * @throws TimeoutException
	 *             if there is no message available in TIMEOUT milliseconds.
	 * @throws BannedAddressException
	 */
	private Message getMessage(int timeout) throws InvalidVersionException, TimeoutException,
	        BannedAddressException {
		Message msg = null;

		if (messages.isEmpty()) {
			msg = netMan.getMessage(timeout);

			if (msg instanceof MessageS2CConnectNACK) {
				throw new BannedAddressException();
			}

			if (msg == null) {
				throw new TimeoutException();
			}
		} else {
			msg = messages.remove(0);
		}

		logger.debug("CF getMessage: " + msg);
		return msg;
	}

	/**
	 * Request a synchronization with server. It shouldn't be needed now that we
	 * are using TCP.
	 */
	@Deprecated
	public synchronized void resync() {
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
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws LoginFailedException
	 *             if login is rejected
	 * @throws BannedAddressException
	 */
	public synchronized void login(String username, String password)
	        throws InvalidVersionException, TimeoutException, LoginFailedException,
	        BannedAddressException {
		login(username, password, null);
	}

	/**
	 * Login to server using the given username and password.
	 *
	 * @param username
	 *            Player username
	 * @param password
	 *            Player password
	 * @param seed
	 *            preauthentication seed
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws LoginFailedException
	 *             if login is rejected
	 * @throws BannedAddressException
	 */
	@SuppressWarnings("null")
	public synchronized void login(String username, String password, String seed)
	        throws InvalidVersionException, TimeoutException, LoginFailedException,
	        BannedAddressException {
		int received = 0;
		RSAPublicKey key = null;
		byte[] clientNonce = null;
		byte[] serverNonce = null;

		/* Send to server a login request and indicate the game name and version */
		netMan.addMessage(new MessageC2SLoginRequestKey(null, getGameName(), getVersionNumber()));

		int timeout = TIMEOUT;
		while (received < 3) {
			Message msg = getMessage(timeout);
			// Okay, now  we know that there is a marauroa server responding to the handshake.
			// We can give it more time for the next steps in case the database is slow.
			// Loging heavily depends on the database because number of failed logins for both
			// ip-address and username, banstatus, username&password have to be checked. And
			// the list of characters needs to be loaded from the database.
			timeout = TIMEOUT_EXTENDED;

			switch (msg.getType()) {
				case S2C_INVALIDMESSAGE: {
					throw new LoginFailedException(((MessageS2CInvalidMessage) msg).getReason());
				}
				/* Server sends its public RSA key */
				case S2C_LOGIN_SENDKEY: {
					logger.debug("Received Key");
					key = ((MessageS2CLoginSendKey) msg).getKey();

					clientNonce = Hash.random(Hash.hashLength());
					netMan.addMessage(new MessageC2SLoginSendPromise(null, Hash.hash(clientNonce)));
					break;
				}
					/* Server sends a random big integer */
				case S2C_LOGIN_SENDNONCE: {
					logger.debug("Received Server Nonce");
					if (serverNonce != null) {
						throw new LoginFailedException("Already received a serverNonce");
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
					if (seed != null) {
						byte[] bs = null;
						try {
							bs = seed.getBytes("UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.error(e, e);
						}
						if (bs.length != 16) {
							throw new LoginFailedException("Seed has not the correct length.");
						}
						byte[] b3 = null;
						b3 = Hash.xor(b1, bs);
						if (b3 == null) {
							throw new LoginFailedException("Incorrect hash seed");
						}
						byte[] cryptedSeed = key.encodeByteArray(b3);
						netMan.addMessage(new MessageC2SLoginSendNonceNamePasswordAndSeed(null,
					        clientNonce, username, cryptedPassword, cryptedSeed));
					} else {
						netMan.addMessage(new MessageC2SLoginSendNonceNameAndPassword(null,
					        clientNonce, username, cryptedPassword));
					}
					break;
				}
					/* Server replied with ACK to login operation */
				case S2C_LOGIN_ACK:
					logger.debug("Login correct");

					onPreviousLogins(((MessageS2CLoginACK) msg).getPreviousLogins());

					received++;
					break;
				/* Server send the character list */
				case S2C_CHARACTERLIST:
					logger.debug("Received Character list");

					/*
					 * We notify client of characters by calling the callback
					 * method.
					 */
					String[] characters = ((MessageS2CCharacterList) msg).getCharacters();
					onAvailableCharacters(characters);
					Map<String, RPObject> characterDetails = ((MessageS2CCharacterList) msg).getCharacterDetails();
					onAvailableCharacterDetails(characterDetails);
					received++;
					break;
				/*
				 * Server sends the server info message with information about
				 * versions, homepage, etc...
				 */
				case S2C_SERVERINFO:
					logger.debug("Received Server info");
					String[] info = ((MessageS2CServerInfo) msg).getContents();

					/* We notify client of this info by calling the callback method. */
					onServerInfo(info);
					received++;
					break;
				/* Login failed, explain reason on event */
				case S2C_LOGIN_NACK:
					MessageS2CLoginNACK msgNACK = (MessageS2CLoginNACK) msg;
					logger.debug("Login failed. Reason: " + msgNACK.getResolution());
					throw new LoginFailedException(msgNACK.getResolution(), msgNACK.getResolutionCode());

				/* Login failed, explain reason on event */
				case S2C_LOGIN_MESSAGE_NACK:
					MessageS2CLoginMessageNACK msgMessageNACK = (MessageS2CLoginMessageNACK) msg;
					logger.debug("Login failed. Reason: " + msgMessageNACK.getReason());
					throw new LoginFailedException(msgMessageNACK.getReason());

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
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws BannedAddressException
	 */
	public synchronized boolean chooseCharacter(String character) throws TimeoutException,
	        InvalidVersionException, BannedAddressException {
		Message msgCC = new MessageC2SChooseCharacter(null, character);
		netMan.addMessage(msgCC);

		int received = 0;

		while (received != 1) {
			Message msg = getMessage(TIMEOUT_EXTENDED);

			switch (msg.getType()) {
				/* Server accepted the character we chose */
				case S2C_CHOOSECHARACTER_ACK:
					logger.debug("Choose Character ACK");
					keepAliveTimer = new Timer("KeepAlive", true);
					keepAliveTimer.schedule(new KeepAliveSender(netMan), 1000, 10000);
					return true;
					/* Server rejected the character we chose. No reason */
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
	 * @param username
	 *            the player desired username
	 * @param password
	 *            the player password
	 * @param email
	 *            player's email for notifications and/or password reset.
	 * @return AccountResult
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws BannedAddressException
	 */
	public synchronized AccountResult createAccount(String username, String password, String email)
	        throws TimeoutException, InvalidVersionException, BannedAddressException {
		Message msgCA = new MessageC2SCreateAccount(null, username, password, email);

		netMan.addMessage(msgCA);

		int received = 0;

		AccountResult result = null;

		while (received != 1) {
			Message msg = getMessage(TIMEOUT_EXTENDED);

			switch (msg.getType()) {
				case S2C_INVALIDMESSAGE: {
					result = new AccountResult(Result.FAILED_EXCEPTION, username);
					break;
				}
				/* Account was created */
				case S2C_CREATEACCOUNT_ACK:
					logger.debug("Create account ACK");

					MessageS2CCreateAccountACK msgack = (MessageS2CCreateAccountACK) msg;
					result = new AccountResult(Result.OK_CREATED, msgack.getUsername());

					received++;
					break;

				/* Account was not created. Reason explained on event. */
				case S2C_CREATEACCOUNT_NACK:
					logger.debug("Create account NACK");
					MessageS2CCreateAccountNACK nack = (MessageS2CCreateAccountNACK) msg;
					result = new AccountResult(nack.getResolutionCode(), username);

					received++;
					break;
				default:
					logger.debug("Unexpected method while waiting for confirmation of account creation: " + msg);
			}
		}

		return result;
	}

	/**
	 * Request server to create a character on server. You must have
	 * successfully logged into server before invoking this method.
	 *
	 * @param character
	 *            the character to create
	 * @param template
	 *            an object template to create the player avatar.
	 * @return CharacterResult
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws BannedAddressException
	 */
	public synchronized CharacterResult createCharacter(String character, RPObject template)
	        throws TimeoutException, InvalidVersionException, BannedAddressException {
		Message msgCA = new MessageC2SCreateCharacter(null, character, template);

		netMan.addMessage(msgCA);

		int received = 0;

		CharacterResult result = null;

		while (received != 2) {
			Message msg = getMessage(TIMEOUT_EXTENDED);

			switch (msg.getType()) {
				/* Account was created */
				case S2C_CREATECHARACTER_ACK:
					logger.debug("Create character ACK");

					MessageS2CCreateCharacterACK msgack = (MessageS2CCreateCharacterACK) msg;

					result = new CharacterResult(Result.OK_CREATED, msgack.getCharacter(), msgack
					        .getTemplate());
					received++;
					break;

				/* Server send the character list */
				case S2C_CHARACTERLIST:
					logger.debug("Received Character list");
					/*
					 * We notify client of characters by calling the callback
					 * method.
					 */
					String[] characters = ((MessageS2CCharacterList) msg).getCharacters();
					onAvailableCharacters(characters);
					Map<String, RPObject> characterDetails = ((MessageS2CCharacterList) msg).getCharacterDetails();
					onAvailableCharacterDetails(characterDetails);

					received++;
					break;

				/* Account was not created. Reason explained on event and return. */
				case S2C_CREATECHARACTER_NACK:
					logger.debug("Create character NACK");
					MessageS2CCreateCharacterNACK reply = (MessageS2CCreateCharacterNACK) msg;

					result = new CharacterResult(reply.getResolutionCode(), character, template);
					return result;
			}
		}

		return result;
	}

	/**
	 * Sends a RPAction to server
	 *
	 * @param action
	 *            the action to send to server.
	 */
	public void send(RPAction action) {
		MessageC2SAction msgAction = new MessageC2SAction(null, action);
		netMan.addMessage(msgAction);
	}

	/**
	 * Request logout of server
	 *
	 * @return true if we have successfully logout or false if server rejects to
	 *         logout our player and maintain it on game world.
	 * @throws InvalidVersionException
	 *             if we are not using a compatible version
	 * @throws TimeoutException
	 *             if timeout happens while waiting for the message.
	 * @throws BannedAddressException
	 */
	public synchronized boolean logout() throws InvalidVersionException, TimeoutException,
	        BannedAddressException {
		Message msgL = new MessageC2SLogout(null);

		netMan.addMessage(msgL);
		int received = 0;

		while (received != 1) {
			Message msg = getMessage(TIMEOUT);
			switch (msg.getType()) {
				case S2C_LOGOUT_ACK:
					logger.debug("Logout ACK");
					if (keepAliveTimer != null) {
						keepAliveTimer.cancel();
					}
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
		if (keepAliveTimer != null) {
			keepAliveTimer.cancel();
		}

		// Netman is null while we don't call connect method.
		if (netMan != null) {
			netMan.finish();
		}
	}

	/**
	 * Call this method to get and apply messages
	 *
	 * @param delta
	 *            unused
	 * @return true if new messages were received.
	 */
	public synchronized boolean loop(@SuppressWarnings("unused") int delta) {
		boolean receivedMessages = false;

		/* Check network for new messages. */
		messages.addAll(((TCPNetworkClientManager) netMan).getMessages());
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

					/* or it can be the data transfer itself */
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
	 * sends a KeepAliveMessage, this is automatically done in game, but you may
	 * be required to call this method very five minutes in pre game.
	 */
	public void sendKeepAlive() {
		MessageC2SKeepAlive msg = new MessageC2SKeepAlive();
		netMan.addMessage(msg);
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
	 *
	 * @param message
	 *            the perception message itself.
	 */
	abstract protected void onPerception(MessageS2CPerception message);

	/**
	 * is called before a content transfer is started.
	 *
	 * <code> items </code> contains a list of names and timestamp.
	 * That information can be used to decide if a transfer from server is needed.
	 * By setting attribute ack to true in a TransferContent it will be acknowledged.
	 * All acknowledges items in the returned List, will be transfered by server.
	 *
	 * @param items
	 *            in this list by default all items.ack attributes are set to false;
	 * @return the list of approved and rejected items.
	 */
	abstract protected List<TransferContent> onTransferREQ(List<TransferContent> items);

	/**
	 * It is called when we get a transfer of content
	 *
	 * @param items
	 *            the transfered items.
	 */
	abstract protected void onTransfer(List<TransferContent> items);

	/**
	 * It is called when we get the list of characters
	 *
	 * @param characters
	 *            the characters we have available at this account.
	 */
	abstract protected void onAvailableCharacters(String[] characters);


	/**
	 * It is called when we get the list of characters
	 *
	 * @param characters
	 *            the characters we have available at this account.
	 */
	protected void onAvailableCharacterDetails(@SuppressWarnings("unused") Map<String, RPObject> characters) {
		// stub
	}


	/**
	 * It is called when we get the list of server information strings
	 *
	 * @param info
	 *            the list of server strings with information.
	 */
	abstract protected void onServerInfo(String[] info);

	/**
	 * Returns the name of the game that this client implements
	 *
	 * @return the name of the game that this client implements
	 */
	abstract protected String getGameName();

	/**
	 * Returns the version number of the game
	 *
	 * @return the version number of the game
	 */
	abstract protected String getVersionNumber();

	/**
	 * Call the client with a list of previous logins.
	 * @param previousLogins a list of strings with the previous logins
	 */
	abstract protected void onPreviousLogins(List<String> previousLogins);
}
