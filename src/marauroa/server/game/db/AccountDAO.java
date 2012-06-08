/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.Sha512Crypt;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.StringChecker;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.container.PlayerEntry;

/**
 * data access object for accounts
 *
 * @author miguel, hendrik
 */
public class AccountDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(AccountDAO.class);

	/**
	 Creates a new AccountDAO
	 */
	protected AccountDAO() {
		// hide constructor as this class should only be instantiated by DAORegister
	}


	/**
	 * creates an account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param passwordHash password hash
	 * @param email email-address
	 * @throws SQLException in case of an database error
	 */
	public void addPlayer(DBTransaction transaction, String username, byte[] passwordHash, String email) throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(email)) {
				throw new SQLException("Invalid string username=(" + username + ") email=(" + email
				        + ")");
			}

			String query = "insert into account(username, password, email, status)"
				+ " values('[username]','[password]', '[email]', 'active')";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			try {
				if (Configuration.getConfiguration().get("password_hash", "sha512").equals("md5")) {
					params.put("password", Hash.toHexString(passwordHash));
				} else {
					String password = Configuration.getConfiguration().get("password_pepper", "") + Hash.toHexString(passwordHash);
					params.put("password", Sha512Crypt.Sha512_crypt(password, null, 0));
				}
			} catch (IOException e) {
				throw new SQLException(e);
			}
			params.put("email", email);
			logger.debug("addPlayer is using query: " + query);

			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't add player \"" + username + "\" to database", e);
			throw e;
		}
	}

	/**
	 * generates an account name based on the specified pattern (uses for automatic testing)
	 *
	 * @param pattern
	 * @return account name
	 */
	public String generatePlayer(String pattern) {
		int length = pattern.length();
		Random rand = new Random();
		StringBuffer os = new StringBuffer();

		for (int i = 0; i < length; i++) {
			char c = pattern.charAt(i);
			if (c == '#') {
				// Replaced the # with a number between 0 and 10.
				os.append(rand.nextInt(10));
			} else if (c == '@') {
				// Replaced @ with a lower case letter between a and z
				char character = (char) (rand.nextInt(26) + 97);
				os.append(character);
			} else {
				// if it isn't anyone of the above, just add the character.
				os.append(c);
			}
		}

		return os.toString();
	}

	/**
	 * changes the email-address
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param email new email-address
	 * @throws SQLException in case of an database error
	 */
	public void changeEmail(DBTransaction transaction, String username, String email)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(email)) {
				throw new SQLException("Invalid string username=(" + username + ") email=(" + email
				        + ")");
			}

			int id = getDatabasePlayerId(transaction, username);

			String query = "update account set email='[email]' where id=[player_id]";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", id);
			params.put("email", email);
			logger.debug("changePassword is using query: " + query);

			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't change email for player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * changes the password
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param password new password
	 * @throws SQLException in case of an database error
	 */
	public void changePassword(DBTransaction transaction, String username, String password)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}
			String query = "update account set password='[password]' where username='[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			try {
				if (Configuration.getConfiguration().get("password_hash", "sha512").equals("md5")) {
					params.put("password", Hash.toHexString(Hash.hash(password)));
				} else {
					String temp = Configuration.getConfiguration().get("password_pepper", "") + Hash.toHexString(Hash.hash(password));
					params.put("password", Sha512Crypt.Sha512_crypt(temp, null, 0));
				}
			} catch (IOException e) {
				throw new SQLException(e);
			}
			logger.debug("changePassword is using query: " + query);

			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't update password for player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * checks if this account exists
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return true, if the account exists; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasPlayer(DBTransaction transaction, String username) throws SQLException {
		try {
			String query = "select count(*) as amount from account where username = '[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			logger.debug("hasPlayer is using query: " + query);

			int count = transaction.querySingleCellInt(query, params);
			return count > 0;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * sets the status of the account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param status account status
	 * @throws SQLException in case of an database error
	 */
	public void setAccountStatus(DBTransaction transaction, String username, String status)
	        throws SQLException {
		try {
			String query = "update account set status='[status]' where username = '[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			params.put("status", status);
			logger.debug("setAccountStatus is executing query " + query);

			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't update account status of player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * gets the status of the account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return account status, or <code>null</code> if no such account exists
	 * @throws SQLException in case of an database error
	 */
	public String getAccountStatus(DBTransaction transaction, String username) throws SQLException {
		try {
			String query = "SELECT account.status As status, accountban.reason As reason FROM account LEFT JOIN accountban ON (account.id=accountban.player_id AND (accountban.expire > CURRENT_TIMESTAMP OR accountban.expire IS NULL)) WHERE username='[username]' order by ifnull(expire,'9999-12-31') desc limit 1 ";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);

			logger.debug("getAccountStatus is executing query " + query);

			ResultSet result = transaction.query(query, params);

			String status = null;
			if (result.next()) {
				status = result.getString("status");
				if (status.equals("active")) {
					if (result.getString("reason") != null) {
						status = "banned";
					}
				}
			}
			result.close();

			return status;
		} catch (SQLException e) {
			logger.error("Can't query player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * gets the ban message of the account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return ban message, or <code>null</code> if the account is not banned.
	 * @throws SQLException in case of an database error
	 */
	public String getAccountBanMessage(DBTransaction transaction, String username) throws SQLException {
		try {
			String query = "SELECT account.status As status, accountban.reason As reason, accountban.expire As expire FROM account LEFT JOIN accountban ON (account.id=accountban.player_id AND (accountban.expire > CURRENT_TIMESTAMP OR accountban.expire IS NULL)) WHERE username='[username]' order by ifnull(expire,'9999-12-31') desc limit 1 ";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);

			logger.debug("getAccountStatus is executing query " + query);

			ResultSet result = transaction.query(query, params);

			String res = null;
			if (result.next()) {
				String status = result.getString("status");
				String reason = result.getString("reason");
				String expire = result.getString("expire");
				if (reason != null) {
					if (expire != null) {
						res = "Your account is temporarily suspended until " + expire + " server time.\r\n";
					} else {
						res = "Your account is banned.\r\n";
					}
					res = res + "The reason given was: " + reason + "\r\n";
				} else if ("banned".equals(status)) {
					res = "Your account has been banned. Please contact support.\r\n";
				} else if ("inactive".equals(status)) {
					res = "Your account has been flagged as inactive. Please contact support\r\n.";
				} else if ("merged".equals(status)) {
					res = "Your account has been merged into another account.\nPlease login with that account or contact support.\r\n";
				}

				if (((reason != null) || (!"active".equals(status))) && (!"merged".equals(status))) {
					try {
						Configuration conf = Configuration.getConfiguration();
						if (conf.has("server_abuseContact")) {
							res += conf.get("server_abuseContact");
						}
					} catch (IOException e) {
						logger.error(e, e);
					}
				}
			}
			result.close();

			return res;
		} catch (SQLException e) {
			logger.error("Can't query player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * gets the email-address of the account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return email of account, or <code>null</code> if no such account exists
	 * @throws SQLException in case of an database error
	 */
	public String getEmail(DBTransaction transaction, String username) throws SQLException {
		try {
			String query = "select email from account where username = '[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			logger.debug("getEmail is executing query " + query);

			ResultSet result = transaction.query(query, params);
			String email = null;
			if (result.next()) {
				email = result.getString("email");
			}
			result.close();

			return email;
		} catch (SQLException e) {
			logger.error("Can't query player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * gets the id of the account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return id of account, or -1 if no such account exists
	 * @throws SQLException in case of an database error
	 */
	public int getDatabasePlayerId(DBTransaction transaction, String username) throws SQLException {
		String query = "select id from account where username = '[username]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", username);
		logger.debug("getDatabasePlayerId is executing query " + query);

		ResultSet result = transaction.query(query, params);

		int id = -1;
		if (result.next()) {
			id = result.getInt("id");
		}
		result.close();

		return id;
	}

	/**
	 * verifies username and password
	 *
	 * @param transaction DBTransaction
	 * @param informations login credentials
	 * @return true, on success; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean verify(DBTransaction transaction, PlayerEntry.SecuredLoginInfo informations)
	        throws SQLException {
		if (Hash.compare(Hash.hash(informations.clientNonce), informations.clientNonceHash) != 0) {
			logger.debug("Different hashs for client Nonce");
			return false;
		}

		// if a login seed was provided, check it
		if (informations.seed != null) {
			LoginSeedDAO loginSeedDAO = DAORegister.get().get(LoginSeedDAO.class);
			Boolean seedVerified = loginSeedDAO.verifySeed(transaction, informations.username, informations.seed);
			if (seedVerified == null) {
				informations.reason = MessageS2CLoginNACK.Reasons.SEED_WRONG;
				return false;
			}
			// the provided seed is valid, use it up
			loginSeedDAO.useSeed(informations.seed);
			if (seedVerified.booleanValue()) {
				// the seed was even pre authenticated, so we are done here
				return true;
			}
		}

		byte[] passwordHash = informations.getDecryptedPasswordHash();
		boolean res = verifyUsingDB(transaction, informations.username, passwordHash);
		return res;
	}

	/**
	 * verifies the account credentials using the database
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param hexPassword hashed password
	 * @return true on success, false if the account does not exists or the password does not match
	 * @throws SQLException in case of an database error
	 */
	private boolean verifyUsingDB(DBTransaction transaction, String username, byte[] password) throws SQLException {
		try {
			String query = "select username, password from account where username = '[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			logger.debug("verifyAccount is executing query " + query);
			ResultSet resultSet = transaction.query(query, params);
			if (!resultSet.next()) {
				resultSet.close();
				return false;
			}

			String userNameFromDB = resultSet.getString("username");
			if (!userNameFromDB.equalsIgnoreCase(username)) {
				logger.warn("Username \"" + username + "\" is not the same that stored username \"" + userNameFromDB + "\"");
				resultSet.close();
				return false;
			}
			String storedPassword = resultSet.getString("password");
			resultSet.close();

			boolean res = false;
			if (storedPassword.startsWith("$6$")) {
				String pepper = Configuration.getConfiguration().get("password_pepper", "");
				res = Sha512Crypt.verifyPassword(pepper + Hash.toHexString(password), storedPassword);
				if (!res) {
					res = Sha512Crypt.verifyPassword(pepper + Hash.toHexString(password), storedPassword);
				}
			} else {
				// check old Marauroa 2.0 and 3.0 password type
				String hexPassword = Hash.toHexString(password);
				res = hexPassword.equalsIgnoreCase(storedPassword);
				if (!res) {
					// check old Marauroa 1.0 password type
					hexPassword = Hash.toHexString(Hash.hash(password));
					res = hexPassword.equalsIgnoreCase(storedPassword);
				}
			}
			return res;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		} catch (IOException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			return false;
		}
	}


	/**
	 * deletes an account from the database
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return always true
	 * @throws SQLException in case of an database error
	 */
	public boolean removePlayer(DBTransaction transaction, String username) throws SQLException {
		try {
			/* We first remove any characters associated with this player. */
			CharacterDAO characterDAO = DAORegister.get().get(CharacterDAO.class);
			for (String character : characterDAO.getCharacters(transaction, username)) {
				characterDAO.removeCharacter(transaction, username, character);
			}

			String query = "delete from account where username='[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			logger.debug("removePlayer is using query: " + query);
			transaction.execute(query, params);

			return true;
		} catch (SQLException e) {
			logger.error("Can't remove player\"" + username + "\" from database", e);
			throw e;
		}
	}


	/**
	 * is account creation limit reached for recently created accounts
	 *
	 * @param transaction DBTransaction
	 * @param address ip-address
	 * @return true, if too many accounts have been created recently
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public boolean isAccountCreationLimitReached(DBTransaction transaction, String address) throws SQLException, IOException  {

		Configuration conf = Configuration.getConfiguration();
		String whiteList = "," + conf.get("ip_whitelist", "127.0.0.1") + ",";
		if (whiteList.indexOf("," + address + ",") > -1) {
			return false;
		}

		// count the number of recently created accounts from this ip-address
		String query = "SELECT count(DISTINCT username) "
			+ "FROM account, loginEvent "
			+ "WHERE account.id=loginEvent.player_id AND address='[address]' AND account.timedate>'[timestamp]'";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("address", address);

		// apply the time frame
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.SECOND, -1 * conf.getInt("account_creation_counting_time", TimeoutConf.ACCOUNT_CREATION_COUNTINGTIME));
		params.put("timestamp", new Timestamp(calendar.getTimeInMillis()).toString());

		// do the database query and evaluate the result
		int attemps = transaction.querySingleCellInt(query, params);
		return attemps > conf.getInt("account_creation_limit", TimeoutConf.ACCOUNT_CREATION_LIMIT);
	}

	/**
	 * adds a ban (which may be temporary)
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param reason Reason for the ban
	 * @param expire timestamp when this ban will expire, may be <code>null</code>
	 * @throws SQLException in case of an database error
	 */
	public void addBan(DBTransaction transaction, String username, String reason, Timestamp expire)
			throws SQLException {
		try {
			int player_id = getDatabasePlayerId(username);

			String expireStr = "'[expire]'";
			if (expire == null) {
				expireStr = "null";
			}

			String query = "insert into accountban(player_id, reason, expire)"
				+ " values('[player_id]','[reason]', " + expireStr + ")";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", player_id);
			params.put("reason", reason);
			params.put("expire", expire);
			logger.debug("addBan is using query: " + query);

			transaction.execute(query, params);

			if("null".equals(expireStr)) {
				setAccountStatus(transaction, username, "banned");
			}

		} catch (SQLException e) {
			logger.error("Can't insert ban for player \"" + username + "\" with expire of " +
					expire + " into database", e);
			throw e;
		}
	}

	/**
	 * creates an account
	 *
	 * @param username username
	 * @param password the hashed password's bytearray
	 * @param email email-address
	 * @throws SQLException in case of an database error
	 */
	public void addPlayer(String username, byte[] password, String email) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addPlayer(transaction, username, password, email);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * changes the email-address
	 *
	 * @param username username
	 * @param email new email-address
	 * @throws SQLException in case of an database error
	 */
	public void changeEmail(String username, String email) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			changeEmail(transaction, username, email);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * changes the password
	 *
	 * @param username username
	 * @param password new password
	 * @throws SQLException in case of an database error
	 */
	public void changePassword(String username, String password) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			changePassword(transaction, username, password);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * checks if this account exists
	 *
	 * @param username username
	 * @return true, if the account exists; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasPlayer(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = hasPlayer(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * sets the status of the account
	 *
	 * @param username username
	 * @param status account status
	 * @throws SQLException in case of an database error
	 */
	public void setAccountStatus(String username, String status) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			setAccountStatus(transaction, username, status);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * gets the status of the account
	 *
	 * @param username username
	 * @return account status, or <code>null</code> if no such account exists
	 * @throws SQLException in case of an database error
	 */
	public String getAccountStatus(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			String res = getAccountStatus(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * gets the ban message of an account
	 *
	 * @param username username
	 * @return account ban message, or <code>null</code> if this account is not banned
	 * @throws SQLException in case of an database error
	 */
	public String getAccountBanMessage(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			String res = getAccountBanMessage(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * gets the email-address of the account
	 *
	 * @param username username
	 * @return email of account, or <code>null</code> if no such account exists
	 * @throws SQLException in case of an database error
	 */
	public String getEmail(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			String res = getEmail(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * gets the id of the account
	 *
	 * @param username username
	 * @return id of account, or -1 if no such account exists
	 * @throws SQLException in case of an database error
	 */
	public int getDatabasePlayerId(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			int res = getDatabasePlayerId(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * verifies username and password
	 *
	 * @param informations login credentials
	 * @return true, on success; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean verify(PlayerEntry.SecuredLoginInfo informations) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = verify(transaction, informations);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * deletes an account from the database
	 *
	 * @param username username
	 * @return always true
	 * @throws SQLException in case of an database error
	 */
	public boolean removePlayer(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = removePlayer(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}


	/**
	 * is account creation limit reached for recently created accounts
	 *
	 * @param address ip-address
	 * @return true, if too many accounts have been created recently
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public boolean isAccountCreationLimitReached(String address) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = isAccountCreationLimitReached(transaction, address);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * adds a ban (which may be temporary)
	 *
	 * @param username username
	 * @param reason Reason for the ban
	 * @param expire timestamp when this ban will expire, may be <code>null</code>
	 * @throws SQLException in case of an database error
	 */
	public void addBan(String username, String reason, Timestamp expire)
			throws SQLException {

		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addBan(transaction, username, reason, expire);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}
}
