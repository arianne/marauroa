/* $Id: IDatabase.java,v 1.1 2007/02/03 17:33:42 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.db.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;

import marauroa.common.game.RPObject;
import marauroa.server.game.Statistics.Variables;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.db.JDBCTransaction;
import marauroa.server.game.db.nio.JDBCDatabase.LoginEvent;

public interface IDatabase {
	/**
	 * Adds this player to database with username, password and email.
	 *  
	 * @param transaction the database transaction.
	 * @param username player's name
	 * @param password player's password
	 * @param email player's email
	 * @throws SQLException 
	 */
	public void addPlayer(JDBCTransaction transaction, String username,
			byte[] password, String email) throws SQLException;

	/**
	 * Removes a player, its characters and the avatars that represent it from database.
	 * @param transaction the database transaction
	 * @param username the player to remove.
	 * @return true if success or false otherwise.
	 * @throws SQLException 
	 */
	public boolean removePlayer(JDBCTransaction transaction, String username) throws SQLException;

	/**
	 * Query database to look for a player.
	 * @param transaction the database transaction
	 * @param username the player to look for
	 * @return true if player is found or false if it is not.
	 * @throws SQLException if there is a database problem.
	 */
	public boolean hasPlayer(JDBCTransaction transaction, String username)
			throws SQLException;

	/**
	 * Set the status account of a player. On account creating it is set to active.
	 * We may want to change an account to:
	 *   inactive
	 *   active
	 *   banned
	 *   
	 * @param transaction the player database
	 * @param username player username
	 * @param status status we are going to set
	 * @throws SQLException if there is a database problem
	 */
	public void setAccountStatus(JDBCTransaction transaction, String username,
			String status) throws SQLException;

	/**
	 * Returns the account status of the given player.
	 * 
	 * @param transaction the player database
	 * @param username player username
	 * @return the status of the player
	 * @throws SQLException if there is any database problem
	 */
	public String getAccountStatus(JDBCTransaction transaction, String username) throws SQLException;

	/**
	 * Adds a character to database for a player.
	 * 
	 * @param transaction the database transaction
	 * @param username player's username
	 * @param character character's name
	 * @param player player RPObject
	 * @throws SQLException if there is any kind of database problem.
	 * @throws IOException if RPObject can NOT be serialized
	 */
	public void addCharacter(JDBCTransaction transaction, String username,
			String character, RPObject player) throws SQLException, IOException;

	/**
	 * Removes a character of a player. This method also remove the associated RPObject.
	 * 
	 * @param transaction the database transaction
	 * @param username player's username
	 * @param character character name
	 * @return true if it is removed or false otherwise
	 * @throws SQLException if there is any database problem
	 */
	public boolean removeCharacter(JDBCTransaction transaction,
			String username, String character) throws SQLException;

	/**
	 * This method returns true if the player has that character or false if it hasn't
	 * 
	 * @param transaction the database transaction
	 * @param username player's name
	 * @param character character's name
	 * @return true if character is found or false otherwise
	 * @throws SQLException if there is any problem with database
	 */
	public boolean hasCharacter(JDBCTransaction transaction, String username,
			String character) throws SQLException;

	/**
	 * Returns the list of characters this player owns.
	 * 
	 * @param transaction the database transaction
	 * @param username player's username
	 * @return the list of characters.
	 * @throws SQLException if there is any database problem.
	 */
	public List<String> getCharacters(JDBCTransaction transaction,
			String username) throws SQLException;

	
	/**
	 * This method load from database the character's avatar asociated to this character.
	 * 
	 * @param transaction the database transaction
	 * @param username the player's username
	 * @param character the player's character name
	 * @return The loaded RPObject
	 * @throws SQLException if there is any problem at database
	 * @throws IOException if player can NOT be serialized
	 */
	public RPObject loadCharacter(JDBCTransaction transaction, String username, String character) throws SQLException, IOException;

	/**
	 * This method stores a character's avatar at database and update the link with Character table.
	 * 
	 * @param transaction the database transaction
	 * @param username the player's username
	 * @param character the player's character name
	 * @param player the RPObject itself.
	 * @throws SQLException if there is any problem at database.
	 * @throws IOException if player can NOT be serialized
	 */
	public void storeCharacter(JDBCTransaction transaction, String username, String character, RPObject player) throws SQLException, IOException;

	/**
	 * This method adds a logging game event to database, so later you can query it to get information
	 * about how game is evolving.  
	 * @param trans the database transaction
	 * @param source the source of the event
	 * @param event the event itself
	 * @param params any params the event may need.
	 */
	public void addGameEvent(JDBCTransaction trans, String source, String event, String... params);

	/**
	 * This method inserts in database a statistics events.
	 * @param trans the database transaction
	 * @param var the statistics variables. @See marauroa.server.game.Statistics.Variables
	 */
	public void addStatisticsEvent(JDBCTransaction trans, Variables var);


	/**
	 * This method returns true if the player match in database matches the given password and the account is active.
	 * @param transaction the database transaction
	 * @param informations login informations created by GameServerManager
	 * @return true if account information is correct or false otherwise
	 * @throws SQLException if there is any database problem.
	 */
	public boolean verify(JDBCTransaction transaction,
			PlayerEntry.SecuredLoginInfo informations) throws SQLException;

	/**
	 * This methods adds an entry to the login events table so player can know who has tried to access
	 * his account and if he has been sucessful or not.
	 * @param transaction the database transaction
	 * @param username the username who tried to login
	 * @param source the IP address from which we tried
	 * @param correctLogin the result of the action
	 * @throws SQLException if there is any problem such as a player that doesn't exist
	 */
	public void addLoginEvent(JDBCTransaction transaction,
			String username, InetSocketAddress source, boolean correctLogin)
			throws SQLException;

	/**
	 * This method returns a list of the login events and its result. 
	 * 
	 * @param transaction
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	public List<LoginEvent> getLoginEvents(JDBCTransaction transaction, String username) throws SQLException;

	/** This method returns a transaction to the database. */
	// TODO: Generalize it.
	public JDBCTransaction getTransaction();
}