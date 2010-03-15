/* $Id: LoginEventDAO.java,v 1.20 2010/03/15 18:54:46 nhnb Exp $ */
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

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.Pair;
import marauroa.common.TimeoutConf;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;

/**
 * data access object for login events
 *
 * @author miguel, hendrik
 */
public class LoginEventDAO {
	private static final marauroa.common.Logger logger = Log4J.getLogger(LoginEventDAO.class);

	/**
	 * Creates a new LoginEventDAO
	 */
	protected LoginEventDAO() {
		// hide constructor as this class should only be instantiated by DAORegister
	}

	/**
	 * logs an login attempt
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param source ip-address
	 * @param correctLogin true, if the login was succesful; false otherwise
	 * @throws SQLException in case of an database error
	 */
	@Deprecated
	public void addLoginEvent(DBTransaction transaction, String username, InetAddress source, boolean correctLogin) throws SQLException {
		addLoginEvent(transaction, username, source, null, null, correctLogin);
	}

	/**
	 * logs an login attempt
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param source ip-address
	 * @param seed seed
	 * @param correctLogin true, if the login was succesful; false otherwise
	 * @throws SQLException in case of an database error
	 */
	@Deprecated
	public void addLoginEvent(DBTransaction transaction, String username, InetAddress source, String seed, boolean correctLogin) throws SQLException {
		addLoginEvent(transaction, username, source, null, seed, correctLogin);
	}

	/**
	 * logs an login attempt
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param source ip-address
	 * @param service name of service
	 * @param seed seed
	 * @param correctLogin true, if the login was succesful; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public void addLoginEvent(DBTransaction transaction, String username, InetAddress source, String service, String seed, boolean correctLogin) throws SQLException {

		try {
			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			// Note: playerId == -1 means that the player does not exist. We log this anyway to
			// be able to notice if someone tries to hack accounts by picking	a fixed password
			// and bruteforcing matching usernames.

			String query = "insert into loginEvent(player_id, address, service, seed, result)"
				+ " values ([player_id], '[address]', '[service]', '[seed]', [result])";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("player_id", Integer.valueOf(id));
			params.put("address", source.getHostAddress());
			params.put("result", Integer.valueOf(correctLogin ? 1 : 0));
			if (service != null) {
				params.put("service", service);
			}
			if (seed == null) {
				seed = "";
			}
			params.put("seed", seed);
			transaction.execute(query, params);
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		}
	}

	/** Class to store the login events */
	public static class LoginEvent {

		private long id = -1;
		
		private long playerId = -1;
		
		private String service = null;

		/** TCP/IP address of the source of the login message */
		public String address;

		/** Time and date of the login event */
		public String date;

		/** True if login was correct */
		public boolean correct;

		/**
		 * Creates a new LoginEvent object
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
		 * Creates a new LoginEvent object
		 * 
		 * @param id database id
		 * @param playerId database id of account
		 * @param service name of service
		 * @param address the address from where the login was tried
		 * @param date the date at which login was tried
		 * @param sucessful true, if the attempt was successful; false otherwise
		 */
		public LoginEvent(long id, long playerId, String service,
				String address, String date, boolean sucessful) {

			this(address, date, sucessful);
			this.id = id;
			this.playerId = playerId;
			this.service = service;
		}

		/**
		 * This method returns a String that represent the object.
		 *
		 * @return a string representing the object.
		 */
		@Override
		public String toString() {
			return "Login " + (correct ? "successful" : "FAILED") + " at " + date + " from " + address;
		}

		/**
		 * gets the id of this database row
		 *
		 * @return id or -1
		 */
		public long getId() {
			return id;
		}

		/**
		 * gets the id of the account
		 *
		 * @return player_id or -1
		 */
		public long getPlayerId() {
			return playerId;
		}

		/**
		 * gets the name of the service
		 *
		 * @return id or <code>null</code>
		 */
		public String getService() {
			return service;
		}

		/**
		 * gets the ip-address
		 * 
		 * @return ip-address
		 */
		public String getAddress() {
			return address;
		}

		/**
		 * gets the timestamp of the login attempt
		 *
		 * @return timestamp
		 */
		public String getDate() {
			return date;
		}

		/**
		 * was this a successful login attempt?
		 *
		 * @return true, if the attempt was successful; false otherwise
		 */
		public boolean isSuccessful() {
			return correct;
		}

	}

	/**
	 * gets a list of recent login events
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @param events number of events
	 * @return list of login attempts
	 * @throws SQLException in case of an database error
	 */
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

			resultSet.close();
			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + username + "\"", e);
			throw e;
		}
	}

	/**
	 * gets the last successful login event
	 *
	 * @param transaction DBTransaction
	 * @param playerId accountId
	 * @param service name of service, may be <code>null</code> for all
	 * @return last succesful login event
	 * @throws SQLException in case of an database error
	 */
	public LoginEvent getLastSuccessfulLoginEvent(DBTransaction transaction, int playerId, String service) throws SQLException {
		try {
			String serviceQuery = "";
			if (service != null) {
				serviceQuery = " AND service='[service]'";
			}
			String query = "SELECT id, player_id, service, address, timedate, result FROM loginEvent"
				+ " WHERE player_id=[player_id] AND result=1" + serviceQuery + " ORDER BY timedate desc LIMIT 2";

			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", service);
			params.put("player_id", Integer.toString(playerId));
			
			logger.debug("getLastSuccessfulLoginEvent is executing query " + query);

			ResultSet resultSet = transaction.query(query, params);

			// go to this login
			if (!resultSet.next()) {
				resultSet.close();
				return null;
			}
			// go to the last login
			if (!resultSet.next()) {
				resultSet.close();
				return null;
			}
			
			LoginEvent event = new LoginEvent(resultSet.getLong("id"), 
					resultSet.getLong("player_id"), resultSet.getString("service"),
					resultSet.getString("address"), resultSet.getString("timedate"),
					resultSet.getBoolean("result"));
			resultSet.close();
			return event;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + playerId + "\"", e);
			throw e;
		}
	}	

	/**
	 * gets the amount of failed login attemps
	 *
	 * @param transaction DBTransaction
	 * @param id only look for events younger than this id
	 * @param playerId accountId
	 * @return amount of failed login attempts grouped by service name
	 * @throws SQLException in case of an database error
	 */
	public List<Pair<String, Long>> getAmountOfFailedLogins(DBTransaction transaction, long id, int playerId) throws SQLException {
		try {
			String query = "SELECT service, count(*) FROM loginEvent"
				+ " WHERE player_id=[player_id] + AND id > [id] AND result = 0 GROUP BY service";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", Long.toString(id));
			params.put("player_id", Integer.toString(playerId));
			logger.debug("getAmountOfFailedLogins is executing query " + query);
		
			ResultSet resultSet = transaction.query(query, params);
			List<Pair<String, Long>> list = new LinkedList<Pair<String, Long>>();
			while (resultSet.next()) {
				list.add(new Pair<String, Long>(resultSet.getString(1), Long.valueOf(resultSet.getLong(2))));
			}
			resultSet.close();
			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + playerId + "\"", e);
			throw e;
		}
	}	

	/**
	 * checks if this account is temporary blocked because of too many failed login attempts.
	 * Blocking accounts is not related to banning accounts.
	 *
	 * @param transaction DBTransaction
	 * @param username username
	 * @return true, if this account is blocked; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean isAccountBlocked(DBTransaction transaction, String username) throws SQLException {
		String query = "SELECT count(*) as amount FROM loginEvent, account"
				+ " WHERE loginEvent.player_id=account.id"
				+ " AND username='[username]'"
		        + " AND loginEvent.result=0 and loginEvent.timedate > '[timestamp]'";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", username);

		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.SECOND, -1 * TimeoutConf.FAILED_LOGIN_BLOCKTIME);
		params.put("timestamp", new Timestamp(calendar.getTimeInMillis()).toString());

		int attemps = transaction.querySingleCellInt(query, params);
		return attemps > TimeoutConf.FAILED_LOGIN_ATTEMPTS_ACCOUNT;
	}

	/**
	 * checks if the ip-address is temporary blocked because of too many failed login attempts.
	 * Blocking ip-addresses is not related to banning ip-addresses.
	 *
	 * @param transaction DBTransaction
	 * @param address ip-address
	 * @return true, if this address is blocked; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean isAddressBlocked(DBTransaction transaction, String address) throws SQLException {
		String query = "SELECT count(*) as amount FROM loginEvent"
				+ " WHERE address='[address]'"
		        + " AND result=0 and timedate > '[timestamp]'";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("address", address);

		Calendar calendar = new GregorianCalendar();
		calendar.add(Calendar.SECOND, -1 * TimeoutConf.FAILED_LOGIN_BLOCKTIME);
		params.put("timestamp", new Timestamp(calendar.getTimeInMillis()).toString());

		int attemps = transaction.querySingleCellInt(query, params);
		return attemps > TimeoutConf.FAILED_LOGIN_ATTEMPTS_IP;
	}
	

	/**
	 * logs an login attempt
	 *
	 * @param username username
	 * @param source ip-address
	 * @param correctLogin true, if the login was succesful; false otherwise
	 * @throws SQLException in case of an database error
	 */
	@Deprecated
	public void addLoginEvent(String username, InetAddress source, boolean correctLogin) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addLoginEvent(transaction, username, source, null, null, correctLogin);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}


	/**
	 * logs an login attempt
	 *
	 * @param seed a seed to log
	 * @param username username
	 * @param source ip-address
	 * @param correctLogin true, if the login was succesful; false otherwise
	 * @throws SQLException in case of an database error
	 */
	@Deprecated
	public void addLoginEvent(String username, InetAddress source, String seed, boolean correctLogin) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addLoginEvent(transaction, username, source, null, seed, correctLogin);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * logs an login attempt
	 *
	 * @param username username
	 * @param source ip-address
	 * @param service name of service
	 * @param seed seed
	 * @param correctLogin true, if the login was succesful; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public void addLoginEvent(String username, InetAddress source, String service, String seed, boolean correctLogin) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			addLoginEvent(transaction, username, source, service, seed, correctLogin);
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	
	/**
	 * gets a list of recent login events
	 *
	 * @param username username
	 * @param events number of events
	 * @return list of login attempts
	 * @throws SQLException in case of an database error
	 */
	public List<String> getLoginEvents(String username, int events) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			List<String> res = getLoginEvents(transaction, username, events);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}


	/**
	 * checks if this account is temporary blocked because of too many failed login attempts.
	 * Blocking accounts is not related to banning accounts.
	 *
	 * @param username username
	 * @return true, if this account is blocked; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean isAccountBlocked(String username) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = isAccountBlocked(transaction, username);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

	/**
	 * checks if the ip-address is temporary blocked because of too many failed login attempts.
	 * Blocking ip-addresses is not related to banning ip-addresses.
	 *
	 * @param address ip-address
	 * @return true, if this address is blocked; false otherwise
	 * @throws SQLException in case of an database error
	 */
	public boolean isAddressBlocked(String address) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			boolean res = isAddressBlocked(transaction, address);
			return res;
		} finally {
			TransactionPool.get().commit(transaction);
		}
	}

}
