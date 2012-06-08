/***************************************************************************
 *                   (C) Copyright 2007-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.container;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.TimeoutConf;
import marauroa.common.Utility;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAKey;
import marauroa.common.game.RPObject;
import marauroa.common.net.NetConst;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageS2CLoginNACK.Reasons;
import marauroa.common.net.message.TransferContent;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.LoginEventDAO;
import marauroa.server.game.dbcommand.StoreCharacterCommand;

/**
 * This class represent a player on game. It handles all the business glue that
 * it is needed by the server.
 *
 * @author miguel
 */
public class PlayerEntry {
	private static Logger logger = Log4J.getLogger(PlayerEntry.class);

	/**
	 * This class stores the information needed to allow a secure login. Once
	 * login is completed the information is cleared.
	 */
	public static class SecuredLoginInfo {
		@SuppressWarnings("hiding")
		private static Logger logger = Log4J.getLogger(SecuredLoginInfo.class);

		/** A long array of bytes that represent the Hash of a random value. */
		public byte[] serverNonce;

		/** A long array of bytes that represent a random value. */
		public byte[] clientNonce;

		/** A long byte array that represent the hash of the client Nonce field */
		public byte[] clientNonceHash;

		/** Username of the player */
		public String username;

		/**
		 * An array that represent the hash of the password xor ClientNonce xor
		 * ServerNonce.
		 */
		public byte[] password;

		/** The server RSA key. */
		public RSAKey key;

		/** client ip address */
		public InetAddress address;

		/** seed identifying the client */
		public String seed;

		/** reason why a login failed */
		public Reasons reason;

		/**
		 * Constructor
		 *
		 * @param key
		 *            the server private key
		 * @param clientNonceHash
		 *            the client hash
		 * @param serverNonce
		 *            the server random bigint
		 * @param address client ip address
		 */
		public SecuredLoginInfo(RSAKey key, byte[] clientNonceHash, byte[] serverNonce, InetAddress address) {
			this.key = key;
			this.clientNonceHash = Utility.copy(clientNonceHash);
			this.serverNonce = Utility.copy(serverNonce);
			this.address = address;
		}

		/**
		 * Verify that a player is whom he/she says it is.
		 *
		 * @return true if it is correct: username and password matches.
		 * @throws SQLException
		 *             if there is any database problem.
		 */
		public boolean verify() throws SQLException {
			return DAORegister.get().get(AccountDAO.class).verify(this);
		}

		/**
		 * Add a login event to database each time player login, even if it
		 * fails.
		 *
		 * @param addr the IP address that originated the request.
		 * @param result 0 failed password, 1 successful login, 2 banned, 3 inactive, 4 blocked, 5 merged
		 * @throws SQLException if there is any database problem.
		 */
		public void addLoginEvent(InetAddress addr, int result) throws SQLException {
			String service = null;
			try {
				Configuration conf = Configuration.getConfiguration();
				if (conf.has("server_service")) {
					service = conf.get("server_service");
				} else {
					service = conf.get("server_typeGame");
				}
			} catch (IOException e) {
				logger.error(e, e);
			}
			DAORegister.get().get(LoginEventDAO.class).addLoginEvent(username, addr, service, seed, result);
		}

		/**
		 * counts the number of connections from this ip-address
		 *
		 * @param playerContainer PlayerEntryContainer
		 * @return number of active connections
		 */
		public int countConnectionsFromSameIPAddress(PlayerEntryContainer playerContainer) {
			if (address == null) {
				return 0;
			}
			int counter = 0;
			for (PlayerEntry playerEntry : playerContainer) {
				try {
					if ((playerEntry.getAddress() != null) && address.getHostAddress().equals(playerEntry.getAddress().getHostAddress())) {
						counter++;
					}
				} catch (NullPointerException e) {
					logger.error(address);
					logger.error(address.getHostAddress());
					logger.error(playerEntry);
					logger.error(playerEntry);
					logger.error(playerEntry.getAddress());
					logger.error(e, e);
				}
			}
			return counter;
		}

		/**
		 * Returns true if an account is temporarily blocked due to too many
		 * tries in the defined time frame.
		 *
		 * @return true if an account is temporarily blocked due to too many
		 *         tries in the defined time frame.
		 * @throws SQLException
		 *             if there is any database problem.
		 */
		public boolean isBlocked() throws SQLException {
			DBTransaction transaction = TransactionPool.get().beginWork();
			boolean res = true;
			try {
				LoginEventDAO loginEventDAO = DAORegister.get().get(LoginEventDAO.class);
				res = loginEventDAO.isAccountBlocked(transaction, username)
					|| loginEventDAO.isAddressBlocked(transaction, address.getHostAddress());

				TransactionPool.get().commit(transaction);
			} catch (SQLException e) {
				TransactionPool.get().rollback(transaction);
				logger.error(e, e);
			}
			return res;
		}

		/**
		 * gets the decrypted password
		 *
		 * @return the decrypted password hash
		 */
		public byte[] getDecryptedPasswordHash() {
			byte[] b1 = key.decodeByteArray(password);
			byte[] b2 = Hash.xor(clientNonce, serverNonce);
			if (b2 == null) {
				logger.debug("B2 is null");
				return null;
			}

			byte[] passwordHash = Hash.xor(b1, b2);
			if (password == null) {
				logger.debug("Password is null");
				return null;
			}
			return passwordHash;
		}

		/**
		 * returns a string suitable for debug output of this DBCommand.
		 *
		 * @return debug string
		 */
		@Override
		public String toString() {
			return "SecuredLoginInfo [username=" + username + ", address="
					+ address + ", seed=" + seed + ", reason=" + reason + "]";
		}
	}

	/**
	 * We record when this player entry was created to remove players that don't
	 * complete login stage but that keep connected.
	 */
	public long creationTime;

	/** The state in which this player is */
	public ClientState state;

	/** The runtime clientid */
	public int clientid;

	/** The client associated SocketChannel */
	public SocketChannel channel;

	/**
	 * The login Info. It is created after the first login message and destroyed
	 * after the login is finished.
	 */
	public SecuredLoginInfo loginInformations;

	/** The name of the player */
	public String username;

	/** The name of the chosen character */
	public String character;

	/** The object of the player */
	public RPObject object;

	/**
	 * We need to control if player is active because sometimes server changes its IP
	 * and we are not able to realize about this at server side, so all the clients are left there
	 * until the TCP stack determine a timeout that can be a long time.
	 */
	public long activityTimestamp;

	/**
	 * We define how many milliseconds has to be elapsed until we consider a player has timeout.
	 */
	private static final long TIMEOUT_IN_GAME_MILLISECONDS = 30 * 1000;

	/**
	 * We need a longer timeout pre-game because players might want to create a character here,
	 * there is no keep-alive yet.
	 */
	private static final long TIMEOUT_PRE_GAME_MILLISECONDS = 10 * 60 * 1000;

	/**
	 * A counter to detect dropped packets or bad order at client side. We
	 * enumerate each perception so client can know in which order it is
	 * expected to apply them. When using TCP there is no problem as delivery is
	 * guaranteed.
	 */
	public int perceptionCounter;

	/** It is true if client notified us that it got out of sync */
	public boolean requestedSync;

	/** Contains the content that is going to be transfered to client */
	public List<TransferContent> contentToTransfer;

	/** version of protocol this client speaks */
	private int protocolVersion = NetConst.NETWORK_PROTOCOL_VERSION;

	/** grant a longer timeout during login */
	private boolean gotKeepAliveInGameState = false;

	/** the number of characters owned by this account */
	public int characterCounter;

	/**
	 * Constructor
	 *
	 * @param channel
	 *            the socket channel
	 */
	public PlayerEntry(SocketChannel channel) {
		this.channel = channel;

		clientid = Message.CLIENTID_INVALID;
		state = ClientState.CONNECTION_ACCEPTED;
		loginInformations = null;
		username = null;
		character = null;
		object = null;
		perceptionCounter = 0;
		/*
		 * We set this to true so that RP Manager will send a sync perception to
		 * player as soon as possible.
		 */
		requestedSync = true;
		contentToTransfer = Collections.synchronizedList(new LinkedList<TransferContent>());

		creationTime = System.currentTimeMillis();
		activityTimestamp=creationTime;
	}

	/**
	 * Return the inet address of this PlayerEntry.
	 *
	 * @return the inet address of this PlayerEntry.
	 */
	public InetAddress getAddress() {
		return channel.socket().getInetAddress();
	}

	/**
	 * Returns true when nothing has been received from client in TIMEOUT_SECONDS.
	 * Note that client sends confirmations to perceptions, so this mean that client is
	 * for whatever reason not working.
	 *
	 * @return  true when nothing has been received from client in TIMEOUT_SECONDS.
	 */
	public boolean isTimeout() {
		if (state==ClientState.GAME_BEGIN) {
			if (gotKeepAliveInGameState) {
				return (System.currentTimeMillis()-activityTimestamp)>TIMEOUT_IN_GAME_MILLISECONDS;
			} else {
				return (System.currentTimeMillis()-activityTimestamp)>TIMEOUT_IN_GAME_MILLISECONDS * 4;
			}
		} else {
			return (System.currentTimeMillis()-activityTimestamp)>TIMEOUT_PRE_GAME_MILLISECONDS;
		}
	}

	/**
	 * Refresh player timeout timestamp.
	 * This method is invoked when a new message arrives from client.
	 */
	public void update() {
		activityTimestamp=System.currentTimeMillis();
		if (state==ClientState.GAME_BEGIN) {
			gotKeepAliveInGameState  = true;
		}
	}

	/**
	 * Returns the next perception timestamp.
	 *
	 * @return the next perception timestamp
	 */
	public int getPerceptionTimestamp() {
		return perceptionCounter++;
	}

	/**
	 * returns the current perception timestamp
	 *
	 * @return the current perception timestamp
	 */
	public int getThisPerceptionTimestamp() {
		return perceptionCounter;
	}

	/**
	 * Clears the specified content to be transfered
	 *
	 * @param content TransferContent to remove from the queue
	 */
	public void removeContent(TransferContent content) {
		if (!contentToTransfer.remove(content)) {
			logger.warn("Trying to clean unknown content: " + content);
		}
	}

	/**
	 * Returns the named content or returns null if it is not found.
	 *
	 * @param name
	 *            name of the content to find
	 * @return the content or null if it is not found.
	 */
	public TransferContent getContent(String name) {
		if (contentToTransfer == null) {
			logger.warn("contentToTransfer is null.");
			return null;
		}

		synchronized(contentToTransfer) {
			for (TransferContent item : contentToTransfer) {
				if (item.name.equals(name)) {
					return item;
				}
			}
		}

		logger.warn("content " + name + " not found.");
		return null;
	}

	/**
	 * This method stores an object at database backend
	 *
	 * @param player
	 *            the object to store
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public void storeRPObject(RPObject player) throws SQLException, IOException {
		// And update the entry
		object = player;

		// We store the object in the database
		DBCommand command = new StoreCharacterCommand(username, character, player);
		DBCommandQueue.get().enqueue(command);
	}

	/**
	 * This method query database to check if the player with username given by
	 * the entry has a character with the name passed as argument.
	 *
	 * @param charname
	 *            The name we are querying for.
	 * @return true if it is found or false otherwise.
	 * @throws SQLException
	 *             If there is a Database exception.
	 */
	public boolean hasCharacter(String charname) throws SQLException {
		return DAORegister.get().get(CharacterDAO.class).hasCharacter(username, charname);
	}

	/**
	 * Allows to ban this player account
	 * @throws SQLException
	 */
	public void ban() throws SQLException {
		DAORegister.get().get(AccountDAO.class).setAccountStatus(username, "banned");
	}

	/**
	 * sets the RPObject for this entry.
	 *
	 * @param object RPObject
	 */
	public void setObject(RPObject object) {
		this.object = object;
	}

	/**
	 * This method returns a list of all the characters available for this
	 * player
	 *
	 * @return a list containing all the usable characters
	 * @throws SQLException
	 *             if there is any database problem.
	 */
	public List<String> getCharacters() throws SQLException {
		return DAORegister.get().get(CharacterDAO.class).getCharacters(username);
	}

	/**
	 * This method forces an update on the next perception sending.
	 */
	public void requestSync() {
		requestedSync = true;
	}

	/**
	 * Return a list of the previous login attempts.
	 *
	 * @return a list of the previous login attempts.
	 * @throws SQLException
	 */
	public List<String> getPreviousLogins() throws SQLException {
		return DAORegister.get().get(LoginEventDAO.class).getLoginEvents(username, 1);
	}

	/**
	 * This method tag this entry as removable if there is more than
	 * UNCOMPLETED_LOGIN_TIMEOUT milliseconds since the creation time of the
	 * entry and the actual time and the entry has not completed the login
	 * stage.
	 *
	 * @return true, if it is removable, false otherwise
	 */
	boolean isRemovable() {
		/*
		 * Add logged players that didn't choose a character or that have not
		 * even login yet.
		 */
		boolean isInOKState = (state == ClientState.GAME_BEGIN);
		return !isInOKState
		        && System.currentTimeMillis() - creationTime > TimeoutConf.UNCOMPLETED_LOGIN_TIMEOUT;
	}

	/**
	 * gets the version of the protocol this clients speaks
	 *
	 * @return protocolVersion
	 */
	public int getProtocolVersion() {
		return protocolVersion;
	}


	/**
	 * sets the protocol version
	 *
	 * @param protocolVersion protocol version
	 */
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = Math.min(NetConst.NETWORK_PROTOCOL_VERSION, protocolVersion);
	}


	@Override
	public String toString() {
		StringBuffer os = new StringBuffer("PlayerEntry");
		os.append("[clientid=" + clientid + "]");
		os.append("[channel=" + channel + "]");
		os.append("[state=" + state + "]");
		os.append("[username=" + username + "]");
		os.append("[character=" + character + "]");
		os.append("[object defined=" + (object != null) + "]");

		return os.toString();
	}

}
