package marauroa.server.game.db.nio;

import java.sql.SQLException;

import marauroa.server.game.db.JDBCTransaction;

public interface IPlayerAccess {

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
	 */
	public boolean removePlayer(JDBCTransaction transaction, String username);

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

}