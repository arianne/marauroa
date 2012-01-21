package marauroa.server.game.container;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.Utility;
import marauroa.common.crypto.RSAKey;
import marauroa.common.net.message.MessageS2CLoginNACK.Reasons;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.LoginEventDAO;

/**
 * This class stores the information needed to allow a secure login. Once
 * login is completed the information is cleared.
 */
public class SecuredLoginInfo {
	private static Logger logger = Log4J.getLogger(SecuredLoginInfo.class);

	/** A long array of bytes that represent the Hash of a random value. */
	public byte[] serverNonce;

	/** A long array of bytes that represent a random value. */
	public byte[] clientNonce;

	/** A long byte array that represent the hash of the client Nonce field */
	public byte[] clientNonceHash;

	/** Username of the player */
	public String username;

	/**
	 * An array that represent the hash of the password xor ClientNonce xor
	 * ServerNonce.
	 */
	public byte[] password;

	/** The server RSA key. */
	public RSAKey key;

	/** client ip address */
	public InetAddress address;

	/** seed identifying the client */
	public String seed;

	/** reason why a login failed */
	public Reasons reason;

	/** is the password encrypted */
	public boolean usingSecureChannel = true;

	/**
	 * Constructor
	 *
	 * @param key
	 *            the server private key
	 * @param clientNonceHash
	 *            the client hash
	 * @param serverNonce
	 *            the server random bigint
	 * @param address client ip address
	 */
	public SecuredLoginInfo(RSAKey key, byte[] clientNonceHash, byte[] serverNonce, InetAddress address) {
		this.key = key;
		this.clientNonceHash = Utility.copy(clientNonceHash);
		this.serverNonce = Utility.copy(serverNonce);
		this.address = address;
	}

	/**
	 * Constructor
	 *
	 * @param address client ip address
	 */
	public SecuredLoginInfo(InetAddress address) {
		this.address = address;
	}

	/**
	 * Verify that a player is whom he/she says it is.
	 *
	 * @return true if it is correct: username and password matches.
	 * @throws SQLException
	 *             if there is any database problem.
	 */
	public boolean verify() throws SQLException {
		return DAORegister.get().get(AccountDAO.class).verify(this);
	}

	/**
	 * Add a login event to database each time player login, even if it
	 * fails.
	 *
	 * @param address the IP address that originated the request.
	 * @param result 0 failed password, 1 successful login, 2 banned, 3 inactive, 4 blocked, 5 merged
	 * @throws SQLException if there is any database problem.
	 */
	public void addLoginEvent(InetAddress address, int result) throws SQLException {
		String service = null;
		try {
			Configuration conf = Configuration.getConfiguration();
			if (conf.has("server_service")) {
				service = conf.get("server_service");
			} else {
				service = conf.get("server_typeGame");
			}
		} catch (IOException e) {
			logger.error(e, e);
		}
		DAORegister.get().get(LoginEventDAO.class).addLoginEvent(username, address, service, seed, result);
	}

	/**
	 * counts the number of connections from this ip-address
	 *
	 * @param playerContainer PlayerEntryContainer
	 * @return number of active connections
	 */
	public int countConnectionsFromSameIPAddress(PlayerEntryContainer playerContainer) {
		if (address == null) {
			return 0;
		}
		int counter = 0;
		for (PlayerEntry playerEntry : playerContainer) {
			try {
				if ((playerEntry.getAddress() != null) && address.getHostAddress().equals(playerEntry.getAddress().getHostAddress())) {
					counter++;
				}
			} catch (NullPointerException e) {
				logger.error(address);
				logger.error(address.getHostAddress());
				logger.error(playerEntry);
				logger.error(playerEntry);
				logger.error(playerEntry.getAddress());
				logger.error(e, e);
			}
		}
		return counter;
	}

	/**
	 * Returns true if an account is temporarily blocked due to too many
	 * tries in the defined time frame.
	 *
	 * @return true if an account is temporarily blocked due to too many
	 *         tries in the defined time frame.
	 * @throws SQLException
	 *             if there is any database problem.
	 */
	public boolean isBlocked() throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		boolean res = true;
		try {
			LoginEventDAO loginEventDAO = DAORegister.get().get(LoginEventDAO.class);
			res = loginEventDAO.isAccountBlocked(transaction, username)
				|| loginEventDAO.isAddressBlocked(transaction, address.getHostAddress());

			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			TransactionPool.get().rollback(transaction);
			logger.error(e, e);
		}
		return res;
	}

	/**
	 * Returns a string indicating the status of the account.
	 * It can be: <ul>
	 * <li>active
	 * <li>inactive
	 * <li>banned
	 * </ul>
	 * @return a string indicating the status of the account.
	 * @throws SQLException
	 */
	public String getStatus() throws SQLException {
		DBTransaction transaction = TransactionPool.get().beginWork();
		String res = null;
		try {
			if (DAORegister.get().get(AccountDAO.class).hasPlayer(transaction, username)) {
				res = DAORegister.get().get(AccountDAO.class).getAccountBanMessage(transaction, username);
			}
			TransactionPool.get().commit(transaction);
		} catch (SQLException e) {
			TransactionPool.get().rollback(transaction);
		}
		return res;
	}

	/**
	 * are we using a secure channel so that we can skip our own RSA encryption and replay protection
	 *
	 * @return usingSecureChannel
	 */
	public boolean isUsingSecureChannel() {
		return usingSecureChannel;
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "SecuredLoginInfo [username=" + username + ", address="
				+ address + ", seed=" + seed + ", reason=" + reason + "]";
	}

}