/* $Id: IPlayerDatabase.java,v 1.5 2007/01/08 19:26:13 arianne_rpg Exp $ */
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
package marauroa.server.game;

import java.net.InetSocketAddress;

import marauroa.common.game.RPObject;

/** The interface that all the databases marauroa use MUST implement. */
public interface IPlayerDatabase {
	/**
	 * Returns true if the strign is valid and doesn't contains any strange
	 * character like :, ;, ,, ", ', ...
	 */
	public boolean validString(String string);

	/**
	 * This method returns true if the informations match with any of the
	 * accounts in database or false if none of them match.
	 * 
	 * @param trans
	 *            is the Transaction to use.
	 * @param informations
	 *            containes the informations to decide if the login is correct.
	 * @return true if informations are correct, false otherwise.
	 */
	public boolean verifyAccount(
			Transaction trans,
			PlayerEntryContainer.RuntimePlayerEntry.SecuredLoginInfo informations)
			throws GenericDatabaseException;

	/**
	 * This method sets the account into one of the predefined states:
	 * active,inactive,banned don't forget to commit the changes.
	 * 
	 * @param username
	 *            is the name of the player
	 * @param status
	 *            the new status of the account
	 */
	public void setAccountStatus(Transaction trans, String username,
			String status) throws GenericDatabaseException;

	/**
	 * This method returns the number of Players that exist on database
	 * 
	 * @return the number of players that exist on database
	 */
	public int getPlayerCount(Transaction trans)
			throws GenericDatabaseException;

	/**
	 * This method add a Login event to the player
	 * 
	 * @param username
	 *            is the name of the player
	 * @param source
	 *            the IP address of the player
	 * @param correctLogin
	 *            true if the login has been correct.
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 */
	public void addLoginEvent(Transaction trans, String username,
			InetSocketAddress source, boolean correctLogin)
			throws PlayerNotFoundException, GenericDatabaseException;

	/**
	 * This method returns the list of Login events as a array of Strings
	 * 
	 * @param username
	 *            is the name of the player
	 * @return an array of String containing the login events.
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 */
	public String[] getLoginEvent(Transaction trans, String username)
			throws PlayerNotFoundException, GenericDatabaseException;

	/**
	 * This method returns the lis of character that the player pointed by
	 * username has.
	 * 
	 * @param username
	 *            the name of the player from which we are requesting the list
	 *            of characters.
	 * @return an array of String with the characters
	 * @exception PlayerNotFoundException
	 *                if that player does not exists.
	 */
	public String[] getCharactersList(Transaction trans, String username)
			throws PlayerNotFoundException, GenericDatabaseException;

	/**
	 * This method is the opposite of getRPObject, and store in Database the
	 * object for an existing player and character. The difference between
	 * setRPObject and addCharacter are that setRPObject update it while
	 * addCharacter add it to database and fails if it already exists .
	 * 
	 * @param username
	 *            is the name of the player
	 * @param character
	 *            is the name of the character that the username player wants to
	 *            add.
	 * @param object
	 *            is the RPObject that represent this character in game.
	 * 
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 * @exception CharacterNotFoundException
	 *                if the player-character doesn't exist in database.
	 * @exception GenericDatabaseException
	 *                if the character doesn't exist or it is not owned by the
	 *                player.
	 */
	public void setRPObject(Transaction trans, String username,
			String character, RPObject object) throws PlayerNotFoundException,
			CharacterNotFoundException, GenericDatabaseException;

	/**
	 * This method retrieves from Database the object for an existing player and
	 * character.
	 * 
	 * @param username
	 *            is the name of the player
	 * @param character
	 *            is the name of the character that the username player wants to
	 *            add.
	 * @return a RPObject that is the RPObject that represent this character in
	 *         game.
	 * 
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 * @exception CharacterNotFoundException
	 *                if the player-character doesn't exist in database.
	 * @exception GenericDatabaseException
	 *                if the character doesn't exist or it is not owned by the
	 *                player.
	 */
	public RPObject getRPObject(Transaction trans, String username,
			String character) throws PlayerNotFoundException,
			CharacterNotFoundException, GenericDatabaseException;

	/**
	 * This method returns true if the database has the player pointed by
	 * username
	 * 
	 * @param username
	 *            the name of the player we are asking if it exists.
	 * @return true if player exists or false otherwise.
	 */
	public boolean hasPlayer(Transaction trans, String username)
			throws GenericDatabaseException;

	/**
	 * This method add the player to database with username and password as
	 * identificator.
	 * 
	 * @param username
	 *            is the name of the player
	 * @param password
	 *            is a string used to verify access.
	 * @exception PlayerAlreadyAddedExceptio
	 *                if the player is already in database
	 */
	public void addPlayer(Transaction trans, String username, byte[] password,
			String email) throws PlayerAlreadyAddedException,
			GenericDatabaseException;

	/**
	 * This method remove the player with usernae from database.
	 * 
	 * @param username
	 *            is the name of the player
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 */
	public void removePlayer(Transaction trans, String username)
			throws PlayerNotFoundException, GenericDatabaseException;

	/**
	 * This method returns true if the player has that character or false if it
	 * hasn't
	 * 
	 * @param username
	 *            is the name of the player
	 * @param character
	 *            is the name of the character
	 * @return true if player has the character or false if it hasn't
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 */
	public boolean hasCharacter(Transaction trans, String username,
			String character) throws PlayerNotFoundException,
			GenericDatabaseException;

	/**
	 * This method add a character asociated to a player.
	 * 
	 * @param username
	 *            is the name of the player
	 * @param character
	 *            is the name of the character that the username player wants to
	 *            add.
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 * @exception CharacterAlreadyAddedException
	 *                if that player-character exist in database.
	 * @exception GenericDatabaseException
	 *                if the character doesn't exist or it is not owned by the
	 *                player.
	 */
	public void addCharacter(Transaction trans, String username,
			String character, RPObject object) throws PlayerNotFoundException,
			CharacterAlreadyAddedException, GenericDatabaseException;

	/**
	 * This method removes a character asociated with a player.
	 * 
	 * @param username
	 *            is the name of the player
	 * @param character
	 *            is the name of the character that the username player owns.
	 * @exception PlayerNotFoundException
	 *                if the player doesn't exist in database.
	 * @exception CharacterNotFoundException
	 *                if the character doesn't exist or it is not owned by the
	 *                player.
	 */
	public void removeCharacter(Transaction trans, String username,
			String character) throws PlayerNotFoundException,
			CharacterNotFoundException, GenericDatabaseException;

	/**
	 * This method returns a valid connection
	 * 
	 * @return a valid Transaction
	 * @exception GenericDatabaseException
	 *                if connection to DB cannot be established
	 */
	public Transaction getTransaction() throws GenericDatabaseException;
}
