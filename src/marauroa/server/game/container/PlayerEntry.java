/* $Id: PlayerEntry.java,v 1.33 2007/05/03 18:58:13 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
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
import java.util.List;

import marauroa.common.TimeoutConf;
import marauroa.common.crypto.RSAKey;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.TransferContent;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.server.game.db.IDatabase;
import marauroa.server.game.db.Transaction;

/**
 * This class represent a player on game. It handles all the bussiness glue that
 * it is needed by the server.
 *
 * @author miguel
 */
public class PlayerEntry {

	/** A object representing the database */
	static IDatabase playerDatabase;

	/** Get the database object. */
	public static void initDatabase() {
		playerDatabase = DatabaseFactory.getDatabase();
	}

	/**
	 * This class store the information needed to allow a secure login. Once
	 * login is completed the information is cleared.
	 */
	static public class SecuredLoginInfo {

		/** A long array of bytes that represent the Hash of a random value. */
		public byte[] serverNonce;

		/** A long array of bytes that represent a random value. */
		public byte[] clientNonce;

		/** A long byte array that represetn the hash of the client Nonce field */
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

		/**
		 * Constructor
		 *
		 * @param key
		 *            the server private key
		 * @param clientNonceHash
		 *            the client hash
		 * @param serverNonce
		 *            the server random bigint
		 */
		public SecuredLoginInfo(RSAKey key, byte[] clientNonceHash, byte[] serverNonce) {
			this.key = key;
			this.clientNonceHash = clientNonceHash;
			this.serverNonce = serverNonce;
		}

		/**
		 * Verify that a player is whom he/she says it is.
		 *
		 * @return true if it is correct: username and password matches.
		 * @throws SQLException
		 *             if there is any database problem.
		 */
		public boolean verify() throws SQLException {
			return playerDatabase.verify(playerDatabase.getTransaction(), this);
		}

		/**
		 * Add a login event to database each time player login, even if it
		 * fails.
		 *
		 * @param address
		 *            the IP address that originated the request.
		 * @param loginResult
		 *            the result of the login action, where true is login
		 *            correct and false login failed.
		 * @throws SQLException
		 *             if there is any database problem.
		 */
		public void addLoginEvent(InetAddress address, boolean loginResult) throws SQLException {
			Transaction transaction = playerDatabase.getTransaction();

			transaction.begin();
			playerDatabase.addLoginEvent(transaction, username, address, loginResult);
			transaction.commit();
		}

		/**
		 * Returns true if an account is temporally blocked due to too many
		 * tries on the defined time frame.
		 *
		 * @return true if an account is temporally blocked due to too many
		 *         tries on the defined time frame.
		 * @throws SQLException
		 *             if there is any database problem.
		 */
		public boolean isAccountBlocked() throws SQLException {
			Transaction transaction = playerDatabase.getTransaction();
			return playerDatabase.isAccountBlocked(transaction, username);
		}

		/**
		 * Returns a string indicating the status of the account.
		 * It can be: <ul>
		 * <li>active
		 * <li>inactive
		 * <li>banned
		 * </ul>
		 * @return a string indicating the status of the account.
		 * @throws SQLException
		 */
		public String getStatus() throws SQLException {
			Transaction transaction = playerDatabase.getTransaction();
			if(playerDatabase.hasPlayer(transaction, username)) {
				return playerDatabase.getAccountStatus(transaction, username);
			} else {
				return null;
			}			
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

	/** The name of the choosen character */
	public String character;

	/** The object of the player */
	public RPObject object;

	/**
	 * A counter to detect dropped packets or bad order at client side. We
	 * enumerate each perception so client can know in which order it is
	 * expected to apply them. When using TCP there is no problem as delivery is
	 * guaranted.
	 */
	public int perceptionCounter;

	/** It is true if client notified us that it got out of sync */
	public boolean requestedSync;

	/** Contains the content that is going to be transfered to client */
	public List<TransferContent> contentToTransfer;

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
		contentToTransfer = null;

		creationTime = System.currentTimeMillis();
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
	 * Returns the next perception timestamp.
	 *
	 * @return the next perception timestamp
	 */
	public int getPerceptionTimestamp() {
		return perceptionCounter++;
	}

	/**
	 * Clears the contents to be transfered
	 */
	public void clearContent() {
		contentToTransfer = null;
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
			return null;
		}

		for (TransferContent item : contentToTransfer) {
			if (item.name.equals(name)) {
				return item;
			}
		}

		return null;
	}

	/**
	 * This method stores an object at database backend
	 *
	 * @param player
	 *            the object to store
	 * @throws SQLException
	 */
	public void storeRPObject(RPObject player) throws SQLException, IOException {
		Transaction transaction = playerDatabase.getTransaction();

		try {
			transaction.begin();

			/* We store the object in the database */
			playerDatabase.storeCharacter(transaction, username, character, player);

			/* And update the entry */
			object = player;

			transaction.commit();
		} catch (SQLException e) {
			transaction.rollback();
			throw e;
		} catch (IOException e) {
			transaction.rollback();
			throw e;
		}
	}

	/**
	 * This method query database to check if the player with username given by
	 * the entry has a character with the name passed as argument.
	 *
	 * @param character
	 *            The name we are querying for.
	 * @return true if it is found or false otherwise.
	 * @throws Exception
	 *             If there is a Database exception.
	 */
	public boolean hasCharacter(String character) throws SQLException {
		return playerDatabase.hasCharacter(playerDatabase.getTransaction(), username, character);
	}

	/**
	 * Allows to ban this player account
	 * @throws SQLException
	 */
	public void ban() throws SQLException {
		playerDatabase.setAccountStatus(playerDatabase.getTransaction(), username, "banned");
	}
	
	/**
	 * This method loads the object pointed by username and character from
	 * database and assign already it to the entry.
	 *
	 * @return the loaded object
	 * @throws IOException
	 * @throws IOException
	 *             if the load fails.
	 * @throws SQLException
	 *             in case of an database error
	 */
	public RPObject loadRPObject() throws SQLException, IOException {
		object = playerDatabase.loadCharacter(playerDatabase.getTransaction(), username, character);
		return object;
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
		return playerDatabase.getCharacters(playerDatabase.getTransaction(), username);
	}

	/**
	 * This method forces an update on the next perception sending.
	 */
	public void requestSync() {
		requestedSync = true;
	}

	/**
	 * Return a list of the previous login attemps.
	 *
	 * @return a list of the previous login attemps.
	 * @throws SQLException
	 */
	public List<String> getPreviousLogins() throws SQLException {
		return playerDatabase.getLoginEvents(playerDatabase.getTransaction(), username, 1);
	}

	/**
	 * This method tag this entry as removable if there is more than
	 * UNCOMPLETED_LOGIN_TIMEOUT milliseconds since the creation time of the
	 * entry and the actual time and the entry has not completed the login
	 * stage.
	 *
	 * @return true, if it is removeable, false otherwise
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

	@Override
	public String toString() {
		StringBuffer os = new StringBuffer("PlayerEntry");
		os.append("[clientid=" + clientid + "]");
		os.append("[state=" + state + "]");
		os.append("[username=" + username + "]");
		os.append("[character=" + character + "]");

		return os.toString();
	}
}
