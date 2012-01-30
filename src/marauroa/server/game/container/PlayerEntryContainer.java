/***************************************************************************
 *                   (C) Copyright 2007-2011 - Marauroa                    *
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

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.server.RWLock;
import marauroa.server.game.Statistics;

/**
 * This is a helper class to sort and access PlayerEntry in a controlled way.
 *
 * This class implements the singleton pattern. PlayerContainer is the data
 * structure that contains all of the information about the players while the
 * game is running.
 * <p>
 * It consists of a list of PlayerEntry objects and is heavily linked with the
 * database, so we can hide its complexity to GameManager. By making
 * PlayerDatabase hidden by PlayerContainer we achieve the illusion that
 * managing the runtime behavior we modify automatically the permanent one.
 *
 * @author miguel
 *
 */
public class PlayerEntryContainer implements Iterable<PlayerEntry> {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(PlayerEntryContainer.class);

	/** A reader/writers lock for controlling the access */
	private RWLock lock;

	/** A random number generator instance. */
	private Random rand;

	/** This map store player entry for fast access using clientid */
	Map<Integer, PlayerEntry> clientidMap;

	private static PlayerEntryContainer playerEntryContainer;

	/** Statistics about actions runs */
	private Statistics stats = Statistics.getStatistics();

	/** Constructor */
	protected PlayerEntryContainer() {
		/* Initialize the random number generator */
		rand = new Random();
		rand.setSeed(new Date().getTime());

		lock = new RWLock();

		/* We initialize the list that will help us sort the player entries. */
		clientidMap = Collections.synchronizedMap(new HashMap<Integer, PlayerEntry>());
	}

	/**
	 * This method returns an instance of PlayerEntryContainer
	 *
	 * @return A shared instance of PlayerEntryContainer
	 */
	public static PlayerEntryContainer getContainer() {
		if (playerEntryContainer == null) {
			playerEntryContainer = new PlayerEntryContainer();
		}
		return playerEntryContainer;
	}

	/**
	 * This method returns an iterator over tha available player entry objects.
	 *
	 * @return the iterator
	 */
	public Iterator<PlayerEntry> iterator() {
		return new LinkedList<PlayerEntry>(clientidMap.values()).iterator();
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
	 * This method returns the size of the container.
	 *
	 * @return Container's size.
	 */
	public int size() {
		return clientidMap.size();
	}

	/**
	 * This method returns true if there is any PlayerEntry which has client id
	 * as clientid.
	 *
	 * @param clientid
	 *            the id of the PlayerEntry we are looking for
	 * @return true if it is found or false otherwise.
	 */
	public boolean has(int clientid) {
		return clientidMap.containsKey(clientid);
	}

	/**
	 * This method returns the PlayerEntry whose client id is clientid or null
	 * otherwise.
	 *
	 * @param clientid
	 *            the id of the PlayerEntry we are looking for
	 * @return the PlayerEntry if is it found or null otherwise
	 */
	public PlayerEntry get(int clientid) {
		return clientidMap.get(clientid);
	}

	/**
	 * This method returns the entry that has been associated with this
	 * SocketChannel, or null if it does not exists.
	 *
	 * @param channel
	 *            the socket channel to check
	 * @return the PlayerEntry or null if it is not found.
	 */
	public PlayerEntry get(SocketChannel channel) {
		synchronized (clientidMap) {
			for (PlayerEntry entry : clientidMap.values()) {
				if (entry.channel == channel) {
					return entry;
				}
			}
		}

		return null;
	}

	/**
	 * This method returns the first entry that has been associated to this player or
	 * null if it does not exists. Note: It is possible to login with the same account
	 * and different character multiple times.
	 *
	 * @param username
	 *            the username to look for
	 * @return the PlayerEntry or null if it is not found
	 */
	public PlayerEntry get(String username) {
		synchronized (clientidMap) {
			for (PlayerEntry entry : clientidMap.values()) {
				/*
				 * NOTE: Bug fix: We use ignore case to detect already logged
				 * players better.
				 */
				if (username.equalsIgnoreCase(entry.username)) {
					return entry;
				}
			}
		}
		return null;
	}

	/**
	 * This method returns the old entry that has been associated to this player or
	 * null if there is no old entry
	 *
	 * @param oldEntry
	 *            old entry with username and password
	 * @return the PlayerEntry or null if there is none
	 */
	public PlayerEntry getOldEntry(PlayerEntry oldEntry) {
		synchronized (clientidMap) {
			for (PlayerEntry entry : clientidMap.values()) {

				if (oldEntry.character.equalsIgnoreCase(entry.character)
					&& entry != oldEntry) {
					return entry;
				}
			}
		}
		return null;
	}

	/**
	 * This method returns the entry that has been associated to this player or
	 * null if it does not exists.
	 *
	 * @param object
	 *            the RPObject we have to look for
	 * @return the PlayerEntry or null if it is not found
	 */
	public PlayerEntry get(RPObject object) {
		synchronized (clientidMap) {
			for (PlayerEntry entry : clientidMap.values()) {
				/*
				 * We want really to do a fast comparasion
				 */
				if (entry.object == object) {
					return entry;
				}
			}
		}
		return null;
	}

	/**
	 * This method removed a player entry from the container and return it or
	 * null if the entry does not exist.
	 *
	 * @param clientid
	 *            the clientid we want its Player entry to remove.
	 * @return the player entry or null if it has not been found.
	 */
	public PlayerEntry remove(int clientid) {
		return clientidMap.remove(clientid);

	}

	/**
	 * Add a new Player entry to the container. This method assigns
	 * automatically a random clientid to this player entry.
	 *
	 * @param channel
	 *            the socket channel associated with the client
	 * @return client id resulting
	 */
	public PlayerEntry add(SocketChannel channel) {
		/* We create an entry */
		PlayerEntry entry = new PlayerEntry(channel);
		entry.clientid = generateClientID();

		/* Finally adds it to map */
		clientidMap.put(entry.clientid, entry);

		return entry;
	}

	private int generateClientID() {
		int clientid = rand.nextInt();

		synchronized (clientidMap) {
			while (has(clientid) && clientid > 0) {
				clientid = rand.nextInt();
			}
		}

		return clientid;
	}

	/**
	 * Obtains an idle player entry from the container. We can define an idle
	 * player entry like a player that has connected to server and requested to
	 * login, but never actually completed login stage. This client is taking
	 * server resources but doing nothing useful at all.
	 *
	 * @return an idle PlayerEntry or null if not found
	 */
	public PlayerEntry getIdleEntry() {
		synchronized (clientidMap) {
			for (PlayerEntry entry : clientidMap.values()) {
				if (entry.isRemovable()) {
					return entry;
				}
			}
		}
		return null;
	}

	/**
	 * dumps the statistics
	 */
	public void dumpStatistics() {
		HashSet<InetAddress> addresses = new HashSet<InetAddress>();
		int counter = 0;
		synchronized (clientidMap) {
			for (PlayerEntry entry : clientidMap.values()) {
				if (entry.state == ClientState.GAME_BEGIN) {
					addresses.add(entry.getAddress());
					counter++;
				}
			}
		}
		logger.debug("PlayerEntryContainer size: " + counter);
		stats.set("Players online", counter);
		stats.set("Ips online", addresses.size());
	}	

	/**
	 * a string representation useful for debugging.
	 *
	 * @return String
	 */
	@Override
	public String toString() {
		return "PlayerEntryContainer [clientidMap=" + clientidMap + "]";
	}


}
