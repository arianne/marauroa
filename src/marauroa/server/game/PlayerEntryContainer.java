/* $Id: PlayerEntryContainer.java,v 1.13 2006/08/20 15:40:15 wikipedian Exp $ */
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
package marauroa.server.game;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.RSAKey;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPObject;
import marauroa.common.net.TransferContent;
import marauroa.server.RWLock;

import org.apache.log4j.Logger;

/**
 * This class contains a list of the Runtime players existing in Marauroa, but
 * it also links them with their representation in game and in database, so this
 * is the point to manage them all.
 */
public class PlayerEntryContainer {
	/** the logger instance. */
	private static final Logger logger = Log4J
			.getLogger(PlayerEntryContainer.class);

	enum ClientState {
		NULL, LOGIN_COMPLETE, GAME_BEGIN,
	}

	/**
	 * A class to store all the object information to use in runtime and access
	 * database
	 */
	static public class RuntimePlayerEntry {
		static public class SecuredLoginInfo {
			byte[] clientNonceHash;

			byte[] serverNonce;

			byte[] clientNonce;

			String userName;

			byte[] password;

			RSAKey key;

			SecuredLoginInfo(RSAKey key) {
				this.key = key;
			}
		}

		/** The runtime clientid */
		public int clientid;

		/** The state in which this player is */
		public ClientState state;

		/** The initial address of this player */
		public InetSocketAddress source;

		/** The time when the latest event was done in this player */
		public long timestamp;

		/**
		 * The login Info. It is created after the first login message and
		 * destroyed after the login is finished.
		 */
		public SecuredLoginInfo loginInformations;

		public boolean isTimedout() {
			long value = System.currentTimeMillis() - timestamp;
			if (value > TimeoutConf.GAMESERVER_PLAYER_TIMEOUT) {
				return true;
			} else {
				return false;
			}
		}

		/** The time when the latest event was done in this player */
		public long timestampLastStored;

		public RPObject database_storedRPObject;

		public boolean shouldStoredUpdate(RPObject object) {
			Log4J.startMethod(logger, "shouldStoredUpdate");

			boolean result = false;
			long value = System.currentTimeMillis() - timestampLastStored;

			if (value > TimeoutConf.GAMESERVER_PLAYER_STORE_LAPSUS) {
				timestampLastStored = System.currentTimeMillis();
				result = true;
			}

			Log4J.finishMethod(logger, "shouldStoredUpdate");
			return result;
		}

		/** The name of the choosen character */
		public String choosenCharacter;

		/** The name of the player */
		public String username;

		/** The rp object of the player */
		public RPObject.ID characterid;

		/** A counter to detect dropped packets or bad order at client side */
		public int perception_counter;

		public int getPerceptionTimestamp() {
			return perception_counter++;
		}

		/** It is true if client notified us that it got out of sync */
		public boolean perception_OutOfSync;

		/** Contains the content that is going to be transfered to client */
		List<TransferContent> contentToTransfer;

		public void clearContent() {
			contentToTransfer = null;
		}

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

		public String toString() {
			StringBuffer st = new StringBuffer("PlayerEntry(");
			st.append(characterid + ",");
			st.append(choosenCharacter + ",");
			st.append(clientid + ",");
			st.append(state + ",");
			st.append(username + ")");

			return st.toString();
		}
	}

	/** This class is a iterator over the player in PlayerEntryContainer */
	static public class ClientIDIterator {
		private Iterator entryIter;

		/** Constructor */
		private ClientIDIterator(Iterator iter) {
			entryIter = iter;
		}

		/**
		 * This method returns true if there are still most elements.
		 * 
		 * @return true if there are more elements.
		 */
		public boolean hasNext() {
			return (entryIter.hasNext());
		}

		/**
		 * This method returs the clientid and move the pointer to the next
		 * element
		 * 
		 * @return an clientid
		 */
		public int next() {
			Map.Entry entry = (Map.Entry) entryIter.next();

			return ((Integer) entry.getKey()).intValue();
		}

		public void remove() {
			entryIter.remove();
		}
	}

	/** This method returns an iterator of the players in the container */
	public ClientIDIterator iterator() {
		return new ClientIDIterator(listPlayerEntries.entrySet().iterator());
	}

	/**
	 * A HashMap<clientid,RuntimePlayerEntry to store RuntimePlayerEntry
	 * objects
	 */
	private HashMap<Integer, RuntimePlayerEntry> listPlayerEntries;

	/** A object representing the database */
	private IPlayerDatabase playerDatabase;

	private Transaction transaction;

	/** A reader/writers lock for controlling the access */
	private RWLock lock;

	private static PlayerEntryContainer playerEntryContainer;

	/** Constructor */
	private PlayerEntryContainer() throws Exception {
		/* Initialize the random number generator */
		rand.setSeed(new Date().getTime());
		lock = new RWLock();
		listPlayerEntries = new HashMap<Integer, RuntimePlayerEntry>();
		/* Choose the database type using configuration file */
		try {
			playerDatabase = PlayerDatabaseFactory.getDatabase();
			transaction = playerDatabase.getTransaction();
		} catch (Exception e) {
			logger.warn("ABORT: marauroad can't allocate database");
			throw e;
		}
	}

	/**
	 * This method returns an instance of PlayerEntryContainer
	 * 
	 * @return A shared instance of PlayerEntryContainer
	 */
	public static PlayerEntryContainer getContainer() throws Exception {
		if (playerEntryContainer == null) {
			playerEntryContainer = new PlayerEntryContainer();
		}
		return playerEntryContainer;
	}

	/**
	 * This method returns true if a player exist with that clientid.
	 * 
	 * @param clientid
	 *            a player runtime id
	 * @return true if player exist or false otherwise.
	 */
	public boolean hasRuntimePlayer(int clientid) {
		Log4J.startMethod(logger, "hasRuntimePlayer");
		try {
			return listPlayerEntries.containsKey(new Integer(clientid));
		} finally {
			Log4J.finishMethod(logger, "hasRuntimePlayer");
		}
	}

	/**
	 * This method creates a new instance of RuntimePlayerEntry and add it.
	 * 
	 * @param username
	 *            the name of the player
	 * @param source
	 *            the IP address of the player.
	 * @return the clientid for that runtimeplayer
	 */
	public int addRuntimePlayer(String username, InetSocketAddress source) {
		Log4J.startMethod(logger, "addRuntimePlayer");
		try {
			RuntimePlayerEntry entry = new RuntimePlayerEntry();

			entry.state = ClientState.NULL;
			entry.timestamp = System.currentTimeMillis();
			entry.timestampLastStored = System.currentTimeMillis();
			entry.source = source;
			entry.username = username;
			entry.choosenCharacter = null;
			entry.clientid = generateClientID(source);
			entry.perception_counter = 0;
			entry.perception_OutOfSync = true;

			listPlayerEntries.put(new Integer(entry.clientid), entry);
			return entry.clientid;
		} finally {
			Log4J.finishMethod(logger, "addRuntimePlayer");
		}
	}

	/**
	 * This method creates a new instance of RuntimePlayerEntry and add it.
	 * 
	 * @param username
	 *            the name of the player
	 * @param source
	 *            the IP address of the player.
	 * @return the clientid for that runtimeplayer
	 */
	public int addRuntimePlayer(RSAKey key, byte[] clientNonceHash,
			InetSocketAddress source) {
		Log4J.startMethod(logger, "addRuntimePlayer");
		try {
			RuntimePlayerEntry entry = new RuntimePlayerEntry();

			entry.state = ClientState.NULL;
			entry.timestamp = System.currentTimeMillis();
			entry.timestampLastStored = System.currentTimeMillis();
			entry.source = source;
			entry.username = null;
			entry.choosenCharacter = null;
			entry.clientid = generateClientID(source);
			entry.perception_counter = 0;
			entry.perception_OutOfSync = true;
			entry.loginInformations = new RuntimePlayerEntry.SecuredLoginInfo(
					key);
			entry.loginInformations.clientNonceHash = clientNonceHash;

			listPlayerEntries.put(new Integer(entry.clientid), entry);
			return entry.clientid;
		} finally {
			Log4J.finishMethod(logger, "addRuntimePlayer");
		}
	}

	/**
	 * This method remove the entry if it exists.
	 * 
	 * @param clientid
	 *            is the runtime id of the player
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 */
	public void removeRuntimePlayer(int clientid)
			throws NoSuchClientIDException {
		Log4J.startMethod(logger, "removeRuntimePlayer");
		try {
			if (hasRuntimePlayer(clientid)) {
				listPlayerEntries.remove(new Integer(clientid));
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "removeRuntimePlayer");
		}
	}

	/**
	 * This method returns true if the clientid and the source address match.
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param source
	 *            the IP address of the player.
	 * @return true if they match or false otherwise
	 */
	public boolean verifyRuntimePlayer(int clientid, InetSocketAddress source) {
		Log4J.startMethod(logger, "verifyRuntimePlayer");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				if (source.equals(entry.source)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} finally {
			Log4J.finishMethod(logger, "verifyRuntimePlayer");
		}
	}

	/**
	 * This method returns true if the information is a correct login
	 * informations, and if there is a correct entry in the database.
	 * 
	 * @return true if informations are correct, false otherwise.
	 */
	public boolean verifyAccount(
			RuntimePlayerEntry.SecuredLoginInfo informations)
			throws GenericDatabaseException {
		Log4J.startMethod(logger, "verifyAccount");
		try {
			return playerDatabase.verifyAccount(transaction, informations);
		} catch (Exception e) {
			transaction = playerDatabase.getTransaction();
			logger.warn("error verifying account", e);
			throw new GenericDatabaseException(e);
		} finally {
			Log4J.finishMethod(logger, "verifyAccount");
		}
	}

	/**
	 * This method add a Login event to the player
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param source
	 *            the IP address of the player
	 * @param correctLogin
	 *            true if the login has been correct.
	 * 
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchPlayerFoundException
	 *             if the player doesn't exist in database.
	 */
	public void addLoginEvent(String username, InetSocketAddress source,
			boolean correctLogin) throws NoSuchClientIDException,
			NoSuchPlayerException, GenericDatabaseException {
		Log4J.startMethod(logger, "addLoginEvent");
		try {
			transaction.begin();
			playerDatabase.addLoginEvent(transaction, username, source,
					correctLogin);
			transaction.commit();
		} catch (PlayerNotFoundException e) {
			transaction.rollback();
			logger.warn("No such Player(" + username + ")");
			throw new NoSuchPlayerException(username);
		} catch (Exception e) {
			transaction.rollback();
			transaction = playerDatabase.getTransaction();

			logger.warn("error adding LoginEvent", e);
			throw new GenericDatabaseException(e);
		} finally {
			Log4J.finishMethod(logger, "addLoginEvent");
		}
	}

	/**
	 * This method returns the list of Login events as a array of Strings
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @return an array of String containing the login events.
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchPlayerFoundException
	 *             if the player doesn't exist in database.
	 */
	public String[] getLoginEvent(int clientid) throws NoSuchClientIDException,
			NoSuchPlayerException, GenericDatabaseException {
		Log4J.startMethod(logger, "getLoginEvent");
		try {
			if (hasRuntimePlayer(clientid)) {
				try {
					RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
							.get(new Integer(clientid));

					return playerDatabase.getLoginEvent(transaction,
							entry.username);
				} catch (PlayerNotFoundException e) {
					logger.warn("No such Player(" + clientid + ")", e);
					throw new NoSuchPlayerException("[clientid:" + clientid
							+ "]");
				} catch (Exception e) {
					transaction = playerDatabase.getTransaction();
					logger.warn("error getting LoginEvent", e);
					throw new GenericDatabaseException(e);
				}
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "getLoginEvent");
		}
	}

	/**
	 * This method returns true if the PlayerContainer has the player pointed by
	 * username
	 * 
	 * @param username
	 *            the name of the player we are asking if it exists.
	 * @return true if player exists or false otherwise.
	 */
	public boolean hasPlayer(String username) {
		Log4J.startMethod(logger, "hasPlayer");
		try {
			Iterator it = listPlayerEntries.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				RuntimePlayerEntry playerEntry = (RuntimePlayerEntry) entry
						.getValue();

				if (playerEntry.username != null
						&& playerEntry.username.equals(username)) {
					return true;
				}
			}
			return false;
		} finally {
			Log4J.finishMethod(logger, "hasPlayer");
		}
	}

	/**
	 * This method returns true if the playerentryContainer has the player
	 * pointed by username
	 * 
	 * @param username
	 *            the name of the player we are asking if it exists.
	 * @return true if player exists or false otherwise.
	 */
	public int getClientidPlayer(String username) {
		Log4J.startMethod(logger, "getClientidPlayer");
		try {
			Iterator it = listPlayerEntries.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				RuntimePlayerEntry playerEntry = (RuntimePlayerEntry) entry
						.getValue();

				if (playerEntry.username != null
						&& playerEntry.username.equals(username)) {
					return playerEntry.clientid;
				}
			}
			return -1;
		} finally {
			Log4J.finishMethod(logger, "getClientidPlayer");
		}
	}

	public int getClientidPlayer(RPObject.ID id) {
		Log4J.startMethod(logger, "getClientidPlayer");
		try {
			Iterator it = listPlayerEntries.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				RuntimePlayerEntry playerEntry = (RuntimePlayerEntry) entry
						.getValue();

				logger.debug("getClientidPlayer [" + playerEntry + "]");

				if (playerEntry.state == ClientState.GAME_BEGIN
						&& playerEntry.characterid.equals(id)) {
					return playerEntry.clientid;
				}
			}

			return -1;
		} finally {
			Log4J.finishMethod(logger, "getClientidPlayer");
		}
	}

	/**
	 * This method returns true if the player has that character or false if it
	 * hasn't
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param character
	 *            is the name of the character
	 * @return true if player has the character or false if it hasn't
	 * 
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchPlayerFoundException
	 *             if the player doesn't exist in database.
	 */
	public boolean hasCharacter(int clientid, String character)
			throws NoSuchClientIDException, NoSuchPlayerException,
			GenericDatabaseException {
		Log4J.startMethod(logger, "hasCharacter");
		try {
			if (hasRuntimePlayer(clientid)) {
				try {
					RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
							.get(new Integer(clientid));

					return playerDatabase.hasCharacter(transaction,
							entry.username, character);
				} catch (PlayerNotFoundException e) {
					logger.debug("No such Player(" + clientid + ")", e);
					throw new NoSuchPlayerException("[clientid: " + clientid
							+ "]");
				} catch (Exception e) {
					transaction = playerDatabase.getTransaction();
					logger.warn("error while checking if client " + clientid
							+ " has Character " + character, e);
					throw new GenericDatabaseException(e);
				}
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "hasCharacter");
		}
	}

	/**
	 * This method assign the character to the playerEntry.
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param character
	 *            is the name of the character
	 * 
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 */
	public void setChoosenCharacter(int clientid, String character)
			throws NoSuchClientIDException {
		Log4J.startMethod(logger, "setChoosenCharacter");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				entry.choosenCharacter = character;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "setChoosenCharacter");
		}
	}

	/**
	 * This method returns the lis of character that the player pointed by
	 * username has.
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @return an array of String with the characters
	 * 
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchPlayerFoundException
	 *             if the player doesn't exist in database.
	 */
	public String[] getCharacterList(int clientid)
			throws NoSuchClientIDException, NoSuchPlayerException,
			GenericDatabaseException {
		Log4J.startMethod(logger, "getCharacterList");
		try {
			if (hasRuntimePlayer(clientid)) {
				try {
					RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
							.get(new Integer(clientid));

					return playerDatabase.getCharactersList(transaction,
							entry.username);
				} catch (PlayerNotFoundException e) {
					logger.debug("No such Player(" + clientid + ")", e);
					throw new NoSuchPlayerException("[clientid:" + clientid
							+ "]");
				} catch (Exception e) {
					transaction = playerDatabase.getTransaction();
					logger.warn("error getting CharacterList", e);
					throw new GenericDatabaseException(e);
				}
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "getCharacterList");
		}
	}

	/**
	 * This method retrieves from Database the object for an existing player and
	 * character.
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param character
	 *            is the name of the character that the username player wants to
	 *            add.
	 * @return a RPObject that is the RPObject that represent this character in
	 *         game.
	 * 
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchCharacterException
	 *             if character is not found
	 * @throws NoSuchPlayerException
	 *             if the player doesn't exist in database.
	 */
	public RPObject getRPObject(int clientid, String character)
			throws NoSuchClientIDException, NoSuchPlayerException,
			NoSuchCharacterException, GenericDatabaseException {
		Log4J.startMethod(logger, "getRPObject");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));
				RPObject object = playerDatabase.getRPObject(transaction,
						entry.username, character);

				entry.characterid = new RPObject.ID(object);
				return object;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} catch (PlayerNotFoundException e) {
			logger.debug("No such Player(" + clientid + ")", e);
			throw new NoSuchPlayerException("[clientid" + clientid + "]");
		} catch (AttributeNotFoundException e) {
			logger.error("error getting attribute " + e.getAttribute(), e);
			throw new NoSuchPlayerException("[clientid:" + clientid
					+ ", character:" + character + "]");
		} catch (CharacterNotFoundException e) {
			logger.error("No such Character(" + clientid + "/" + character
					+ ")", e);
			throw new NoSuchCharacterException(character);
		} catch (Exception e) {
			transaction = playerDatabase.getTransaction();

			logger.warn("error getting RPObject", e);
			throw new GenericDatabaseException(e);
		} finally {
			Log4J.finishMethod(logger, "getRPObject");
		}
	}

	/**
	 * This method is the opposite of getRPObject, and store in Database the
	 * object for an existing player and character. The difference between
	 * setRPObject and addCharacter are that setRPObject update it while
	 * addCharacter add it to database and fails if it already exists
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param object
	 *            is the RPObject that represent this character in game.
	 * 
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchCharacterException
	 *             if character is not found
	 * @throws NoSuchPlayerFoundException
	 *             if the player doesn't exist in database.
	 */
	public void setRPObject(int clientid, RPObject object)
			throws NoSuchClientIDException, NoSuchPlayerException,
			NoSuchCharacterException, GenericDatabaseException {
		Log4J.startMethod(logger, "setRPObject");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				transaction.begin();

				playerDatabase.setRPObject(transaction, entry.username,
						entry.choosenCharacter, object);
				entry.characterid = new RPObject.ID(object);
				transaction.commit();
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} catch (PlayerNotFoundException e) {
			transaction.rollback();
			logger.error("No such Player(" + clientid + ")", e);
			throw new NoSuchPlayerException("(" + clientid + ")");
		} catch (AttributeNotFoundException e) {
			transaction.rollback();
			logger.error("error while setting RPObject", e);
			throw new NoSuchPlayerException("- not available -");
		} catch (CharacterNotFoundException e) {
			transaction.rollback();
			logger.debug("No such Character(unknown)", e);
			throw new NoSuchCharacterException("- not available -");
		} catch (Exception e) {
			transaction.rollback();
			logger.warn("error setting RPObject", e);
			throw new GenericDatabaseException(e);
		} finally {
			Log4J.finishMethod(logger, "setRPObject");
		}
	}

	private static Random rand = new Random();

	private int generateClientID(InetSocketAddress source) {
		int clientid = rand.nextInt();

		while (hasRuntimePlayer(clientid)) {
			clientid = rand.nextInt();
		}

		return clientid;
	}

	protected int size() {
		return listPlayerEntries.size();
	}

	/**
	 * This method returns the lock so that you can control how the resource is
	 * used
	 * 
	 * @return the RWLock of the object
	 */
	public RWLock getLock() {
		return lock;
	}

	/**
	 * This method exposes directly the player info, so you can save valuable
	 * time.
	 */
	public RuntimePlayerEntry get(int clientid) throws NoSuchClientIDException {
		Log4J.startMethod(logger, "get");
		try {
			if (hasRuntimePlayer(clientid)) {
				return (RuntimePlayerEntry) listPlayerEntries.get(new Integer(
						clientid));
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "get");
		}
	}

	/**
	 * This method returns a byte that indicate the state of the player from the
	 * 3 possible options: - STATE_NULL - STATE_LOGIN_COMPLETE -
	 * STATE_GAME_BEGIN
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 */
	public ClientState getRuntimeState(int clientid)
			throws NoSuchClientIDException {
		Log4J.startMethod(logger, "getRuntimeState");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				return entry.state;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "getRuntimeState");
		}
	}

	/**
	 * This method set the state of the player from the 3 possible options: -
	 * STATE_NULL - STATE_LOGIN_COMPLETE - STATE_GAME_BEGIN
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param newState
	 *            the new state to which we move.
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 */
	public ClientState changeRuntimeState(int clientid, ClientState newState)
			throws NoSuchClientIDException {
		Log4J.startMethod(logger, "changeRuntimeState");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));
				ClientState oldState = entry.state;

				entry.state = newState;
				return oldState;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "changeRuntimeState");
		}
	}

	/**
	 * This method returns the RPObject.ID of the object the player whose
	 * clientid is clientid owns.
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @return the RPObject.ID of the object that this player uses. *
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchCharacterException
	 *             if character is not found
	 * @throws NoSuchPlayerFoundException
	 *             if the player doesn't exist in database.
	 */
	public RPObject.ID getRPObjectID(int clientid)
			throws NoSuchClientIDException, NoSuchPlayerException,
			NoSuchCharacterException {
		Log4J.startMethod(logger, "getRPObjectID");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				return entry.characterid;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "getRPObjectID");
		}
	}

	/**
	 * This method set the RPObject.ID of the object the player whose clientid
	 * is clientid owns.
	 * 
	 * @param clientid
	 *            the runtime id of the player
	 * @param id
	 *            the RPObject.ID id of the player
	 * 
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 * @throws NoSuchCharacterException
	 *             if character is not found
	 * @throws NoSuchPlayerFoundException
	 *             if the player doesn't exist in database.
	 */
	public void setRPObjectID(int clientid, RPObject.ID id)
			throws NoSuchClientIDException, NoSuchPlayerException,
			NoSuchCharacterException {
		Log4J.startMethod(logger, "setRPObjectID");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				entry.characterid = id;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "setRPObjectID");
		}
	}

	/**
	 * This method returns the username of the player with runtime id equals to
	 * clientid.
	 * 
	 * @param clientid
	 *            the runtime id of the player *
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 */
	public String getUsername(int clientid) throws NoSuchClientIDException {
		Log4J.startMethod(logger, "getUsername");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				return entry.username;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "getUsername");
		}
	}

	/**
	 * The method returns the IP address of the player represented by clientid
	 * 
	 * @param clientid
	 *            the runtime id of the player *
	 * @throws NoSuchClientIDException
	 *             if clientid is not found
	 */
	public InetSocketAddress getInetSocketAddress(int clientid)
			throws NoSuchClientIDException {
		Log4J.startMethod(logger, "InetSocketAddress ");
		try {
			if (hasRuntimePlayer(clientid)) {
				RuntimePlayerEntry entry = (RuntimePlayerEntry) listPlayerEntries
						.get(new Integer(clientid));

				return entry.source;
			} else {
				logger.debug("No such RunTimePlayer(" + clientid + ")");
				throw new NoSuchClientIDException(clientid);
			}
		} finally {
			Log4J.finishMethod(logger, "InetSocketAddress ");
		}
	}
}
