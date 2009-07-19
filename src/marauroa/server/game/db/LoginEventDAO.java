/* $Id: LoginEventDAO.java,v 1.9 2009/07/19 09:27:56 nhnb Exp $ */
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Log4J;
import marauroa.common.Pair;
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

	public void addLoginEvent(DBTransaction transaction, String username, InetAddress source, boolean correctLogin) throws SQLException {

		try {
			int id = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
			// Note: playerId == -1 means that the player does not exist. We log this anyway to
			// be able to notice if someone tries to hack accounts by picking	a fixed password
			// and bruteforcing matching usernames.

			String query = "insert into loginEvent(player_id, address, timedate, result)"
				+ " values ([player_id], '[address]', NULL, [result])";
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
				return null;
			}
			// go to the last login
			if (!resultSet.next()) {
				return null;
			}
			
			LoginEvent event = new LoginEvent(/*TODO:  resultSet.getLong("id"), 
					resultSet.getLong("player_id"), resultSet.getString("service"),*/
					resultSet.getString("address"), resultSet.getString("timedate"),
					resultSet.getBoolean("result"));
			return event;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + playerId + "\"", e);
			throw e;
		}
	}	

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
			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player \"" + playerId + "\"", e);
			throw e;
		}
	}	

	public void addLoginEvent(String username, InetAddress source, boolean correctLogin) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		addLoginEvent(transaction, username, source, correctLogin);
		TransactionPool.get().commit(transaction);
	}

	public List<String> getLoginEvents(String username, int events) throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		List<String> res = getLoginEvents(transaction, username, events);
		TransactionPool.get().commit(transaction);
		return res;
	}
}
