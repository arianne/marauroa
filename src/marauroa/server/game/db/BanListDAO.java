/* $Id: BanListDAO.java,v 1.4 2010/06/15 18:14:26 nhnb Exp $ */
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
package marauroa.server.game.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.net.validator.InetAddressMask;

/**
 * data access object the ban list
 */
public class BanListDAO {
	private static Logger logger = Log4J.getLogger(BanListDAO.class);

	/**
	 * gets a list of all banned ip-address ranges
	 *
	 * @param transaction DBTransaction
	 * @return list of banned ip-address ranges
	 * @throws SQLException in case of an database error
	 */
	public List<InetAddressMask> getBannedAddresses(DBTransaction transaction) throws SQLException {
		List<InetAddressMask> permanentBans = new LinkedList<InetAddressMask>();

		/* read ban list from DB */
		String query = "select address, mask from banlist";
		logger.debug("getBannedAddresses is executing query " + query);
		ResultSet resultSet = transaction.query(query, null);

		permanentBans.clear();
		while (resultSet.next()) {
			String address = resultSet.getString("address");
			String mask = resultSet.getString("mask");
			InetAddressMask iam = new InetAddressMask(address, mask);
			permanentBans.add(iam);
		}

		resultSet.close();
		return permanentBans;
	}

	/**
	 * gets a list of all banned ip-address ranges
	 *
	 * @return list of banned ip-address ranges
	 * @throws SQLException in case of an database error
	 */
	public List<InetAddressMask> getBannedAddresses() throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			List<InetAddressMask> res = getBannedAddresses(transaction);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

}
