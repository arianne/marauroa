package marauroa.server.game.container;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.List;

import marauroa.common.Log4J;
import marauroa.common.crypto.RSAKey;
import marauroa.common.game.RPObject;
import marauroa.common.net.Message;
import marauroa.common.net.TransferContent;
import marauroa.server.game.NoSuchCharacterException;
import marauroa.server.game.NoSuchPlayerException;
import marauroa.server.game.PlayerNotFoundException;
import marauroa.server.game.db.GenericDatabaseException;
import marauroa.server.game.db.IPlayerDatabase;
import marauroa.server.game.db.PlayerDatabaseFactory;
import marauroa.server.game.db.Transaction;

import org.apache.log4j.Logger;

public class PlayerEntry {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(PlayerEntry.class);

	/** A object representing the database */
	protected static IPlayerDatabase playerDatabase;
    protected static Transaction transaction;
    
    public static void initDatabase() throws GenericDatabaseException {
    		playerDatabase=PlayerDatabaseFactory.getDatabase();
    		transaction=playerDatabase.getTransaction();
    }

    /** 
	 * This class store the information needed to allow a secure login.
	 * Once login is completed the information is cleared.
	 * @author miguel
	 */
	static public class SecuredLoginInfo {
		public byte[] clientNonceHash;
		public byte[] serverNonce;
		public byte[] clientNonce;
		public String username;
		public byte[] password;
		public RSAKey key;

		public SecuredLoginInfo(RSAKey key, byte[] clientNonce, byte[] serverNonce) {
			this.key = key;
			this.clientNonce=clientNonce;
			this.serverNonce=serverNonce;
		}

		public boolean verify() throws GenericDatabaseException {
			return playerDatabase.verifyAccount(transaction, this);
		}

		public void addLoginEvent(InetSocketAddress address, boolean loginResult) throws GenericDatabaseException {
			Log4J.startMethod(logger, "addLoginEvent");
			try {
				transaction.begin();
				playerDatabase.addLoginEvent(transaction, username, address, loginResult);
				transaction.commit();
			} catch (Exception e) {
				transaction.rollback();
				throw new GenericDatabaseException(e);
			} finally {
				Log4J.finishMethod(logger, "addLoginEvent");
			}		}
	}

	/** The state in which this player is */
	public ClientState state;

	/** The runtime clientid */
	public int clientid;
	
	/** The client associated SocketChannel */
	public SocketChannel channel;

	/**
	 * The login Info. It is created after the first login message and
	 * destroyed after the login is finished.
	 */
	public SecuredLoginInfo loginInformations;

	/** The name of the player */
	public String username;

	/** The name of the choosen character */
	public String character;

	/** The object of the player */
	public RPObject object;

	/** A counter to detect dropped packets or bad order at client side */
	public int perception_counter;
	
	/** It is true if client notified us that it got out of sync */
	public boolean requestedSync;

	/** Contains the content that is going to be transfered to client */
	public List<TransferContent> contentToTransfer;
	
	
	public PlayerEntry(SocketChannel channel) {
		this.channel=channel;
		
		clientid=Message.CLIENTID_INVALID;
		state=ClientState.CONNECTION_ACCEPTED;
		loginInformations=null;
		username=null;
		character=null;
		object=null;
		perception_counter=0;
		requestedSync=false;
		contentToTransfer=null;
	}	  

	/** Returns the next perception timestamp. */
	public int getPerceptionTimestamp() {
		return perception_counter++;
	}

	/** Clears the contents to be transfered */
	public void clearContent() {
		contentToTransfer = null;
	}

	public TransferContent getContent(String name) {
		if (contentToTransfer == null) {
			return null;
		}

		for (TransferContent item : contentToTransfer) {
			if (item.name.equals(name)) {
				return item;
			}
		}

		return null;
	}

	/**
	 * This method stores an object at database backend
	 * @param object the object to store
	 */
	public void storeRPObject(RPObject player) throws NoSuchPlayerException, NoSuchCharacterException, GenericDatabaseException {
		Log4J.startMethod(logger, "setRPObject");
		try {
				transaction.begin();

				/* We store the object in the database */
				playerDatabase.setRPObject(transaction, username, character, player);
				
				/* And update the entry */
				object=player;
				
				transaction.commit();

		} catch (Exception e) {
			transaction.rollback();
			logger.warn("Error storing RPObject", e);
			throw new GenericDatabaseException(e);
		} finally {
			Log4J.finishMethod(logger, "setRPObject");
		}	
	}

	/**
	 * This method query database to check if the player with username given by the entry
	 * has a character with the name passed as argument.
	 * 
	 * @param character The name we are querying for.
	 * @return true if it is found or false otherwise.
	 * @throws Exception If there is a Database exception.
	 */
	public boolean hasCharacter(String character) throws Exception {
		return playerDatabase.hasCharacter(transaction, username, character);
	}

	/**
	 * This method loads the object pointed by username and character from database
	 * and assign already it to the entry.
	 * @return the loaded object
	 * @throws Exception if the load fails.
	 */
	public RPObject loadRPObject() throws Exception {
		object = playerDatabase.getRPObject(transaction,username, character);
		return object;
	}

	public String[] getCharacters() throws PlayerNotFoundException, GenericDatabaseException {
		return playerDatabase.getCharactersList(transaction, username);
	}	
}
