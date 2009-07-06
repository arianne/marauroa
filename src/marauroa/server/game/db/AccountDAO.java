package marauroa.server.game.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.net.validator.InetAddressMask;

public class AccountDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(AccountDAO.class);

	public void addPlayer(DBTransaction transaction, String username, byte[] password, String email)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(email)) {
				throw new SQLException("Invalid string username=(" + username + ") email=(" + email
				        + ")");
			}

			String query = "insert into account(username, password, email, timedate, status)"
				+ " values('[username','[password]', '[email]', NULL, DEFAULT)";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			params.put("password", Hash.toHexString(password));
			params.put("email", email);
			logger.debug("addPlayer is using query: " + query);

			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't add player \"" + username + "\" to database", e);
			throw e;
		}
	}

	public String generatePlayer(DBTransaction transaction, String pattern) throws SQLException {
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

	public void changePassword(DBTransaction transaction, String username, String password)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			int id = getDatabasePlayerId(transaction, username);

			byte[] hashedPassword = Hash.hash(password);

			String query = "update account set password='[password]' where id=[player_id]";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", id);
			params.put("password", Hash.toHexString(hashedPassword));
			logger.debug("changePassword is using query: " + query);

			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't update password for player \"" + username + "\"", e);
			throw e;
		}
	}
	
	public boolean hasPlayer(DBTransaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			String query = "select count(*) as amount from  account where username like '[username]'";
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

	public void setAccountStatus(DBTransaction transaction, String username, String status)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(status)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			String query = "update account set status='[status]' where username like '[username]'";
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

	public String getAccountStatus(DBTransaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			String query = "select status from account where username like '[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);

			logger.debug("getAccountStatus is executing query " + query);

			ResultSet result = transaction.query(query, params);

			String status = null;
			if (result.next()) {
				status = result.getString("status");
			}

			return status;
		} catch (SQLException e) {
			logger.error("Can't query player \"" + username + "\"", e);
			throw e;
		}
	}

	public String getEmail(DBTransaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			String query = "select email from account where username like '[username]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			logger.debug("getEmail is executing query " + query);

			ResultSet result = transaction.query(query, params);
			String email = null;
			if (result.next()) {
				email = result.getString("email");
			}

			return email;
		} catch (SQLException e) {
			logger.error("Can't query player \"" + username + "\"", e);
			throw e;
		}
	}
	
	public int getDatabasePlayerId(DBTransaction transaction, String username) throws SQLException {
		if (!StringChecker.validString(username)) {
			throw new SQLException("Invalid string username=(" + username + ")");
		}

		String query = "select id from account where username like '[username]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", username);
		logger.debug("getDatabasePlayerId is executing query " + query);

		ResultSet result = transaction.query(query, params);

		int id = -1;
		if (result.next()) {
			id = result.getInt("id");
		}

		return id;
	}

	public boolean isAccountBlocked(DBTransaction transaction, String username) throws SQLException {
		if (!StringChecker.validString(username)) {
			throw new SQLException("Invalid string username=(" + username + ")");
		}

		int id = getDatabasePlayerId(transaction, username);
		String query = "SELECT count(*) as amount FROM loginEvent where player_id=[player_id]"
		        + " and result=0 and (now()-timedate)<"
		        + TimeoutConf.FAILED_LOGIN_BLOCKTIME;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("player_id", id);

		int attemps = transaction.querySingleCellInt(query, params);
		return attemps > TimeoutConf.FAILED_LOGIN_ATTEMPS;
	}

	public boolean verify(DBTransaction transaction, PlayerEntry.SecuredLoginInfo informations)
	        throws SQLException {
		if (Hash.compare(Hash.hash(informations.clientNonce), informations.clientNonceHash) != 0) {
			logger.debug("Different hashs for client Nonce");
			return false;
		}

		byte[] b1 = informations.key.decodeByteArray(informations.password);
		byte[] b2 = Hash.xor(informations.clientNonce, informations.serverNonce);
		if (b2 == null) {
			logger.debug("B2 is null");
			return false;
		}

		byte[] password = Hash.xor(b1, b2);
		if (password == null) {
			logger.debug("Password is null");
			return false;
		}

		if (!StringChecker.validString(informations.username)) {
			throw new SQLException("Invalid string username=(" + informations.username + ")");
		}

		// check new Marauroa 2.0 password type
		String hexPassword = Hash.toHexString(password);
		boolean res = verifyUsingDB(transaction, informations.username, hexPassword);

		if (!res) {
			// compatiblity: check new Marauroa 1.0 password type
			hexPassword = Hash.toHexString(Hash.hash(password));
			res = verifyUsingDB(transaction, informations.username, hexPassword);
		}

		return res;
	}
	
	private boolean verifyUsingDB(DBTransaction transaction, String username, String hexPassword) throws SQLException {
		try {
			String query = "select username from account where username like "
				+ "'[username]' and password like '[password]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			params.put("password", hexPassword);
			logger.debug("verifyAccount is executing query " + query);
			ResultSet resultSet = transaction.query(query, params);
			if (resultSet.next()) {
				String userNameFromDB = resultSet.getString("username");
				if (!userNameFromDB.equals(username)) {
					logger.warn("Username \"" + username + "\" is not the same that stored username \"" + userNameFromDB + "\"");
				}
				return true;
			}
			return false;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		}
	}
	
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

		return permanentBans;
	}

	public boolean removePlayer(DBTransaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

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
}
