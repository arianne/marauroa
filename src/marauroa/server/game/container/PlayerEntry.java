/* $Id: PlayerEntry.java,v 1.14 2007/02/28 20:37:50 arianne_rpg Exp $ */
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
import java.net.InetSocketAddress;
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
 * This class represent a player on game.
 * It handles all the bussiness glue that it is needed by the server.
 * @author miguel *
 */
public class PlayerEntry {
	/** A object representing the database */
	protected static IDatabase playerDatabase;

	/** Get the database object. */
    public static void initDatabase() {
    		playerDatabase=DatabaseFactory.getDatabase();
    }

    /**
	 * This class store the information needed to allow a secure login.
	 * Once login is completed the information is cleared.
	 * @author miguel
	 */
	static public class SecuredLoginInfo {
		public byte[] clientNonceHash;
		public byte[] serverNonce;
		public byte[] clientNonce;
		public String username;
		public byte[] password;
		public RSAKey key;

		/**
		 *  Constructor
		 * @param key the server private key
		 * @param clientNonce the client hash
		 * @param serverNonce the server hash
		 */
		public SecuredLoginInfo(RSAKey key, byte[] clientNonce, byte[] serverNonce) {
			this.key = key;
			this.clientNonce=clientNonce;
			this.serverNonce=serverNonce;
		}

		/**
		 * Verify that a player is whom he/she says it is.
		 * @return true if it is correct: username and password matches.
		 * @throws SQLException if there is any database problem.
		 */
		public boolean verify() throws SQLException {
			return playerDatabase.verify(playerDatabase.getTransaction(), this);
		}

		/**
		 * Add a login event to database each time player login, even if it fails.
		 * @param address the IP address that originated the request.
		 * @param loginResult the result of the login action, where true is login correct and false login failed.
		 * @throws SQLException if there is any database problem.
		 */
		public void addLoginEvent(InetSocketAddress address, boolean loginResult) throws SQLException {
    		Transaction transaction=playerDatabase.getTransaction();

    		transaction.begin();
			playerDatabase.addLoginEvent(transaction, username, address, loginResult);
			transaction.commit();
		}
	}

	/**
	 * We record when this player entry was created to remove players that don't complete
	 * login stage but that keep connected.
	 */
	public long creationTime;

	/** The state in which this player is */
	public ClientState state;

	/** The runtime clientid */
	public int clientid;

	/** The client associated SocketChannel */
	public SocketChannel channel;

	/**
	 * The login Info. It is created after the first login message and
	 * destroyed after the login is finished.
	 */
	public SecuredLoginInfo loginInformations;

	/** The name of the player */
	public String username;

	/** The name of the choosen character */
	public String character;

	/** The object of the player */
	public RPObject object;

	/** A counter to detect dropped packets or bad order at client side */
	public int perception_counter;

	/** It is true if client notified us that it got out of sync */
	public boolean requestedSync;

	/** Contains the content that is going to be transfered to client */
	public List<TransferContent> contentToTransfer;

	/**
	 * Constructor
	 * @param channel the socket channel
	 */
	public PlayerEntry(SocketChannel channel) {
		this.channel=channel;

		clientid=Message.CLIENTID_INVALID;
		state=ClientState.CONNECTION_ACCEPTED;
		loginInformations=null;
		username=null;
		character=null;
		object=null;
		perception_counter=0;
		requestedSync=false;
		contentToTransfer=null;

		creationTime=System.currentTimeMillis();
	}

	/**
	 * Return the inet address of this PlayerEntry.
	 * @return the inet address of this PlayerEntry.
	 */
	public InetAddress getAddress() {
		return channel.socket().getInetAddress();
	}

	/**
	 * Returns the next perception timestamp.
	 * @return the next perception timestamp
	 */
	public int getPerceptionTimestamp() {
		return perception_counter++;
	}

	/** Clears the contents to be transfered */
	public void clearContent() {
		contentToTransfer = null;
	}

	/**
	 * Returns the named content or returns null if it is not found.
	 * @param name name of the content to find
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
	 * @param player the object to store
	 * @throws SQLException
	 */
	public void storeRPObject(RPObject player) throws SQLException,IOException {
		Transaction transaction=playerDatabase.getTransaction();

		try {
			transaction.begin();

			/* We store the object in the database */
			playerDatabase.storeCharacter(transaction, username, character, player);

			/* And update the entry */
			object=player;

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
	 * This method query database to check if the player with username given by the entry
	 * has a character with the name passed as argument.
	 *
	 * @param character The name we are querying for.
	 * @return true if it is found or false otherwise.
	 * @throws Exception If there is a Database exception.
	 */
	public boolean hasCharacter(String character) throws Exception {
		return playerDatabase.hasCharacter(playerDatabase.getTransaction(), username, character);
	}

	/**
	 * This method loads the object pointed by username and character from database
	 * and assign already it to the entry.
	 * @return the loaded object
	 * @throws IOException
	 * @throws Exception if the load fails.
	 */
	public RPObject loadRPObject() throws SQLException, IOException {
		object = playerDatabase.loadCharacter(playerDatabase.getTransaction(),username, character);
		return object;
	}

	/**
	 * This method returns a list of all the characters available for this player
	 * @return a list containing all the usable characters
	 * @throws SQLException if there is any database problem.
	 */
	public List<String> getCharacters() throws SQLException {
		return playerDatabase.getCharacters(playerDatabase.getTransaction(), username);
	}

	/**
	 * This method forces an update on the next perception sending.
	 */
	public void requestSync() {
		requestedSync=true;
	}

	/**
	 * Return a list of the previous login attemps.
	 * @return a list of the previous login attemps.
	 * @throws SQLException
	 */
	public List<String> getPreviousLogins() throws SQLException {
		return playerDatabase.getLoginEvents(playerDatabase.getTransaction(), username, 1);
	}

	/**
	 * This method tag this entry as removable if there is more than UNCOMPLETED_LOGIN_TIMEOUT milliseconds
	 * since the creation time of the entry and the actual time and the entry has not completed
	 * the login stage.
	 * @return
	 */
	boolean isRemovable() {
		return state==ClientState.CONNECTION_ACCEPTED && System.currentTimeMillis()-creationTime>TimeoutConf.UNCOMPLETED_LOGIN_TIMEOUT;
	}
}
