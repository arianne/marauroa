/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.validator;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import marauroa.common.Log4J;
import marauroa.common.net.Channel;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.dbcommand.LoadBanListCommand;
import marauroa.server.game.messagehandler.DelayedEventHandler;
import marauroa.server.game.rp.RPServerManager;



/**
 * The ConnectionValidator validates the ariving connections, currently it can
 * only check if the address is banned.
 * <p>
 * There are two types of bans:
 * <ul>
 * <li>Permanent bans<br>
 * That are stored at database and that we offer no interface.
 * <li>Temportal bans<br>
 * That are not stored but that has a interface for adding, removing and
 * querying bans.
 * </ul>
 */
public class ConnectionValidator implements Iterable<InetAddressMask>, DelayedEventHandler {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(ConnectionValidator.class);

	/** Permanent bans are stored inside the database. */
	private final List<InetAddressMask> permanentBans;

	/**
	 * Temporal bans are added using the API and are lost on each server reset.
	 * Consider using Database for a permanent ban
	 */
	List<InetAddressMask> temporalBans;

	/** A timer to remove ban when it is done. */
	private final Timer timer;

	/* timestamp of last reload */
	private long lastLoadTS;

	/* How often do we reload ban information from database? Each 5 minutes */
	private final static long RELOAD_PERMANENT_BANS = 5 * 60 * 1000;

	/**
	 * Constructor. It loads permanent bans from database.
	 */
	public ConnectionValidator() {
		permanentBans = Collections.synchronizedList(new LinkedList<InetAddressMask>());
		temporalBans = Collections.synchronizedList(new LinkedList<InetAddressMask>());
		timer = new Timer();
	}

	/**
	 * Request connection validator to stop all the activity, and stop checking
	 * if any ban needs to be removed.
	 *
	 */
	public void finish() {
		timer.cancel();
	}

	/**
	 * This class extend timer task to remove bans when the time is reached.
	 *
	 * @author miguel
	 *
	 */
	private class RemoveBan extends TimerTask {

		private final InetAddressMask mask;

		/**
		 * Constructor
		 *
		 * @param mask
		 */
		public RemoveBan(InetAddressMask mask) {
			this.mask = mask;
		}

		@Override
		public void run() {
			temporalBans.remove(mask);
		}
	}

	/**
	 * Adds a ban just for this ip address for i seconds.
	 *
	 * @param channel
	 *            the channel whose IP we are going to ban.
	 * @param time
	 *            how many seconds to ban.
	 */
	public void addBan(Channel channel, int time) {
		addBan(channel.getInetAddress().getHostAddress(), "255.255.255.255", time);

	}

	/**
	 * This adds a temporal ban.
	 *
	 * @param address
	 *            the address to ban
	 * @param mask
	 *            mask to apply to the address
	 * @param time
	 *            how many seconds should the ban apply.
	 */
	public void addBan(String address, String mask, long time) {
		InetAddressMask inetmask = new InetAddressMask(address, mask);

		timer.schedule(new RemoveBan(inetmask), time);
		temporalBans.add(inetmask);
	}

	/**
	 * Removes one of the added temporal bans.
	 *
	 * @param address
	 *            the address to remove
	 * @param mask
	 *            the mask used.
	 * @return true if it has been removed.
	 */
	public boolean removeBan(String address, String mask) {
		return temporalBans.remove(new InetAddressMask(address, mask));
	}

	/**
	 * Returns an iterator over the temporal bans. To access permanent bans, use
	 * database facility.
	 */
	public Iterator<InetAddressMask> iterator() {
		return temporalBans.iterator();
	}

	/**
	 * Is the source ip-address banned?
	 *
	 * @param address
	 *            the InetAddress of the source
	 * @return true if the source ip is banned
	 */
	public synchronized boolean checkBanned(InetAddress address) {
		checkReload();

		for (InetAddressMask iam : temporalBans) {
			if (iam.matches(address)) {
				logger.debug("Address " + address + " is banned by " + iam);
				return true;
			}
		}

		for (InetAddressMask iam : permanentBans) {
			if (iam.matches(address)) {
				logger.debug("Address " + address + " is permanently banned by " + iam);
				return true;
			}
		}

		return false;
	}

	public void handleDelayedEvent(RPServerManager rpMan, Object data) {
		LoadBanListCommand cmd = (LoadBanListCommand) data;
		permanentBans.clear();
		permanentBans.addAll(cmd.getPermanentBans());
		lastLoadTS = System.currentTimeMillis();
	}

	/**
	 * checks if reload is necessary and performs it
	 */
	public synchronized void checkReload() {
		if (System.currentTimeMillis() - lastLoadTS >= RELOAD_PERMANENT_BANS) {
			DBCommandQueue.get().enqueue(new LoadBanListCommand(this));
		}
	}

}
