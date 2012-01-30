/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
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

import java.util.Date;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.game.RPObject;
import marauroa.common.net.NetConst;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.StringChecker;
import marauroa.server.db.TransactionPool;

/**
 * data access object for characters
 *
 * @author miguel, hendrik
 */
public class CharacterDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(CharacterDAO.class);

	/**
	 * Creates a new CharacterDAO
	 */
	protected CharacterDAO() {
		// hide constructor as this class should only be instantiated by DAORegister
	}

	/**
	 * creates a new character
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param character name of character
	 * @param player RPObject of the player
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public void addCharacter(DBTransaction transaction, String username, String character,
	        RPObject player) throws SQLException, IOException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			int object_id = DAORegister.get().get(RPObjectDAO.class).storeRPObject(transaction, player);

			String query = "insert into characters(player_id, charname, object_id, status)"
				+ "values([player_id], '[character]', [object_id], DEFAULT)";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(id));
			params.put("object_id", Integer.valueOf(object_id));
			params.put("character", character);
			logger.debug("addCharacter is executing query " + query);
			logger.debug("Character: " + player);

			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't add player \"" + username + "\" character \"" + character + "\" to database", e);
			throw e;
		} catch (IOException e) {
			logger.error("Can't add player \"" + username + "\" character \"" + character + "\" to database", e);
			throw e;
		}
	}

	/**
	 * deletes a character
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param character name of character
	 * @return true, if the character was deleted, false if it did not exist
	 * @throws SQLException in case of an database error
	 */
	public boolean removeCharacter(DBTransaction transaction, String username, String character)
	        throws SQLException {
		try {
			int player_id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			if (player_id < 0) {
				return false;
			}

			String query = "select object_id from characters where player_id=[player_id] and charname='[character]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(player_id));
			params.put("character", character);
			ResultSet result = transaction.query(query, params);

			if (result.next()) {
				int id = result.getInt("object_id");
				result.close();
				DAORegister.get().get(RPObjectDAO.class).removeRPObject(transaction, id);
			} else {
				result.close();
				return false;
			}

			query = "delete from characters where player_id=[player_id] and charname='[character]'";

			logger.debug("removeCharacter is using query: " + query);
			transaction.execute(query, params);

			return true;
		} catch (SQLException e) {
			logger.error("Can't remove player \"" + username + "\" character \"" + character + "\" from database", e);
			throw e;
		}
	}


	/**
	 * checks whether the specified character exists
	 *
	 * @param transaction DBTransaction
	 * @param character name of character
	 * @return true, if the character exists and belongs to the account; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasCharacter(DBTransaction transaction, String character) throws SQLException {
		try {
			String query = "SELECT charname FROM characters WHERE charname = '[character]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("character", character);
			logger.debug("hasCharacter is executing query " + query);

			ResultSet result = transaction.query(query, params);

			String actualName = null;
			if (result.next()) {
				actualName = result.getString("charname");
			}
			result.close();
			return (actualName != null) && (actualName.equalsIgnoreCase(character));
		} catch (SQLException e) {
			logger.error("Can't query for character \"" + character + "\"", e);
			throw e;
		}
	}
	


	/**
	 * checks whether the specified account owns the specified character
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param character name of character
	 * @return true, if the character exists and belongs to the account; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasCharacter(DBTransaction transaction, String username, String character) throws SQLException {
		try {
			String query = "SELECT charname, username FROM characters, account "
				+" WHERE account.username='[username]' AND account.id=characters.player_id AND charname='[character]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			params.put("character", character);
			logger.debug("hasCharacter is executing query " + query);

			ResultSet result = transaction.query(query, params);

			String actualName = null;
			String actualUsername = null;
			if (result.next()) {
				actualName = result.getString("charname");
				actualUsername = result.getString("username");
			}
			result.close();
			return (actualName != null) && (actualName.equalsIgnoreCase(character)) && (actualUsername != null) && (actualUsername.equalsIgnoreCase(username));
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\" character \"" + character + "\"", e);
			throw e;
		}
	}


	/**
	 * checks whether the specified account owns the specified character and it is active
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param character name of character
	 * @return true, if the character exists and belongs to the account; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasActiveCharacter(DBTransaction transaction, String username, String character) throws SQLException {
		try {
			String query = "SELECT charname, username FROM characters, account "
				+ " WHERE account.username='[username]' AND account.id=characters.player_id "
				+ " AND characters.status='active' AND charname='[character]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("username", username);
			params.put("character", character);
			logger.debug("hasCharacter is executing query " + query);

			ResultSet result = transaction.query(query, params);

			String actualName = null;
			String actualUsername = null;
			if (result.next()) {
				actualName = result.getString("charname");
				actualUsername = result.getString("username");
			}
			result.close();
			return (actualName != null) && (actualName.equalsIgnoreCase(character)) && (actualUsername != null) && (actualUsername.equalsIgnoreCase(username));
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\" character \"" + character + "\"", e);
			throw e;
		}
	}

	/**
	 * gets a list of characters for this account
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return list of characters
	 * @throws SQLException in case of an database error
	 */
	public List<String> getCharacters(DBTransaction transaction, String username) throws SQLException {
		try {
			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			if (id == -1) {
				/**
				 * This should never happen as we should check previously that
				 * player exists...
				 */
				throw new SQLException("Unable to find player(" + username + ")");
			}

			String query = "select charname from characters where player_id=[player_id] order by charname";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(id));
			logger.debug("getCharacterList is executing query " + query);

			ResultSet charactersSet = transaction.query(query, params);
			List<String> list = new LinkedList<String>();

			while (charactersSet.next()) {
				list.add(charactersSet.getString("charname"));
			}

			charactersSet.close();
			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
 	 * This method stores a character's avatar in the database and updates the link
 	 * with the Character table.
	 *
	 * @param transaction
	 *            the database transaction
	 * @param username
	 *            the player's username
	 * @param character
	 *            the player's character name
	 * @param player
	 *            the RPObject itself.
	 * @throws SQLException
	 *             if there is any problem at database.
	 * @throws IOException
	 */
	public void storeCharacter(DBTransaction transaction, String username, String character,
	        RPObject player) throws SQLException, IOException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			int objectid = DAORegister.get().get(RPObjectDAO.class).storeRPObject(transaction, player);
			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);

			String query = "update characters set object_id=[object_id] where charname='[character]' and player_id=[player_id]";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("object_id", Integer.valueOf(objectid));
			params.put("player_id", Integer.valueOf(id));
			params.put("character", character);
			
			logger.debug("storeCharacter is executing query " + query);
			logger.debug("Character: " + player);

			transaction.execute(query, params);
		} catch (SQLException sqle) {
			logger.warn("Error storing character: " + player, sqle);
			throw sqle;
		} catch (IOException e) {
			logger.warn("Error storing character: " + player, e);
			throw e;
		}
	}

	/**
 	 * This method loads the character's avatar associated with this
 	 * character from the database.
	 *
	 * @param transaction
	 *            the database transaction
	 * @param username
	 *            the player's username
	 * @param character
	 *            the player's character name
	 * @return The loaded RPObject
	 * @throws SQLException
	 *             if there is any problem at database
	 * @throws IOException
	 */
	public RPObject loadCharacter(DBTransaction transaction, String username, String character)
	        throws SQLException, IOException {
		try {

			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			String query = "select object_id from characters where charname='[character]' and player_id=[player_id]";
			logger.debug("loadCharacter is executing query " + query);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(id));
			params.put("character", character);
			
			ResultSet result = transaction.query(query, params);

			RPObject player = null;
			if (result.next()) {
				int objectid = result.getInt("object_id");
				player = DAORegister.get().get(RPObjectDAO.class).loadRPObject(transaction, objectid);
				logger.debug("Character: " + player);
			} else {
				logger.warn("No object for character " + character + " on account " + id + " username " + username);
			}

			result.close();
			return player;
		} catch (SQLException sqle) {
			logger.warn("Error loading character: " + character, sqle);
			throw sqle;
		} catch (IOException e) {
			logger.warn("Error loading character: " + character, e);
			throw e;
		}
	}

	/**
 	 * This method loads all the characters associated with this
 	 * username from the database.
	 *
	 * @param transaction
	 *            the database transaction
	 * @param username
	 *            the player's username
	 * @return The loaded RPObject
	 * @throws SQLException
	 *             if there is any problem at database
	 * @throws IOException
	 *             if there is a problem reading the blob 
	 */
	private Map<String, RPObject> loadAllCharacters(DBTransaction transaction, String username, String condition)
	        throws SQLException, IOException {
		try {
			Map<String, RPObject> res = new LinkedHashMap<String, RPObject>();

			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			String query = "SELECT characters.charname As charname, rpobject.data As data, rpobject.protocol_version As protocol_version, rpobject.object_id As object_id from characters, rpobject where rpobject.object_id=characters.object_id AND player_id=[player_id]"
					+ condition + " ORDER BY characters.charname";
			logger.debug("loadAllCharacters is executing query " + query);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(id));
			
			ResultSet result = transaction.query(query, params);

			RPObject player = null;
			while (result.next()) {
				int objectid = result.getInt("object_id");
				String name = result.getString("charname");
				Blob data = result.getBlob("data");
				int protocolVersion = NetConst.FIRST_VERSION_WITH_MULTI_VERSION_SUPPORT - 1;
				Object temp = result.getObject("protocol_version");
				if (temp != null) {
					protocolVersion = ((Integer) temp).intValue(); 
				}
				RPObject rpobject = DAORegister.get().get(RPObjectDAO.class).readRPObject(objectid, data, protocolVersion, false);
				logger.debug("Character: " + player);
				res.put(name, rpobject);
			}

			result.close();
			return res;
		} catch (SQLException sqle) {
			logger.warn("Error loading characters for account: " + username, sqle);
			throw sqle;
		} catch (IOException e) {
			logger.warn("Error loading characters for account: " + username, e);
			throw e;
		}
	}

	/**
 	 * This method loads all the characters associated with this
 	 * username from the database.
	 *
	 * @param transaction
	 *            the database transaction
	 * @param username
	 *            the player's username
	 * @return The loaded RPObject
	 * @throws SQLException
	 *             if there is any problem at database
	 * @throws IOException
	 *             if there is a problem reading the blob 
	 */
	public Map<String, RPObject> loadAllCharacters(DBTransaction transaction, String username) throws SQLException, IOException {
		return loadAllCharacters(transaction, username, "");
	}


	/**
 	 * This method loads all active the characters associated with this
 	 * username from the database.
	 *
	 * @param transaction
	 *            the database transaction
	 * @param username
	 *            the player's username
	 * @return The loaded RPObject
	 * @throws SQLException
	 *             if there is any problem at database
	 * @throws IOException
	 *             if there is a problem reading the blob 
	 */
	public Map<String, RPObject> loadAllActiveCharacters(DBTransaction transaction, String username) throws SQLException, IOException {
		return loadAllCharacters(transaction, username, " AND characters.status='active' ");
	}


	/**
	 * gets the name of the account to which the specified character belongs.
	 *
	 * @param transaction the database transaction
	 * @param character name of character
	 * @return name of account, or <code>null<code> in case the character does not exist
	 * @throws SQLException if there is any problem at database
	 */
	public String getAccountName(DBTransaction transaction, String character) throws SQLException {
		String res = null;

		String query = "SELECT username FROM account, characters WHERE characters.charname='[charname]' AND characters.player_id=account.id";
		logger.debug("getAccountName is executing query " + query);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("charname", character);
		
		ResultSet result = transaction.query(query, params);
		if (result.next()) {
			res = result.getString("username");
		}
		result.close();

		return res;
	}


	/**
	 * gets the canonical spelling of the character name
	 *
	 * @param transaction the database transaction
	 * @param character name of character
	 * @return name of character, or <code>null<code> in case the character does not exist
	 * @throws SQLException if there is any problem at database
	 */
	public String getCanonicalName(DBTransaction transaction, String character) throws SQLException {
		String res = null;

		String query = "SELECT charname FROM characters WHERE charname='[charname]'";
		logger.debug("getCanonicalName is executing query " + query);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("charname", character);
		
		ResultSet result = transaction.query(query, params);
		if (result.next()) {
			res = result.getString("charname");
		}
		result.close();

		return res;
	}


	/**
	 * is the character creation limit reached?
	 *
	 * @param transaction the database transaction
	 * @param username username of account
	 * @param address  ip-address of client
	 * @return true if too many characters have been created
	 * @throws IOException in case of an input output error
	 * @throws SQLException if there is any problem at database
	 */
	public boolean isCharacterCreationLimitReached(DBTransaction transaction,
			String username, String address) throws IOException, SQLException {

		Configuration conf = Configuration.getConfiguration();
		String whiteList = "," + conf.get("ip_whitelist", "127.0.0.1") + ",";
		if (whiteList.indexOf("," + address + ",") > -1) {
			return false;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("address", address);
		params.put("username", username);

		// apply the time frame
		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.SECOND, -1 * conf.getInt("character_creation_counting_time", TimeoutConf.CHARACTER_CREATION_COUNTINGTIME));
		params.put("timestamp", new Timestamp(calendar.getTimeInMillis()).toString());

		// count the number of recently created characters from this ip-address
		String query = "SELECT count(DISTINCT charname) "
			+ "FROM characters, account "
			+ "WHERE characters.player_id=account.id AND account.username='[username]' AND characters.timedate>'[timestamp]'";

		// do the database query and evaluate the result
		int attemps = transaction.querySingleCellInt(query, params);
		if (attemps > conf.getInt("character_creation_limit", TimeoutConf.CHARACTER_CREATION_LIMIT)) {
			return true;
		}

		// count the number of recently created characters from this ip-address
		query = "SELECT count(DISTINCT charname) "
			+ "FROM characters, account, loginEvent "
			+ "WHERE characters.player_id=account.id AND account.id=loginEvent.player_id AND address='[address]' AND characters.timedate>'[timestamp]'";

		attemps = transaction.querySingleCellInt(query, params);
		return attemps > conf.getInt("character_creation_limit", TimeoutConf.CHARACTER_CREATION_LIMIT);
	}
	
	/**
	 * Gets the date the character was registered
	 *
	 * @param transaction DBTransaction
	 * @param character name of character
	 * @return date of character creation or null if no character with that name exists
	 * @throws SQLException in case of an database error
	 */
	public Date getCreationDate(DBTransaction transaction, String character) throws SQLException {
		try {
			String query = "SELECT timedate FROM characters WHERE charname = '[character]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("character", character);
			logger.debug("getCreationDate is executing query " + query);

			ResultSet result = transaction.query(query, params);
			
			Date date = null;
			if (result.next()) {
				date = result.getDate("timedate");
			}
			result.close();
			return date;
		} catch (SQLException e) {
			logger.error("Can't query date for character \"" + character + "\"", e);
			throw e;
		}
	}


	/**
	 * creates a new character
	 *
	 * @param username username
	 * @param character name of character
	 * @param player RPObject of the player
	 * @throws IOException in case of an input/output error
	 * @throws SQLException in case of an database error
	 */
	public void addCharacter(String username, String character, RPObject player) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addCharacter(transaction, username, character, player);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * deletes a character
	 *
	 * @param username username
	 * @param character name of character
	 * @return true, if the character was deleted, false if it did not exist
	 * @throws SQLException in case of an database error
	 */
	public boolean removeCharacter(String username, String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = removeCharacter(transaction, username, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}


	/**
	 * checks whether the specified character exists
	 *
	 * @param character name of character
	 * @return true, if the character exists and belongs to the account; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasCharacter(String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = hasCharacter(transaction, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * checks whether the specified account owns the specified character
	 *
	 * @param username username
	 * @param character name of character
	 * @return true, if the character exists and belongs to the account; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasCharacter(String username, String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = hasCharacter(transaction, username, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}


	/**
	 * checks whether the specified account owns the specified character and it is active
	 *
	 * @param username username
	 * @param character name of character
	 * @return true, if the character exists and belongs to the account; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean hasActiveCharacter(String username, String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = hasActiveCharacter(transaction, username, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * gets a list of characters for this account
	 *
	 * @param username username
	 * @return list of characters
	 * @throws SQLException in case of an database error
	 */
	public List<String> getCharacters(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			List<String>  res = getCharacters(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
 	 * This method stores a character's avatar in the database and updates the link
 	 * with the Character table.
	 *
	 * @param username
	 *            the player's username
	 * @param character
	 *            the player's character name
	 * @param player
	 *            the RPObject itself.
	 * @throws SQLException
	 *             if there is any problem at database.
	 * @throws IOException
	 */
	public void storeCharacter(String username, String character, RPObject player) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			storeCharacter(transaction, username, character, player);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * This method loads the character's avatar associated with this
	 * character from the database.
	 *
	 * @param username
	 *            the player's username
	 * @param character
	 *            the player's character name
	 * @return The loaded RPObject
	 * @throws SQLException
	 *             if there is any problem at database
	 * @throws IOException
	 */
	public RPObject loadCharacter(String username, String character) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			RPObject res = loadCharacter(transaction, username, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
 	 * This method loads all the characters associated with this
 	 * username from the database.
	 *
	 * @param username
	 *            the player's username
	 * @return The loaded RPObject
	 * @throws SQLException
	 *             if there is any problem at database
	 * @throws IOException
	 *             if there is a problem reading the blob 
	 */
	public Map<String, RPObject> loadAllCharacters(String username) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			Map<String, RPObject> res = loadAllCharacters(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
 	 * This method loads all active the characters associated with this
 	 * username from the database.
	 *
	 * @param username
	 *            the player's username
	 * @return The loaded RPObject
	 * @throws SQLException
	 *             if there is any problem at database
	 * @throws IOException
	 *             if there is a problem reading the blob 
	 */
	public Map<String, RPObject> loadAllActiveCharacters(String username) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			Map<String, RPObject> res = loadAllActiveCharacters(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * gets the name of the account to which the specified character belongs.
	 *
	 * @param character name of character
	 * @return name of account, or <code>null<code> in case the character does not exist
	 * @throws SQLException if there is any problem at database
	 */
	public String getAccountName(String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			String res = getAccountName(transaction, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * gets the canonical spelling of the character name
	 *
	 * @param character name of character
	 * @return name of character, or <code>null<code> in case the character does not exist
	 * @throws SQLException if there is any problem at database
	 */
	public String getCanonicalName(String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			String res = getCanonicalName(transaction, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * is the character creation limit reached?
	 *
	 * @param username username of account
	 * @param address  ip-address of client
	 * @return true if too many characters have been created
	 * @throws IOException in case of an input output error
	 * @throws SQLException if there is any problem at database
	 */
	public boolean isCharacterCreationLimitReached(String username, String address) throws IOException, SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			return isCharacterCreationLimitReached(transaction, username, address);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * Gets the date the character was registered
	 *
	 * @param character name of character
	 * @return date of character creation or null if no character with that name exists
	 * @throws SQLException in case of an database error
	 */
	public Date getCreationDate(String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			Date res = getCreationDate(transaction, character);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}
	
}
