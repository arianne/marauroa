/* $Id: IDatabase.java,v 1.12 2007/02/27 18:39:55 arianne_rpg Exp $ */
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
package marauroa.server.game.db;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;

import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.server.game.Statistics.Variables;
import marauroa.server.game.container.PlayerEntry;

/**
 * This interface exposes the methods a database implementation should have.
 * @author miguel
 *
 */
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
	 * Generates an unique player id.
	 * A pattern is a string that where special symbols will be replaced.
	 * Mainly we have:<ul>
	 * <li><b>@</b> that will be replaced by a random lowercase letter.
	 * <li><b>#</b> that will be replaced by a random lowercase number.
	 * </ul>
	 *
	 * @param transaction the database transaction.
	 * @param pattern the pattern to follow to genereate the player id
	 * @return the generated player id
	 */
	public String generatePlayer(JDBCTransaction transaction, String pattern) throws SQLException;

	/**
	 * Change the password of the associated username.
	 *
	 * @param transaction the database transaction.
	 * @param username the player's usernam
	 * @param password the new password
	 * @return true if the operation is sucessful.
	 * @throws SQLException
	 */
	public void changePassword(JDBCTransaction transaction, String username, String password) throws SQLException;

	/**
	 * Change the email address of the associated username
	 * @param transaction the database transaction.
	 * @param username the player username
	 * @param email the players new email address
	 * @return true if the operation is sucessful.
	 * @throws SQLException
	 */
	public void changeEmail(JDBCTransaction transaction, String username, String email) throws SQLException;

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
	 * Stores all the objects tagged as storable into database and assign them to the zone
	 * we are storing.
	 *
	 * @param transaction the database transaction
	 * @param zone the zone we want to store.
	 * @throws IOException
	 * @throws IOException
	 * @throws SQLException
	 */
	public void storeRPZone(JDBCTransaction transaction, IRPZone zone) throws IOException, SQLException;

	/**
	 * Returns a list of all the stored objects into a zone.
	 * Zone must be correctly created before trying to load it.
	 * @param transaction the database transaction
	 * @param zone the zone to where we add the loaded objects
	 * @throws SQLException
	 * @throws IOException
	 */
	public void loadRPZone(JDBCTransaction transaction, IRPZone zone) throws SQLException, IOException;

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
	 * @param transaction the database transaction
	 * @param username the username to get login events from
	 * @param events the amount of events to return
	 * @return a List of LoginEvent objects
	 * @throws SQLException
	 */
	public List<String> getLoginEvents(JDBCTransaction transaction, String username, int events) throws SQLException;

	/** This method returns a transaction to the database. */
	// TODO: Generalize it.
	public JDBCTransaction getTransaction();
}
