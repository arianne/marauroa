package marauroa.server.game.db.nio;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.RPObject;
import marauroa.server.game.db.JDBCTransaction;
import marauroa.server.game.db.NoDatabaseConfException;

import org.apache.log4j.Logger;

public class NIOJDBCDatabase implements IPlayerAccess, ICharacterAccess {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(NIOJDBCDatabase.class);

	/** connection info * */
	private Properties connInfo;
	
	protected JDBCSQLHelper sql;

	private JDBCTransaction transaction;
	
	/**
	 * Constructor that connect using a set of Properties.
	 * 
	 * @param connInfo
	 *            a Properties set with the options to create the database.
	 *            Properties should have:
	 *            jdbc_url 
	 *              Contains the URL of the Database we are going to connect to
	 *              jdbc:mysql://127.0.0.1/marauroa
	 *            jdbc_class
	 *              Contains the driver class of the JDBC storage
	 *              com.mysql.jdbc.Driver
	 *            jdbc_user
	 *              Username we are going to use to connect to database
	 *            jdbc_pwd
	 *              Password.
	 */
	protected NIOJDBCDatabase(Properties connInfo) throws NoDatabaseConfException {
		this.connInfo=connInfo;
		
		sql=JDBCSQLHelper.get();		
		transaction=getTransaction();
		
		initialize();
	}

	/**
	 * Creates the database Tables.
	 * Extend it to change the database initialization.
	 */
	protected void initialize() {
		sql.runDBScript(transaction, "marauroa/server/marauroa_init.sql");
	}
	
	private static NIOJDBCDatabase database;
	
	public static NIOJDBCDatabase getDatabase() {
		if(database==null) {
			Configuration conf=null;

			try {
				conf=Configuration.getConfiguration();
			} catch(FileNotFoundException e) {
				logger.fatal("Unable to locate Configuration file: "+Configuration.getConfigurationFile(), e);
			}
			Properties props = new Properties();

			props.put("jdbc_url", conf.get("jdbc_url"));
			props.put("jdbc_class", conf.get("jdbc_class"));
			props.put("jdbc_user", conf.get("jdbc_user"));
			props.put("jdbc_pwd", conf.get("jdbc_pwd"));

			database=new NIOJDBCDatabase(props);
		}
		
		return database;		
	}
	

	/**
	 * This method creates the real connection to database.
	 * 
	 * @param props the database configuration.
	 * @return a Connection to Database
	 * @throws NoDatabaseConfException If there is any kind of problem creating the connection.
	 */
	private Connection createConnection(Properties props) throws NoDatabaseConfException {
		Log4J.startMethod(logger, "createConnection");
		try {
			/* We instantiate now the Driver class */
			try{
				Class.forName((String) props.get("jdbc_class")).newInstance();
			} catch (Exception e) {
				logger.fatal("Unable to create Driver class: "+props.get("jdbc_class"), e);
				throw new NoDatabaseConfException(e);
			}

			Properties connInfo = new Properties();

			connInfo.put("user", props.get("jdbc_user"));
			connInfo.put("password", props.get("jdbc_pwd"));
			connInfo.put("charSet", "UTF-8");

			Connection conn = DriverManager.getConnection((String) props.get("jdbc_url"), connInfo);

			/* This is for MySQL:
			 *  We set auto commit to false, so that transactions work
			 *  We set isolation mode to this so everything works. Google for it 
			 */
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			return conn;
		}  catch (SQLException e) {
			logger.fatal("Unable to create a connection to: "+props.get("jdbc_url"),e);
			throw new NoDatabaseConfException(e);
		} finally {
			Log4J.finishMethod(logger, "createConnection");
		}
	}

	/** 
	 * This method gets a transaction from the connection and check that it is valid.
	 * @return a valid transaction.
	 */
	public JDBCTransaction getTransaction() {
		if (transaction == null || !transaction.isValid()) {
			transaction = new JDBCTransaction(createConnection(connInfo));
		}

		return transaction;
	}	
	
	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.IPlayerAccess#addPlayer(marauroa.server.game.db.JDBCTransaction, java.lang.String, byte[], java.lang.String)
	 */
	public void addPlayer(JDBCTransaction transaction, String username, byte[] password, String email) throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(email)) {
				throw new SQLException("Invalid string username=("+username+") email=("+email+")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();

			String query = "insert into player(id,username,password,email,timedate,status) values("
				+ "NULL,'"
				+ username + "','"
				+ Hash.toHexString(password) + "','"
				+ email + "',"
				+ "NULL," 
				+ "DEFAULT)";
			
			logger.debug("addPlayer is using query: "+query);
			
			stmt.execute(query);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't add player("+username+") to Database", e);
			throw e;
		}
	}
	
	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.IPlayerAccess#removePlayer(marauroa.server.game.db.JDBCTransaction, java.lang.String)
	 */
	public boolean removePlayer(JDBCTransaction transaction, String username) {
		/** TODO: Code this. Right now it is not used anyway.*/
		return false;
	}

	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.IPlayerAccess#hasPlayer(marauroa.server.game.db.JDBCTransaction, java.lang.String)
	 */
	public boolean hasPlayer(JDBCTransaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=("+username+")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			String query = "select count(*) as amount from  player where username like '"+ username + "'";

			logger.debug("hasPlayer is using query: " + query);

			ResultSet result = stmt.executeQuery(query);

			boolean playerExists = false;

			if (result.next() && result.getInt("amount") != 0) {
				playerExists = true; 
			}

			stmt.close();
			return playerExists;
		} catch (SQLException e) {
			logger.error("Can't query for player("+username+")", e);
			throw e;
		}
	}
	
	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.IPlayerAccess#setAccountStatus(marauroa.server.game.db.JDBCTransaction, java.lang.String, java.lang.String)
	 */
	public void setAccountStatus(JDBCTransaction transaction, String username, String status) throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(status)) {
				throw new SQLException("Invalid string username=("+username+")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			
			String query = "update player set status='" + status + "' where username like '" + username + "'";
			logger.debug("setAccountStatus is executing query " + query);
			
			stmt.executeUpdate(query);
			stmt.close();
		} catch (SQLException e) {
			logger.error("Can't udpate account status of player("+username+")",e);
			throw e;
		}		
	}
	
	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.IPlayerAccess#getAccountStatus(marauroa.server.game.db.JDBCTransaction, java.lang.String)
	 */
	public String getAccountStatus(JDBCTransaction transaction, String username) {
		return null;
	}

	private int getDatabasePlayerId(JDBCTransaction trans, String username) throws SQLException {
		Connection connection = trans.getConnection();
		Statement stmt = connection.createStatement();

		if (!StringChecker.validString(username)) {
			throw new SQLException("Invalid string username=("+username+")");
		}

		String query = "select id from player where username like '" + username	+ "'";

		logger.debug("getDatabasePlayerId is executing query " + query);

		ResultSet result = stmt.executeQuery(query);

		int id=-1;

		if (result.next()) {
			id = result.getInt("id");
		}

		result.close();
		stmt.close();

		return id;
	}
	
	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.ICharacterAccess#addCharacter(marauroa.server.game.db.JDBCTransaction, java.lang.String, java.lang.String, marauroa.common.game.RPObject)
	 */
	public void addCharacter(JDBCTransaction transaction, String username, String character, RPObject player) throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=("+username+") character=("+character+")");
			}

				Connection connection = transaction.getConnection();
				Statement stmt = connection.createStatement();

				int id = getDatabasePlayerId(transaction, username);
				int object_id = storeRPObject(transaction, player);

				String query = "insert into characters(player_id,charname,object_id) values("
						+ id + ",'" + character + "'," + object_id + ")";
				stmt.execute(query);
				stmt.close();
			
		} catch (SQLException e) {
			logger.error("Can't add player("+username+") character("+character+")", e);
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.ICharacterAccess#removeCharacter(marauroa.server.game.db.JDBCTransaction, java.lang.String, java.lang.String)
	 */
	public boolean removeCharacter(JDBCTransaction transaction, String username, String character) {
		/** TODO: Code this. Right now it is not used anyway.*/
		return false;
	}
	
	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.ICharacterAccess#hasCharacter(marauroa.server.game.db.JDBCTransaction, java.lang.String, java.lang.String)
	 */
	public boolean hasCharacter(JDBCTransaction transaction, String username, String character) throws SQLException {
		try {
			if (!StringChecker.validString(username) || !StringChecker.validString(character)) {
				throw new SQLException("Invalid string username=("+username+") character=("+character+")");
			}

			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			String query = "select count(*) as amount from  player,characters where "
				    + "username like '"	+ username
					+ "' and charname like '" + character
					+ "' and player.id=characters.player_id";

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
				logger.error("Can't query for player("+username+") character("+character+")", e);
				throw e;
			} 
	}
	
	/* (non-Javadoc)
	 * @see marauroa.server.game.db.nio.ICharacterAccess#getCharacters(marauroa.server.game.db.JDBCTransaction, java.lang.String)
	 */
	public List<String> getCharacters(JDBCTransaction transaction, String username) throws SQLException {
		try {
			if (!StringChecker.validString(username)) {
				throw new SQLException("Invalid string username=("+username+")");
			}
			
			int id = getDatabasePlayerId(transaction, username);
			if(id==-1) {
				/** This should never happen as we should check previously that player exists... */
				throw new SQLException("Unable to find player("+username+")");
			}
			
			Connection connection = transaction.getConnection();
			Statement stmt = connection.createStatement();
			String query = "select charname from characters where player_id="+ id;

			logger.debug("getCharacterList is executing query " + query);

			ResultSet charactersSet = stmt.executeQuery(query);
			List<String> list = new LinkedList<String>();

			while (charactersSet.next()) {
				list.add(charactersSet.getString("charname"));
			}

			charactersSet.close();

			stmt.close();

			Log4J.finishMethod(logger, "getCharacterList");

			return list;
		} catch (SQLException e) {
			logger.error("Can't query for player("+username+")", e);
			throw e;
		}
	}

	
	private int storeRPObject(JDBCTransaction transaction2, RPObject player) {
		// TODO Auto-generated method stub
		return 0;
	}}
