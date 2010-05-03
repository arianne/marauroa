/* $Id: LoginSeedDAO.java,v 1.1 2010/05/03 21:20:48 nhnb Exp $ */
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
import java.util.HashMap;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;

/**
 * data access object for login seeds
 *
 * @author hendrik
 */
public class LoginSeedDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(LoginSeedDAO.class);

	/**
	 * Creates a new LoginSeedDAO
	 */
	protected LoginSeedDAO() {
		// hide constructor as this class should only be instantiated by DAORegister
	}


	/**
	 * checks if the ip-address is temporary blocked because of too many failed login attempts.
	 * Blocking ip-addresses is not related to banning ip-addresses.
	 *
	 * @param username username
	 * @param seed seed
	 * @return <code>true</code>, if this seed is already authenticated;
	 *    <code>false<code>, if the seed exists but is not authenticated
	 *    <code>null</code>, if the seed does not exist at all
	 * @throws SQLException in case of an database error
	 */
	public Boolean verifySeed(DBTransaction transaction, String username, String seed) throws SQLException {
		String query = "SELECT complete FROM account, loginseed WHERE account.id=loginseed.player_id AND loginseed.seed='[seed]' AND account.username='[username]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("seed", seed);
		params.put("username", username);
		logger.debug("verifySeed is executing query " + query);
		ResultSet resultSet = transaction.query(query, params);

		if (!resultSet.next()) {
			return null;
		}
		return resultSet.getBoolean("complete");
	}


	/**
	 * checks if the ip-address is temporary blocked because of too many failed login attempts.
	 * Blocking ip-addresses is not related to banning ip-addresses.
	 *
	 * @param username username
	 * @param seed seed
	 * @return <code>true</code>, if this seed is already authenticated;
	 *    <code>false<code>, if the seed exists but is not authenticated
	 *    <code>null</code>, if the seed does not exist at all
	 * @throws SQLException in case of an database error
	 */
	public Boolean verifySeed(String username, String seed) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			return verifySeed(transaction, username, seed);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

}
