package marauroa.server.game.db.nio;

import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.List;

import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.db.JDBCTransaction;
import marauroa.server.game.db.nio.NIOJDBCDatabase.LoginEvent;

public interface ILoginEventsAccess {

	/**
	 * This method returns true if the player match in database matches the given password and the account is active.
	 * @param transaction the database transaction
	 * @param informations login informations created by GameServerManager
	 * @return true if account information is correct or false otherwise
	 * @throws SQLException if there is any database problem.
	 */
	public abstract boolean verify(JDBCTransaction transaction,
			PlayerEntry.SecuredLoginInfo informations) throws SQLException;

	public abstract void addLoginEvent(JDBCTransaction transaction,
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
	public abstract List<LoginEvent> getLoginEvents(
			JDBCTransaction transaction, String username) throws SQLException;

}