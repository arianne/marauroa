package marauroa.server.game.db;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;

public class LoginEventDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(LoginEventDAO.class);

	public void addLoginEvent(DBTransaction transaction, String username, InetAddress source,
	        boolean correctLogin) throws SQLException {
		try {
			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);

			if (id == -1) {
				/**
				 * This will happen when the player doesn't exist at database.
				 * For example, a mispelled username.
				 */
				return;
			}

			String query = "insert into loginEvent(player_id, address, timedate, result)"
				+ "values([player_id], '[address]', NULL, [result])";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(id));
			params.put("address", source.getHostAddress());
			params.put("result", Integer.valueOf(correctLogin ? 1 : 0));
			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		}
	}

	/** Class to store the login events */
	public static class LoginEvent {

		/** TCP/IP address of the source of the login message */
		public String address;

		/** Time and date of the login event */
		public String date;

		/** True if login was correct */
		public boolean correct;

		/**
		 * Constructor
		 *
		 * @param address
		 *            the address from where the login was tried
		 * @param date
		 *            the date at which login was tried
		 * @param sucessful
		 *            true if login was sucessful
		 */
		public LoginEvent(String address, String date, boolean sucessful) {
			this.address = address;
			this.date = date;
			this.correct = sucessful;
		}

		/**
		 * This method returns a String that represent the object.
		 *
		 * @return a string representing the object.
		 */
		@Override
		public String toString() {
			return "Login " + (correct ? "SUCCESSFUL" : "FAILED") + " at " + date + " from " + address;
		}
	}

	public List<String> getLoginEvents(DBTransaction transaction, String username, int events) throws SQLException {
		try {
			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			if (id == -1) {
				/*
				 * This should never happen as we should check previously that
				 * player exists...
				 */
				throw new SQLException("Unable to find player(" + username + ")");
			}

			String query = "select address, timedate, result from loginEvent where player_id=[player_id]"
			        + " order by timedate desc limit [events]";
			logger.debug("getLoginEvents is executing query " + query);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(id));
			params.put("events", Integer.valueOf(events));

			ResultSet resultSet = transaction.query(query, params);

			List<String> list = new LinkedList<String>();

			while (resultSet.next()) {
				LoginEvent event = new LoginEvent(resultSet.getString("address"), resultSet
				        .getString("timedate"), resultSet.getBoolean("result"));
				list.add(event.toString());
			}

			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		}
	}
}
