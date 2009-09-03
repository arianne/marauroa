/* $Id: CharacterDAO.java,v 1.10 2009/09/03 06:48:51 nhnb Exp $ */
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

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
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

	public void addCharacter(DBTransaction transaction, String username, String character,
	        RPObject player) throws SQLException, IOException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			int object_id = DAORegister.get().get(RPObjectDAO.class).storeRPObject(transaction, player);

			String query = "insert into characters(player_id, charname, object_id)"
				+ "values([player_id], '[character]', [object_id])";
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

	public boolean removeCharacter(DBTransaction transaction, String username, String character)
	        throws SQLException {
		try {
			int player_id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);

			String query = "select object_id from characters where player_id=[player_id] and charname='[character]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(player_id));
			params.put("character", character);
			ResultSet result = transaction.query(query, params);

			if (result.next()) {
				int id = result.getInt("object_id");
				DAORegister.get().get(RPObjectDAO.class).removeRPObject(transaction, id);
			} else {
				result.close();
				throw new SQLException("Character (" + character
				        + ") without object: Database integrity error.");
			}

			query = "delete from characters where player_id=[player_id] and charname='[character]'";

			logger.debug("removeCharacter is using query: " + query);
			transaction.execute(query, params);

			result.close();
			return true;
		} catch (SQLException e) {
			logger.error("Can't remove player \"" + username + "\" character \"" + character + "\" from database", e);
			throw e;
		}
	}

	public boolean hasCharacter(DBTransaction transaction, String username, String character)
	        throws SQLException {
		try {

			/*
			 * NOTE: 
			 * Perse we have agreed that character name is unique per server, 
			 * so we check just characters ignoring username.
			 */
			String query = "select count(*) as amount from  characters where charname like '[character]'";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("character", character);
			logger.debug("hasCharacter is executing query " + query);

			int count = transaction.querySingleCellInt(query, params);
			return count > 0;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\" character \"" + character + "\"", e);
			throw e;
		}
	}

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

			String query = "select charname from characters where player_id=[player_id]";
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


	public void addCharacter(String username, String character, RPObject player) throws SQLException, IOException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		addCharacter(transaction, username, character, player);
		TransactionPool.get().commit(transaction);
	}

	public boolean removeCharacter(String username, String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		boolean res = removeCharacter(transaction, username, character);
		TransactionPool.get().commit(transaction);
		return res;
	}

	public boolean hasCharacter(String username, String character) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		boolean res = hasCharacter(transaction, username, character);
		TransactionPool.get().commit(transaction);
		return res;
	}

	public List<String> getCharacters(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		List<String>  res = getCharacters(transaction, username);
		TransactionPool.get().commit(transaction);
		return res;
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
		storeCharacter(transaction, username, character, player);
		TransactionPool.get().commit(transaction);
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
		RPObject res = loadCharacter(transaction, username, character);
		TransactionPool.get().commit(transaction);
		return res;
	}
}
