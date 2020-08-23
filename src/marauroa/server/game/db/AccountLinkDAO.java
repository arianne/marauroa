/***************************************************************************
 *                   (C) Copyright 2020-2020 - Marauroa                    *
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import marauroa.server.db.DBTransaction;
import marauroa.server.db.StringChecker;
import marauroa.server.game.container.SecuredLoginInfo;

/**
 * data access object for account links
 *
 * @author hendrik
 */
public class AccountLinkDAO {
	private static Logger logger = Logger.getLogger(AccountLinkDAO.class);

	/**
	 Creates a new AccountLinkDAO
	 */
	protected AccountLinkDAO() {
		// hide constructor as this class should only be instantiated by DAORegister
	}

	/**
	 * gets the id of the account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return id of account, or -1 if no such account exists
	 * @throws SQLException in case of an database error
	 */
	public int getAccountIdByLinkedUsername(DBTransaction transaction, String username) throws SQLException {
		String query = "select player_id from accountLink where username = '[username]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", username);

		ResultSet result = transaction.query(query, params);

		int id = -1;
		if (result.next()) {
			id = result.getInt("id");
		}
		result.close();

		return id;
	}



	/**
	 *  gets the id of the account
	 *
	 * @param transaction DBTransaction
	 * @param tokenType type of token
	 * @param secret a secret
	 * @return true on success, false if the account does not exists or the password does not match
	 * @throws SQLException in case of an database error
	 */
	public int getAccountIdByLinkedSecret(DBTransaction transaction, String tokenType, String secret) throws SQLException {
		if (secret == null || secret.trim().equals("") || tokenType == null) {
			return -1;
		}
		String query = "SELECT player_id FROM accountLink WHERE type='[type]' AND secret = '[secret]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", tokenType);
		params.put("secret", secret);

		ResultSet result = transaction.query(query, params);

		int id = -1;
		if (result.next()) {
			id = result.getInt("id");
		}
		result.close();

		return id;
	}

	/**
	 * verifies that a provided token is a known secret
	 *
	 * @param transaction DBTransaction
	 * @param info SecuredLoginInfo
	 * @return true, if a secret could be verified, false otherweise
	 * @throws SQLException in case of a database error
	 */
	public boolean verifyPermanentToken(DBTransaction transaction, SecuredLoginInfo info) throws SQLException {
		String secret = info.token;
		String tokenType = info.tokenType;
		if (secret == null || secret.trim().equals("") || tokenType == null) {
			return false;
		}

		String query = "SELECT account.username FROM account, accountLink WHERE account.id = accountLink.player_id AND accountLink.type='[type]' AND accountLink.secret = '[secret]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", tokenType);
		params.put("secret", secret);

		ResultSet result = transaction.query(query, params);
		if (!result.next()) {
			result.close();
			return false;
		}

		String username = result.getString("username");
		info.username = username;
		result.close();
		return true;
	}

	/**
	 * adds an accountLink. tokenType and either username or secret are required. 
	 *
	 * @param transaction DBTransaction
	 * @param accountId   account.id
	 * @param tokenType   token
	 * @param externalUsername username
	 * @param externalNickname nickname
	 * @param externalEmail email
	 * @param secret secret
	 * @return true if the accountLink was created successfully
	 * @throws SQLException
	 */
	public boolean addAccountLink(DBTransaction transaction, int accountId, String tokenType, String externalUsername, String externalNickname, String externalEmail, String secret) throws SQLException {
		String query = "SELECT id FROM accountLink WHERE type='[type]' AND (username='[username]' OR secret='[secret]')";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", tokenType);
		params.put("username", externalUsername);
		params.put("secret", secret);
		ResultSet result = transaction.query(query, params);
		if (result.next()) {
			logger.warn("Account link already exists: " + result.getInt("id"), new Throwable());
			result.close();
			return false;
		}
		result.close();
		
		String sql = "INSERT INTO accountLink(player_id, type, username, nickname, email, secret) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement stmt = transaction.prepareStatement(sql, null);
		stmt.setInt(1, Integer.valueOf(accountId));
		stmt.setString(2, tokenType);
		stmt.setString(3, StringChecker.trimEmptyToNull(externalUsername));
		stmt.setString(4, StringChecker.trimEmptyToNull(externalNickname));
		stmt.setString(5, StringChecker.trimEmptyToNull(externalEmail));
		stmt.setString(6, StringChecker.trimEmptyToNull(secret));

		boolean res = stmt.execute();
		stmt.close();
		return res;
	}

}
