/* $Id: PacketValidator.java,v 1.8 2006/08/20 15:40:13 wikipedian Exp $ */
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
package marauroa.server.net;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.server.game.IPlayerDatabase;
import marauroa.server.game.JDBCPlayerDatabase;
import marauroa.server.game.JDBCTransaction;

import org.apache.log4j.Logger;

/**
 * The PacketValidator validates the ariving packets, (currently it can only
 * check if the address is banned, may be it will check more later)
 * 
 */
public class PacketValidator {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(PacketValidator.class);

	private InetAddressMask[] banList;

	/* timestamp of last reload */
	private long lastLoadTS;

	/* */
	private long reloadAfter;

	/**
	 * Constructor that opens the socket on the marauroa_PORT and start the
	 * thread to recieve new messages from the network.
	 */
	public PacketValidator() {
		Log4J.startMethod(logger, "PacketValidator");

		/* at most each 5 minutes */
		reloadAfter = 5 * 60 * 1000;
		/* read ban list from configuration */
		loadBannedIPNetworkListFromDB();
		Log4J.finishMethod(logger, "PacketValidator");
	}

	/**
	 * Is the source ip-address banned?
	 * 
	 * @param packet
	 *            the DatagramPacket received
	 * @return true if the source ip is banned
	 */
	public boolean checkBanned(DatagramPacket packet) {
		boolean banned = false;
		banned = checkBanned(packet.getAddress());
		return (banned);
	}

	/**
	 * Is the source ip-address banned?
	 * 
	 * @param address
	 *            the InetAddress of the source
	 * @return true if the source ip is banned
	 */
	public synchronized boolean checkBanned(InetAddress address) {
		boolean banned = false;
		Log4J.startMethod(logger, "checkBanned");
		checkReload();
		if (banList != null) {
			for (int i = 0; i < banList.length; i++) {
				InetAddressMask iam = banList[i];
				if (iam.matches(address)) {
					logger.debug("Address " + address + " is banned by " + iam);
					banned = true;
					break;
				}
			}
		}

		Log4J.finishMethod(logger, "checkBanned");
		return banned;
	}

	/** loads and initializes the ban list from a database */
	public synchronized void loadBannedIPNetworkListFromDB() {
		Log4J.startMethod(logger, "loadBannedIPNetworkListFromDB");
		try {
			IPlayerDatabase db = JDBCPlayerDatabase.getDatabase();

			/* read ban list from DB */
			Connection connection = ((JDBCTransaction) db.getTransaction())
					.getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt
					.executeQuery("select address,mask from banlist");
			banList = null;
			List<InetAddressMask> ban_list_tmp = new ArrayList<InetAddressMask>();
			while (rs.next()) {
				String address = rs.getString("address");
				String mask = rs.getString("mask");
				InetAddressMask iam = new InetAddressMask(address, mask);
				ban_list_tmp.add(iam);
			}

			if (ban_list_tmp.size() > 0) {
				banList = new InetAddressMask[ban_list_tmp.size()];
				banList = ban_list_tmp.toArray(banList);
			}

			// free database resources
			rs.close();
			stmt.close();

			logger.debug("loaded " + ban_list_tmp.size()
					+ " entries from ban table");
		} catch (SQLException sqle) {
			logger.error("cannot read banned networks database table", sqle);
		} catch (Exception e) {
			logger.error("error while reading banned networks", e);
		}

		lastLoadTS = System.currentTimeMillis();
		Log4J.finishMethod(logger, "loadBannedIPNetworkListFromDB");
	}

	/**
	 * checks if reload is necessary and performs it
	 */
	public synchronized void checkReload() {
		Log4J.startMethod(logger, "checkReload");
		if (System.currentTimeMillis() - lastLoadTS >= reloadAfter) {
			loadBannedIPNetworkListFromDB();
		}
		Log4J.finishMethod(logger, "checkReload");
	}
}
