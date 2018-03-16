/***************************************************************************
 *                   (C) Copyright 2007-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.container;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.TimeoutConf;
import marauroa.common.game.RPObject;
import marauroa.common.net.Channel;
import marauroa.common.net.NetConst;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.TransferContent;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.LoginEventDAO;
import marauroa.server.game.dbcommand.StoreCharacterCommand;

/**
 * This class represent a player on game. It handles all the business glue that
 * it is needed by the server.
 *
 * @author miguel
 */
public class PlayerEntry {
	private static Logger logger = Log4J.getLogger(PlayerEntry.class);

	/**
	 * We record when this player entry was created to remove players that don't
	 * complete login stage but that keep connected.
	 */
	public long creationTime;

	/** The state in which this player is */
	public ClientState state;

	/** The runtime clientid */
	public int clientid;

	/** The client associated SocketChannel */
	public Channel channel;

	/**
	 * The login Info. It is created after the first login message and destroyed
	 * after the login is finished.
	 */
	public SecuredLoginInfo loginInformations;

	/** The name of the player */
	public String username;

	/** The name of the chosen character */
	public String character;

	/** The object of the player */
	public RPObject object;

	/** client locale */
	public Locale locale = Locale.ENGLISH;

	/**
	 * We need to control if player is active because sometimes server changes its IP
	 * and we are not able to realize about this at server side, so all the clients are left there
	 * until the TCP stack determine a timeout that can be a long time.
	 */
	public long activityTimestamp;

	/**
	 * We define how many milliseconds has to be elapsed until we consider a player has timeout.
	 */
	private static final long TIMEOUT_IN_GAME_MILLISECONDS = 30 * 1000;

	/**
	 * We need a longer timeout pre-game because players might want to create a character here,
	 * there is no keep-alive yet.
	 */
	private static final long TIMEOUT_PRE_GAME_MILLISECONDS = 10 * 60 * 1000;

	/**
	 * A counter to detect dropped packets or bad order at client side. We
	 * enumerate each perception so client can know in which order it is
	 * expected to apply them. When using TCP there is no problem as delivery is
	 * guaranteed.
	 */
	public int perceptionCounter;

	/** It is true if client notified us that it got out of sync */
	public boolean requestedSync;

	/** Contains the content that is going to be transfered to client */
	public List<TransferContent> contentToTransfer;

	/** version of protocol this client speaks */
	private int protocolVersion = NetConst.NETWORK_PROTOCOL_VERSION;

	/** grant a longer timeout during login */
	private boolean gotKeepAliveInGameState = false;

	/** the number of characters owned by this account */
	public int characterCounter;

	/** should this player entry time out? set to false if another timeout mechanism is in place */
	private boolean checkTimeout = true;


	/**
	 * Constructor
	 *
	 * @param channel
	 *            the socket channel
	 */
	public PlayerEntry(Channel channel) {
		this.channel = channel;

		clientid = Message.CLIENTID_INVALID;
		state = ClientState.CONNECTION_ACCEPTED;
		loginInformations = null;
		username = null;
		character = null;
		object = null;
		perceptionCounter = 0;
		/*
		 * We set this to true so that RP Manager will send a sync perception to
		 * player as soon as possible.
		 */
		requestedSync = true;
		contentToTransfer = Collections.synchronizedList(new LinkedList<TransferContent>());

		creationTime = System.currentTimeMillis();
		activityTimestamp=creationTime;
	}

	/**
	 * Return the inet address of this PlayerEntry.
	 *
	 * @return the inet address of this PlayerEntry.
	 */
	public InetAddress getAddress() {
		return channel.getInetSocketAddress().getAddress();
	}


	/**
	 * Return the inet address of this PlayerEntry.
	 *
	 * @return the inet address of this PlayerEntry.
	 */
	public InetSocketAddress getInetSocketAddress() {
		return channel.getInetSocketAddress();
	}

	/**
	 * Returns true when nothing has been received from client in TIMEOUT_SECONDS.
	 * Note that client sends confirmations to perceptions, so this mean that client is
	 * for whatever reason not working.
	 *
	 * @return  true when nothing has been received from client in TIMEOUT_SECONDS.
	 */
	public boolean isTimeout() {
		if (!checkTimeout ) {
			return false;
		}
		if (state==ClientState.GAME_BEGIN) {
			if (gotKeepAliveInGameState) {
				return (System.currentTimeMillis()-activityTimestamp)>TIMEOUT_IN_GAME_MILLISECONDS;
			} else {
				return (System.currentTimeMillis()-activityTimestamp)>TIMEOUT_IN_GAME_MILLISECONDS * 4;
			}
		} else {
			return (System.currentTimeMillis()-activityTimestamp)>TIMEOUT_PRE_GAME_MILLISECONDS;
		}
	}

	/**
	 * disables the timeout check (because it is done elsewhere)
	 */
	public void disableTimeout() {
		checkTimeout = false;
	}

	/**
	 * Refresh player timeout timestamp.
	 * This method is invoked when a new message arrives from client.
	 */
	public void update() {
		activityTimestamp=System.currentTimeMillis();
		if (state==ClientState.GAME_BEGIN) {
			gotKeepAliveInGameState  = true;
		}
	}

	/**
	 * Returns the next perception timestamp.
	 *
	 * @return the next perception timestamp
	 */
	public int getPerceptionTimestamp() {
		return perceptionCounter++;
	}

	/**
	 * returns the current perception timestamp
	 *
	 * @return the current perception timestamp
	 */
	public int getThisPerceptionTimestamp() {
		return perceptionCounter;
	}

	/**
	 * Clears the specified content to be transfered
	 *
	 * @param content TransferContent to remove from the queue
	 */
	public void removeContent(TransferContent content) {
		if (!contentToTransfer.remove(content)) {
			logger.warn("Trying to clean unknown content: " + content);
		}
	}

	/**
	 * Returns the named content or returns null if it is not found.
	 *
	 * @param name
	 *            name of the content to find
	 * @return the content or null if it is not found.
	 */
	public TransferContent getContent(String name) {
		if (contentToTransfer == null) {
			logger.warn("contentToTransfer is null.");
			return null;
		}

		synchronized(contentToTransfer) {
			for (TransferContent item : contentToTransfer) {
				if (item.name.equals(name)) {
					return item;
				}
			}
		}

		logger.warn("content " + name + " not found.");
		return null;
	}

	/**
	 * This method stores an object at database backend
	 *
	 * @param player
	 *            the object to store
	 * @throws SQLException in case of an database error
	 * @throws IOException in case of an input/output error
	 */
	public void storeRPObject(RPObject player) throws SQLException, IOException {
		// And update the entry
		object = player;

		// We store the object in the database
		DBCommand command = new StoreCharacterCommand(username, character, player);
		DBCommandQueue.get().enqueue(command);
	}

	/**
	 * This method query database to check if the player with username given by
	 * the entry has a character with the name passed as argument.
	 *
	 * @param charname
	 *            The name we are querying for.
	 * @return true if it is found or false otherwise.
	 * @throws SQLException
	 *             If there is a Database exception.
	 */
	public boolean hasCharacter(String charname) throws SQLException {
		return DAORegister.get().get(CharacterDAO.class).hasCharacter(username, charname);
	}

	/**
	 * Allows to ban this player account
	 * @throws SQLException
	 */
	public void ban() throws SQLException {
		DAORegister.get().get(AccountDAO.class).setAccountStatus(username, "banned");
	}

	/**
	 * sets the RPObject for this entry.
	 *
	 * @param object RPObject
	 */
	public void setObject(RPObject object) {
		this.object = object;
	}

	/**
	 * This method returns a list of all the characters available for this
	 * player
	 *
	 * @return a list containing all the usable characters
	 * @throws SQLException
	 *             if there is any database problem.
	 */
	public List<String> getCharacters() throws SQLException {
		return DAORegister.get().get(CharacterDAO.class).getCharacters(username);
	}

	/**
	 * This method forces an update on the next perception sending.
	 */
	public void requestSync() {
		requestedSync = true;
	}

	/**
	 * Return a list of the previous login attempts.
	 *
	 * @return a list of the previous login attempts.
	 * @throws SQLException
	 */
	public List<String> getPreviousLogins() throws SQLException {
		return DAORegister.get().get(LoginEventDAO.class).getLoginEvents(username, 1);
	}

	/**
	 * This method tag this entry as removable if there is more than
	 * UNCOMPLETED_LOGIN_TIMEOUT milliseconds since the creation time of the
	 * entry and the actual time and the entry has not completed the login
	 * stage.
	 *
	 * @return true, if it is removable, false otherwise
	 */
	boolean isRemovable() {
		/*
		 * Add logged players that didn't choose a character or that have not
		 * even login yet.
		 */
		boolean isInOKState = (state == ClientState.GAME_BEGIN);
		return !isInOKState
		        && System.currentTimeMillis() - creationTime > TimeoutConf.UNCOMPLETED_LOGIN_TIMEOUT;
	}

	/**
	 * gets the version of the protocol this clients speaks
	 *
	 * @return protocolVersion
	 */
	public int getProtocolVersion() {
		return protocolVersion;
	}


	/**
	 * sets the protocol version
	 *
	 * @param protocolVersion protocol version
	 */
	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = Math.min(NetConst.NETWORK_PROTOCOL_VERSION, protocolVersion);
	}


	@Override
	public String toString() {
		StringBuilder os = new StringBuilder("PlayerEntry");
		os.append("[clientid=" + clientid + "]");
		os.append("[channel=" + channel + "]");
		os.append("[state=" + state + "]");
		os.append("[username=" + username + "]");
		os.append("[character=" + character + "]");
		os.append("[object defined=" + (object != null) + "]");

		return os.toString();
	}

}
