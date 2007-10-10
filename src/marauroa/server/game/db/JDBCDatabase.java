/* $Id: JDBCDatabase.java,v 1.54 2007/10/10 22:05:21 nhnb Exp $ */
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import marauroa.server.game.Statistics.Variables;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.rp.RPObjectFactory;
import marauroa.server.net.validator.InetAddressMask;

/**
 * This is a JDBC implementation for MySQL is IDatabase interface.
 *
 * @author miguel
 *
 */
public class JDBCDatabase implements IDatabase {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(JDBCDatabase.class);

	/** connection info * */
	private Properties connInfo;

	protected JDBCSQLHelper sql;
	protected RPObjectFactory factory;


	private JDBCTransaction transaction;

	/**
	 * Constructor that connect using a set of Properties.
	 *
	 * @param connInfo
	 *            a Properties set with the options to create the database.
	 *            Properties should have: jdbc_url Contains the URL of the
	 *            Database we are going to connect to
	 *            jdbc:mysql://127.0.0.1/marauroa jdbc_class Contains the driver
	 *            class of the JDBC storage com.mysql.jdbc.Driver jdbc_user
	 *            Username we are going to use to connect to database jdbc_pwd
	 *            Password.
	 */
	protected JDBCDatabase(Properties connInfo) throws NoDatabaseConfException {
		this.connInfo = connInfo;

		sql = JDBCSQLHelper.get();
		transaction = (JDBCTransaction) getTransaction();
		factory=RPObjectFactory.get();

		initialize();
	}

	/**
	 * Creates the database Tables. Extend it to change the database
	 * initialization.
	 */
	protected void initialize() {
		sql.runDBScript(transaction, "marauroa/server/marauroa_init.sql");
	}

	private static JDBCDatabase database;

	/**
	 * Returns an unique instance of the Database.
	 *
	 * @return an unique instance of the Database.
	 */
	public static JDBCDatabase getDatabase() {
		if (database == null) {
			Configuration conf = null;

			try {
				conf = Configuration.getConfiguration();
			} catch (Exception e) {
				logger.fatal("Unable to locate Configuration file: "
				        + Configuration.getConfigurationFile(), e);
				throw new NoDatabaseConfException();
			}

			Properties props = new Properties();

			props.put("jdbc_url", conf.get("jdbc_url"));
			props.put("jdbc_class", conf.get("jdbc_class"));
			props.put("jdbc_user", conf.get("jdbc_user"));
			props.put("jdbc_pwd", conf.get("jdbc_pwd"));

			database = new JDBCDatabase(props);
		}

		return database;
	}

	/**
	 * This method creates the real connection to database.
	 *
	 * @param props
	 *            the database configuration.
	 * @return a Connection to Database
	 * @throws NoDatabaseConfException
	 *             If there is any kind of problem creating the connection.
	 */
	private Connection createConnection(Properties props) throws NoDatabaseConfException {
		try {
			/* We instantiate now the Driver class */
			try {
				Class.forName((String) props.get("jdbc_class")).newInstance();
			} catch (Exception e) {
				logger.fatal("Unable to create Driver class: " + props.get("jdbc_class"), e);
				throw new NoDatabaseConfException(e);
			}

			Properties connInfo = new Properties();

			connInfo.put("user", props.get("jdbc_user"));
			connInfo.put("password", props.get("jdbc_pwd"));
			connInfo.put("charSet", "UTF-8");

			Connection conn = DriverManager.getConnection((String) props.get("jdbc_url"), connInfo);

			/*
			 * This is for MySQL:
			 * - We set auto commit to false, so that transactions work
			 * - We set isolation mode to this so everything works. Google for it
			 */
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			return conn;
		} catch (SQLException e) {
			logger.fatal("Unable to create a connection to: " + props.get("jdbc_url"), e);
			throw new NoDatabaseConfException(e);
		}
	}

	/**
	 * This method gets a transaction from the connection and check that it is
	 * valid.
	 *
	 * @return a valid transaction.
	 */
	public Transaction getTransaction() {
		if (transaction == null || !transaction.isValid()) {
			transaction = new JDBCTransaction(createConnection(connInfo));
		}

		return transaction;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IPlayerAccess#addPlayer(marauroa.server.game.db.Transaction,
	 *      java.lang.String, byte[], java.lang.String)
	 */
	public void addPlayer(Transaction transaction, String username, byte[] password, String email)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(email)) {
				throw new SQLException("Invalid string username=(" + username + ") email=(" + email
				        + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			String query = "insert into account(id,username,password,email,timedate,status) values("
			        + "NULL,'"
			        + username
			        + "','"
			        + Hash.toHexString(password)
			        + "','"
			        + email
			        + "'," + "NULL," + "DEFAULT)";

			logger.debug("addPlayer is using query: " + query);

			stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't add player(" + username + ") to Database", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.IDatabase#generatePlayer(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public String generatePlayer(Transaction transaction, String pattern) throws SQLException {
		int length = pattern.length();
		Random rand = new Random();
		StringBuffer os = new StringBuffer();

		for (int i = 0; i < length; i++) {
			char c = pattern.charAt(i);
			if (c == '#') {
				// Replaced the # with a number between 0 and 10.
				os.append(rand.nextInt(10));
			} else if (c == '@') {
				// Replaced @ with a lower case letter between a and z
				char character = (char) (rand.nextInt(26) + 97);
				os.append(character);
			} else {
				// if it isn't anyone of the above, just add the character.
				os.append(c);
			}
		}

		return os.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.IDatabase#changeEmail(marauroa.server.game.db.Transaction,
	 *      java.lang.String, java.lang.String)
	 */
	public void changeEmail(Transaction transaction, String username, String email)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ") email=(" + email
				        + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			int id = getDatabasePlayerId(transaction, username);

			String query = "update account set email='" + email + "' where id=" + id;
			logger.debug("changePassword is using query: " + query);

			stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't remove player(" + username + ") to Database", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.IDatabase#changePassword(marauroa.server.game.db.Transaction,
	 *      java.lang.String, byte[])
	 */
	public void changePassword(Transaction transaction, String username, String password)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			int id = getDatabasePlayerId(transaction, username);

			byte[] hashedPassword = Hash.hash(password);

			String query = "update account set password='" + Hash.toHexString(hashedPassword)
			        + "' where id=" + id;
			logger.debug("changePassword is using query: " + query);

			stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't remove player(" + username + ") to Database", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IPlayerAccess#removePlayer(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public boolean removePlayer(Transaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			/* We first remove any characters associated with this player. */
			for (String character : getCharacters(transaction, username)) {
				removeCharacter(transaction, username, character);
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			String query = "delete from account where username='" + username + "'";

			logger.debug("removePlayer is using query: " + query);
			stmt.execute(query);
			stmt.close();

			return true;
		} catch (SQLException e) {
			logger.error("Can't remove player(" + username + ") to Database", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IPlayerAccess#hasPlayer(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public boolean hasPlayer(Transaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			String query = "select count(*) as amount from  account where username like '"
			        + username + "'";

			logger.debug("hasPlayer is using query: " + query);

			ResultSet result = stmt.executeQuery(query);

			boolean playerExists = false;

			if (result.next() && result.getInt("amount") != 0) {
				playerExists = true;
			}

			stmt.close();
			return playerExists;
		} catch (SQLException e) {
			logger.error("Can't query for player(" + username + ")", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IPlayerAccess#setAccountStatus(marauroa.server.game.db.Transaction,
	 *      java.lang.String, java.lang.String)
	 */
	public void setAccountStatus(Transaction transaction, String username, String status)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(status)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			String query = "update account set status='" + status + "' where username like '"
			        + username + "'";
			logger.debug("setAccountStatus is executing query " + query);

			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't udpate account status of player(" + username + ")", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IPlayerAccess#getAccountStatus(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public String getAccountStatus(Transaction transaction, String username) throws SQLException {
		try {
			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			String query = "select status from account where username like '" + username + "'";

			logger.debug("getAccountStatus is executing query " + query);

			ResultSet result = stmt.executeQuery(query);

			String status = null;

			if (result.next()) {
				status = result.getString("status");
			}

			result.close();
			stmt.close();

			return status;
		} catch (SQLException e) {
			logger.error("Can't query player(" + username + ")", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IPlayerAccess#getAccountStatus(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public String getEmail(Transaction transaction, String username) throws SQLException {
		try {
			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			String query = "select email from account where username like '" + username + "'";

			logger.debug("getEmail is executing query " + query);

			ResultSet result = stmt.executeQuery(query);

			String status = null;

			if (result.next()) {
				status = result.getString("email");
			}

			result.close();
			stmt.close();

			return status;
		} catch (SQLException e) {
			logger.error("Can't query player(" + username + ")", e);
			throw e;
		}
	}

	private int getDatabasePlayerId(Transaction trans, String username) throws SQLException {
		Connection connection = trans.getConnection();
		Statement stmt = connection.createStatement();

		if (!StringChecker.validString(username)) {
			throw new SQLException("Invalid string username=(" + username + ")");
		}

		String query = "select id from account where username like '" + username + "'";

		logger.debug("getDatabasePlayerId is executing query " + query);

		ResultSet result = stmt.executeQuery(query);

		int id = -1;

		if (result.next()) {
			id = result.getInt("id");
		}

		result.close();
		stmt.close();

		return id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.ICharacterAccess#addCharacter(marauroa.server.game.db.Transaction,
	 *      java.lang.String, java.lang.String, marauroa.common.game.RPObject)
	 */
	public void addCharacter(Transaction transaction, String username, String character,
	        RPObject player) throws SQLException, IOException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			int id = getDatabasePlayerId(transaction, username);
			int object_id = storeRPObject(transaction, player);

			String query = "insert into characters(player_id,charname,object_id) values(" + id
			        + ",'" + character + "'," + object_id + ")";
			logger.debug("addCharacter is executing query " + query);
			logger.debug("Character: " + player);
			stmt.execute(query);
			stmt.close();

		} catch (SQLException e) {
			logger.error("Can't add player(" + username + ") character(" + character + ")", e);
			throw e;
		} catch (IOException e) {
			logger.error("Can't add player(" + username + ") character(" + character + ")", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.ICharacterAccess#removeCharacter(marauroa.server.game.db.Transaction,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean removeCharacter(Transaction transaction, String username, String character)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			int player_id = getDatabasePlayerId(transaction, username);

			String query = "select object_id from characters where player_id=" + player_id
			        + " and charname='" + character + "'";
			ResultSet result = stmt.executeQuery(query);

			if (result.next()) {
				int id = result.getInt("object_id");
				removeRPObject(transaction, id);
			} else {
				result.close();
				stmt.close();

				throw new SQLException("Character (" + character
				        + ") without object: Database integrity error.");
			}
			result.close();

			query = "delete from characters where player_id=" + player_id + " and charname='"
			        + character + "'";

			logger.debug("removeCharacter is using query: " + query);
			stmt.execute(query);
			stmt.close();

			return true;
		} catch (SQLException e) {
			logger.error("Can't remove player(" + username + ") from Database", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.ICharacterAccess#hasCharacter(marauroa.server.game.db.Transaction,
	 *      java.lang.String, java.lang.String)
	 */
	public boolean hasCharacter(Transaction transaction, String username, String character)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			/*
			 * NOTE: 
			 * Perse we have agreed that character name is unique per server, 
			 * so we check just characters ignoring username.
			 */
			String query = "select count(*) as amount from  characters where "
			        + "charname like '" + character + "'";

			logger.debug("hasCharacter is executing query " + query);

			ResultSet result = stmt.executeQuery(query);

			boolean characterExists = false;

			if (result.next() && result.getInt("amount") != 0) {
				characterExists = true;
			}

			result.close();
			stmt.close();

			return characterExists;
		} catch (SQLException e) {
			logger
			        .error("Can't query for player(" + username + ") character(" + character + ")",
			                e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.ICharacterAccess#getCharacters(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public List<String> getCharacters(Transaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			int id = getDatabasePlayerId(transaction, username);
			if (id == -1) {
				/**
				 * This should never happen as we should check previously that
				 * player exists...
				 */
				throw new SQLException("Unable to find player(" + username + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			String query = "select charname from characters where player_id=" + id;

			logger.debug("getCharacterList is executing query " + query);

			ResultSet charactersSet = stmt.executeQuery(query);
			List<String> list = new LinkedList<String>();

			while (charactersSet.next()) {
				list.add(charactersSet.getString("charname"));
			}

			charactersSet.close();

			stmt.close();
			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player(" + username + ")", e);
			throw e;
		}
	}

	/**
	 * This method stores a character's avatar at database and update the link
	 * with Character table.
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
	public void storeCharacter(Transaction transaction, String username, String character,
	        RPObject player) throws SQLException, IOException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			int objectid = storeRPObject(transaction, player);
			int id = getDatabasePlayerId(transaction, username);

			String query = "update characters set object_id=" + objectid + " where charname='"
			        + character + "' and player_id=" + id;
			logger.debug("storeCharacter is executing query " + query);
			logger.debug("Character: " + player);

			stmt.execute(query);

			stmt.close();

		} catch (SQLException sqle) {
			logger.warn("Error storing character: " + player, sqle);
			throw sqle;
		} catch (IOException e) {
			logger.warn("Error storing character: " + player, e);
			throw e;
		}
	}

	/**
	 * This method load from database the character's avatar asociated to this
	 * character.
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
	public RPObject loadCharacter(Transaction transaction, String username, String character)
	        throws SQLException, IOException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=(" + username + ") character=("
				        + character + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			int id = getDatabasePlayerId(transaction, username);
			String query = "select object_id from characters where charname='" + character
			        + "' and player_id=" + id;
			logger.debug("loadCharacter is executing query " + query);
			ResultSet result = stmt.executeQuery(query);

			RPObject player = null;
			if (result.next()) {
				int objectid = result.getInt("object_id");
				player = loadRPObject(transaction, objectid);
				logger.debug("Character: " + player);
			}

			result.close();
			stmt.close();

			return player;
		} catch (SQLException sqle) {
			logger.warn("Error loading character: " + character, sqle);
			throw sqle;
		} catch (IOException e) {
			logger.warn("Error loading character: " + character, e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	public void loadRPZone(Transaction transaction, IRPZone zone) throws SQLException, IOException {
		String zoneid = zone.getID().getID();
		if (!StringChecker.validString(zoneid)) {
			throw new SQLException("Invalid string zoneid=(" + zoneid + ")");
		}

		Connection connection = transaction.getConnection();

		String query = "select data from rpzone where zone_id='" + zoneid + "'";
		logger.debug("loadRPZone is executing query " + query);

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		if (rs.next()) {
			Blob data = rs.getBlob("data");
			InputStream input = data.getBinaryStream();
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			// set read buffer size
			byte[] rb = new byte[1024];
			int ch = 0;
			// process blob
			while ((ch = input.read(rb)) != -1) {
				output.write(rb, 0, ch);
			}
			byte[] content = output.toByteArray();
			input.close();
			output.close();

			ByteArrayInputStream inStream = new ByteArrayInputStream(content);
			InflaterInputStream szlib = new InflaterInputStream(inStream, new Inflater());
			InputSerializer inser = new InputSerializer(szlib);

			rs.close();
			stmt.close();

			int amount = inser.readInt();

			for (int i = 0; i < amount; i++) {
				try {
					RPObject object = factory.transform((RPObject) inser.readObject(new RPObject()));

					/* Give the object a valid id and add it */
					zone.assignRPObjectID(object);
					zone.add(object);
				} catch (Exception e) {
					logger.error("Problem loading RPZone: ", e);
				}
			}
		}

		rs.close();
		stmt.close();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.IDatabase#storeRPZone(marauroa.server.game.db.Transaction,
	 *      marauroa.common.game.IRPZone)
	 */
	public void storeRPZone(Transaction transaction, IRPZone zone) throws IOException, SQLException {
		String zoneid = zone.getID().getID();
		if (!StringChecker.validString(zoneid)) {
			throw new SQLException("Invalid string zoneid=(" + zoneid + ")");
		}

		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer os = new OutputSerializer(out_stream);

		/* compute how many storable objects exists in zone. */
		int amount = 0;
		for (RPObject object : zone) {
			if (object.isStorable()) {
				amount++;
			}
		}

		os.write(amount);

		for (RPObject object : zone) {
			if (object.isStorable()) {
				object.writeObject(os, DetailLevel.FULL);
			}
		}

		out_stream.close();

		/* Setup the stream for a blob */
		ByteArrayInputStream inStream = new ByteArrayInputStream(array.toByteArray());

		String query;

		if (hasRPZone(transaction, zone.getID())) {
			query = "update rpzone set data=? where zone_id='" + zoneid + "'";
		} else {
			query = "insert into rpzone(zone_id,data) values('" + zoneid + "',?)";
		}
		logger.debug("storeRPZone is executing query " + query);

		Connection connection = transaction.getConnection();
		PreparedStatement ps = connection.prepareStatement(query);
		ps.setBinaryStream(1, inStream, inStream.available());
		ps.executeUpdate();
		ps.close();
	}

	protected boolean hasRPZone(Transaction transaction, IRPZone.ID zone) throws SQLException {
		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();

		String query = "SELECT count(*) as amount FROM rpzone where zone_id='" + zone.getID() + "'";

		ResultSet eventSet = stmt.executeQuery(query);
		boolean exists = false;

		while (eventSet.next()) {
			int has = eventSet.getInt("amount");
			if (has > 0) {
				exists = true;
			}
		}

		eventSet.close();
		stmt.close();

		return exists;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.IDatabase#isAccountBlocked(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public boolean isAccountBlocked(Transaction transaction, String username) throws SQLException {
		if (!StringChecker.validString(username)) {
			throw new SQLException("Invalid string username=(" + username + ")");
		}

		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();

		int id = getDatabasePlayerId(transaction, username);
		String query = "SELECT count(*) as amount FROM loginEvent where player_id=" + id
		        + " and result=0 and (now()-timedate)<"
		        + TimeoutConf.FAILED_LOGIN_BLOCKTIME;

		ResultSet eventSet = stmt.executeQuery(query);
		boolean blocked = false;

		while (eventSet.next()) {
			int attemps = eventSet.getInt("amount");
			if (attemps > TimeoutConf.FAILED_LOGIN_ATTEMPS) {
				blocked = true;
			}
		}

		eventSet.close();
		stmt.close();

		return blocked;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.ILoginEventsAccess#verify(marauroa.server.game.db.Transaction,
	 *      marauroa.server.game.container.PlayerEntry.SecuredLoginInfo)
	 */
	public boolean verify(Transaction transaction, PlayerEntry.SecuredLoginInfo informations)
	        throws SQLException {
		if (Hash.compare(Hash.hash(informations.clientNonce), informations.clientNonceHash) != 0) {
			logger.debug("Different hashs for client Nonce");
			return false;
		}

		byte[] b1 = informations.key.decodeByteArray(informations.password);
		byte[] b2 = Hash.xor(informations.clientNonce, informations.serverNonce);
		if (b2 == null) {
			logger.debug("B2 is null");
			return false;
		}

		byte[] password = Hash.xor(b1, b2);
		if (password == null) {
			logger.debug("Password is null");
			return false;
		}

		if (!StringChecker.validString(informations.username)) {
			throw new SQLException("Invalid string username=(" + informations.username + ")");
		}

		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();

		// check new Marauroa 2.0 password type
		String hexPassword = Hash.toHexString(password);
		boolean res = verifyUsingDB(stmt, informations.username, hexPassword);

		if (!res) {
			// compatiblity: check new Marauroa 1.0 password type
			hexPassword = Hash.toHexString(Hash.hash(password));
			res = verifyUsingDB(stmt, informations.username, hexPassword);
		}

		stmt.close();

		return res;
	}
	
	private boolean verifyUsingDB(Statement stmt, String username, String hexPassword) throws SQLException {
		try {
			String query = "select status, username from account where username like '"
			        + username + "' and password like '" + hexPassword + "'";
			logger.debug("verifyAccount is executing query " + query);
			ResultSet result = stmt.executeQuery(query);

			boolean isplayer = false;

			if (result.next()) {
				String userNameFromDB = result.getString("username");
				String account_status = result.getString("status");

				if ("active".equals(account_status)) {
					isplayer = true;
				} else {
					logger.debug("Username/password is ok, but account is in status {"
					        + account_status + "}");
				}

				if (!userNameFromDB.equals(username)) {
					logger.warn("Username(" + username
					        + ") is not the same that stored username(" + userNameFromDB + ")");
					isplayer = false;
				}

			}

			result.close();


			return isplayer;
		} catch (SQLException e) {
			logger.error("Can't query for player(" + username + ")", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.ILoginEventsAccess#addLoginEvent(marauroa.server.game.db.Transaction,
	 *      java.lang.String, java.net.InetSocketAddress, boolean)
	 */
	public void addLoginEvent(Transaction transaction, String username, InetAddress source,
	        boolean correctLogin) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			int id = getDatabasePlayerId(transaction, username);

			if (id == -1) {
				/**
				 * This will happen when the player doesn't exist at database.
				 * For example, a mispelled username.
				 */
				return;
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			String query = "insert into loginEvent(player_id,address,timedate,result) values(" + id
			        + ",'" + source.getHostAddress() + "',NULL," + (correctLogin ? 1 : 0) + ")";
			stmt.execute(query);

			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't query for player(" + username + ")", e);
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
		 * This method returns a String that represent the object
		 *
		 * @return a string representing the object.
		 */
		@Override
		public String toString() {
			return "Login " + (correct ? "SUCESSFULL" : "FAILED") + " at " + date + " from "
			        + address;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.ILoginEventsAccess#getLoginEvents(marauroa.server.game.db.Transaction,
	 *      java.lang.String)
	 */
	public List<String> getLoginEvents(Transaction transaction, String username, int events)
	        throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=(" + username + ")");
			}

			int id = getDatabasePlayerId(transaction, username);
			if (id == -1) {
				/**
				 * This should never happen as we should check previously that
				 * player exists...
				 */
				throw new SQLException("Unable to find player(" + username + ")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			String query = "select address, timedate, result from loginEvent where player_id=" + id
			        + " order by timedate desc limit " + events;

			logger.debug("getLoginEvents is executing query " + query);

			ResultSet eventSet = stmt.executeQuery(query);
			List<String> list = new LinkedList<String>();

			while (eventSet.next()) {
				LoginEvent event = new LoginEvent(eventSet.getString("address"), eventSet
				        .getString("timedate"), eventSet.getBoolean("result"));
				list.add(event.toString());
			}

			eventSet.close();

			stmt.close();
			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player(" + username + ")", e);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IEventsAccess#addGameEvent(marauroa.server.game.db.Transaction,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addGameEvent(Transaction trans, String source, String event, String... params) {
		try {
			Connection connection = trans.getConnection();
			Statement stmt = connection.createStatement();

			String firstParam = (params.length > 0 ? params[0] : "");
			StringBuffer param = new StringBuffer();
			if (params.length > 1) {
				for (int i = 1; i < params.length; i++) {
					param.append(params[i]);
					param.append(" ");
				}
			}
			String param2 = param.toString();
			
			/*
			 * TODO: Clear this.
			 */

			// write the row to the database, escaping and cutting the paramters to column size
			String query = "insert into gameEvents(timedate, source, event, param1, param2) values(NULL,'"
					+ StringChecker.escapeSQLString(source)
					+ "','"
					+ StringChecker.escapeSQLString(event)
					+ "','"
					+ (firstParam==null?null:StringChecker.escapeSQLString(firstParam.substring(0, Math.min(127, firstParam.length()))))
					+ "','"
					+ (param2==null?null:StringChecker.escapeSQLString(param2.substring(0, Math.min(255, param2.length())))) + "')";

			stmt.execute(query);
			stmt.close();
		} catch (SQLException sqle) {
			logger.warn("Error adding game event: " + event, sqle);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.nio.IEventsAccess#addStatisticsEvent(marauroa.server.game.db.Transaction,
	 *      marauroa.server.game.Statistics.Variables)
	 */
	public void addStatisticsEvent(Transaction trans, Variables var) {
		try {
			Connection connection = trans.getConnection();
			Statement stmt = connection.createStatement();

			String query = "insert into statistics(timedate, bytes_send, bytes_recv, players_login, players_logout, players_timeout, players_online) values(NULL,"
			        + var.get("Bytes send")
			        + ","
			        + var.get("Bytes recv")
			        + ","
			        + var.get("Players login")
			        + ","
			        + var.get("Players logout")
			        + ","
			        + var.get("Players timeout") + "," + var.get("Players online") + ")";
			stmt.execute(query);
			stmt.close();
		} catch (SQLException sqle) {
			logger.warn("Error adding statistics event", sqle);
		}
	}

	protected RPObject loadRPObject(Transaction transaction, int objectid) throws SQLException,
	        IOException {
		Connection connection = transaction.getConnection();

		String query = "select data from rpobject where object_id=" + objectid;
		logger.debug("loadRPObject is executing query " + query);

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);

		if (rs.next()) {
			Blob data = rs.getBlob("data");
			InputStream input = data.getBinaryStream();
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			// set read buffer size
			byte[] rb = new byte[1024];
			int ch = 0;
			// process blob
			while ((ch = input.read(rb)) != -1) {
				output.write(rb, 0, ch);
			}
			byte[] content = output.toByteArray();
			input.close();
			output.close();

			ByteArrayInputStream inStream = new ByteArrayInputStream(content);
			InflaterInputStream szlib = new InflaterInputStream(inStream, new Inflater());
			InputSerializer inser = new InputSerializer(szlib);

			rs.close();
			stmt.close();

			RPObject object = null;

			object = factory.transform((RPObject) inser.readObject(new RPObject()));
			object.put("#db_id", objectid);

			return object;
		}

		rs.close();
		stmt.close();
		return null;
	}

	protected int removeRPObject(Transaction transaction, int objectid) throws SQLException {
		Connection connection = transaction.getConnection();

		String query = "delete from rpobject where object_id=" + objectid;
		logger.debug("removeRPObject is executing query " + query);

		Statement stmt = connection.createStatement();
		stmt.execute(query);

		stmt.close();

		return objectid;
	}

	protected boolean hasRPObject(Transaction transaction, int objectid) throws SQLException {
		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();
		String query = "select count(*) as amount from rpobject where object_id=" + objectid;

		logger.debug("hasRPObject is executing query " + query);

		ResultSet result = stmt.executeQuery(query);

		boolean rpObjectExists = false;

		if (result.next()) {
			if (result.getInt("amount") != 0) {
				rpObjectExists = true;
			}
		}

		result.close();
		stmt.close();

		return rpObjectExists;
	}

	protected int storeRPObject(Transaction transaction, RPObject object) throws IOException,
	        SQLException {
		Connection connection = transaction.getConnection();

		ByteArrayOutputStream array = new ByteArrayOutputStream();
		DeflaterOutputStream out_stream = new DeflaterOutputStream(array);
		OutputSerializer serializer = new OutputSerializer(out_stream);

		try {
			object.writeObject(serializer, DetailLevel.FULL);
			out_stream.close();
		} catch (IOException e) {
			logger.warn("Error while serializing rpobject: " + object, e);
			throw e;
		}

		// setup stream for blob
		ByteArrayInputStream inStream = new ByteArrayInputStream(array.toByteArray());

		int object_id = -1;

		if (object.has("#db_id")) {
			object_id = object.getInt("#db_id");
		}

		String query;

		if (object_id != -1 && hasRPObject(transaction, object_id)) {
			query = "update rpobject set data=? where object_id=" + object_id;
		} else {
			query = "insert into rpobject(object_id,data) values(null,?)";
		}
		logger.debug("storeRPObject is executing query " + query);

		PreparedStatement ps = connection.prepareStatement(query);
		ps.setBinaryStream(1, inStream, inStream.available());
		ps.executeUpdate();
		ps.close();

		// If object is new, get the objectid we gave it.
		if (object_id == -1) {
			Statement stmt = connection.createStatement();
			query = "select LAST_INSERT_ID() as inserted_id";
			logger.debug("storeRPObject is executing query " + query);
			ResultSet result = stmt.executeQuery(query);

			result.next();
			object_id = result.getInt("inserted_id");

			// We alter the original object to add the proper db_id
			object.put("#db_id", object_id);

			result.close();
			stmt.close();
		} else {
			object_id = object.getInt("#db_id");
		}

		return object_id;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see marauroa.server.game.db.IDatabase#getBannedAddresses(marauroa.server.game.db.Transaction)
	 */
	public List<InetAddressMask> getBannedAddresses(Transaction transaction) throws SQLException {
		List<InetAddressMask> permanentBans = new LinkedList<InetAddressMask>();

		/* read ban list from DB */
		Connection connection = transaction.getConnection();
		Statement stmt = connection.createStatement();
		String query = "select address,mask from banlist";
		logger.debug("getBannedAddresses is executing query " + query);

		ResultSet rs = stmt.executeQuery(query);

		permanentBans.clear();
		while (rs.next()) {
			String address = rs.getString("address");
			String mask = rs.getString("mask");
			InetAddressMask iam = new InetAddressMask(address, mask);
			permanentBans.add(iam);
		}

		// free database resources
		rs.close();
		stmt.close();

		return permanentBans;
	}
}
