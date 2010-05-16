/* $Id: SecuredLoginEventHandler.java,v 1.2 2010/05/16 20:48:28 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.messagehandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.message.MessageC2SLoginSendNonceNamePasswordAndSeed;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.common.net.message.MessageS2CLoginACK;
import marauroa.common.net.message.MessageS2CLoginMessageNACK;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntry.SecuredLoginInfo;

import org.apache.log4j.Logger;

/**
 * Complete the login stage. It will either success and
 * game continue or fail and resources for this player
 * are freed.
 */
class SecuredLoginEventHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);


	/**
	 * This last method completes the login process.
	 *
	 * @param msg
	 *            the final message the contains the encrypted password.
	 */
	@Override
	public void process(Message msg) {
		try {
			int clientid = msg.getClientID();
			PlayerEntry entry = playerContainer.get(clientid);
			
			// verify event
			if (!isValidEvent(msg, entry, ClientState.CONNECTION_ACCEPTED)) {
				return;
			}

			SecuredLoginInfo info = entry.loginInformations;

			if (msg instanceof MessageC2SLoginSendNonceNameAndPassword) {
				MessageC2SLoginSendNonceNameAndPassword msgLogin = (MessageC2SLoginSendNonceNameAndPassword) msg;
				info.clientNonce = msgLogin.getHash();
				info.username = msgLogin.getUsername();
				info.password = msgLogin.getPassword();
			} else {
				MessageC2SLoginSendNonceNamePasswordAndSeed msgLogin = (MessageC2SLoginSendNonceNamePasswordAndSeed) msg;
				info.clientNonce = msgLogin.getHash();
				info.username = msgLogin.getUsername();
				info.password = msgLogin.getPassword();
				info.seed = decode(info, msgLogin.getSeed());
			}

			/*
			 * We check that player didn't failed too many time the login, if it
			 * did, we reject the login request until the block pass.
			 */
			if (info.isBlocked()) {
				logger.debug("Blocked account for player " + info.username + " and/or address " + info.address);

				/* Send player the Login NACK message */
				MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(),
				        MessageS2CLoginNACK.Reasons.TOO_MANY_TRIES);

				netMan.sendMessage(msgLoginNACK);
				
				/*
				 * Disconnect player of server.
				 */
				netMan.disconnectClient(msg.getSocketChannel());

				return;
			}
			
			/*
			 * We verify the username and the password to make sure player is
			 * who he/she says he/she is.
			 */
			if (!info.verify()) {
				/*
				 * If the verification fails we send player a NACK and record
				 * the event
				 */
				logger.debug("Incorrect username/password for player " + info.username);
				stats.add("Players invalid login", 1);
				info.addLoginEvent(msg.getAddress(), false);

				/* Send player the Login NACK message */
				MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(),
				        MessageS2CLoginNACK.Reasons.USERNAME_WRONG);

				netMan.sendMessage(msgLoginNACK);
				playerContainer.remove(clientid);
				return;
			}

			/*
			 * We check now the account is not banned or inactive.
			 */
			String accountStatus = info.getStatus();
			if (accountStatus != null) {
				logger.info("Banned/Inactive account for player " + info.username);

				/* Send player the Login NACK message */
				MessageS2CLoginMessageNACK msgLoginMessageNACK = new MessageS2CLoginMessageNACK(msg.getSocketChannel(), accountStatus);
				netMan.sendMessage(msgLoginMessageNACK);

				/*
				 * Disconnect player of server.
				 */
				netMan.disconnectClient(msg.getSocketChannel());

				return;
			}

			/* Now we count the number of connections from this ip-address */
			int count = info.countConnectionsFromSameIPAddress(playerContainer);
			Configuration conf = Configuration.getConfiguration();
			int limit = conf.getInt("parallel_connection_limit", TimeoutConf.PARALLEL_CONNECTION_LIMIT);
			if (count > limit) {
				String whiteList = "," + conf.get("ip_whitelist", "127.0.0.1") + ",";
				if (whiteList.indexOf("," + info.address + ",") < 0) {
					logger.info("to many parallel connections from " + info.address + " rejecting login of " + info.username);

					/* Send player the Login NACK message */
					MessageS2CLoginMessageNACK msgLoginMessageNACK = new MessageS2CLoginMessageNACK(msg.getSocketChannel(),
						"There are too many connections from your ip-address.\nPlease contact /support, if you are at a conference or something similar.");
					netMan.sendMessage(msgLoginMessageNACK);

					// Disconnect player of server.
					netMan.disconnectClient(msg.getSocketChannel());
					return;
				}
			}
			

			/* Now we check if this player is previously logged in */
			PlayerEntry existing = playerContainer.get(info.username);

			if (existing != null) {
				/*
				 * Warning: Player is alreay logged in. So we proceed to store it
				 * and remove from game.
				 */
				logger.warn("Client(" + msg.getAddress().toString() + ") trying to login twice");

				if (existing.state == ClientState.GAME_BEGIN) {
					/* We restore back the character to the world */
					
					RPObject object = existing.object;

					playerContainer.getLock().requestWriteLock();
					rpMan.onTimeout(object);
					playerContainer.getLock().releaseLock();

					existing.storeRPObject(object);
				}
				logger.debug("Disconnecting PREVIOUS " + existing.channel + " with " + existing);

				/*
				 * Disconnect player of server.
				 */
				netMan.disconnectClient(existing.channel);

				/*
				 * HACK: Remove the entry now so we can continue.
				 */				
				playerContainer.remove(existing.clientid);
			}

			logger.debug("Correct username/password");

			/* Correct: The login is correct */
			entry.username = info.username;

			/* Obtain previous logins attemps */
			List<String> previousLogins = entry.getPreviousLogins();

			/* Now we store at database the login event */
			info.addLoginEvent(msg.getAddress(), true);

			/* We clean the login information as it is not longer useful. */
			entry.loginInformations = null;

			stats.add("Players login", 1);

			/* Send player the Login ACK message */
			MessageS2CLoginACK msgLoginACK = new MessageS2CLoginACK(msg.getSocketChannel(),
			        previousLogins);
			msgLoginACK.setClientID(clientid);
			netMan.sendMessage(msgLoginACK);

			/* Send player the ServerInfo */
			MessageS2CServerInfo msgServerInfo = new MessageS2CServerInfo(msg.getSocketChannel(),
			        ServerInfo.get());
			msgServerInfo.setClientID(clientid);
			netMan.sendMessage(msgServerInfo);

			/* Build player character list and send it to client */
			String[] characters = entry.getCharacters().toArray(new String[entry.getCharacters().size()]);
			MessageS2CCharacterList msgCharacters = new MessageS2CCharacterList(msg
			        .getSocketChannel(), characters);
			msgCharacters.setClientID(clientid);
			netMan.sendMessage(msgCharacters);

			entry.state = ClientState.LOGIN_COMPLETE;
		} catch (IOException e) {
			logger.error("error while processing SecuredLoginEvent", e);
		} catch (RuntimeException e) {
			logger.error("error while processing SecuredLoginEvent", e);
		} catch (SQLException e) {
			logger.error("error while processing SecuredLoginEvent", e);
		}
	}

	private String decode(SecuredLoginInfo info, byte[] data) {
		byte[] b1 = info.key.decodeByteArray(data);
		byte[] b2 = Hash.xor(info.clientNonce, info.serverNonce);
		if (b2 == null) {
			logger.debug("B2 is null");
			return null;
		}

		byte[] result = Hash.xor(b1, b2);
		try {
			return new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e, e);
			return null;
		}
	}



	/**
	 * This class stores Server information like
	 * <ul>
	 * <li>Type of game
	 * <li>Server name
	 * <li>Server version number
	 * <li>Server contact information.
	 * </ul>
	 */
	private static class ServerInfo {
		private static Logger infoLogger = Logger.getLogger(ServerInfo.class);

		private static Configuration config;
		static {
			try {
				config = Configuration.getConfiguration();
				// just check if mandatory properties are set
				config.get("server_typeGame");
				config.get("server_name");
				config.get("server_version");
				config.get("server_contact");
			} catch (Exception e) {
			    infoLogger.error("ERROR: Unable to load Server info", e);
			}
		}

		/** 
		 * This method builds a String[] from the properties used in Server Info
		 *
		 * @return Server Info
		 */
		public static String[] get() {
			List<String> l_result = new ArrayList<String>();

			Enumeration<?> props = config.propertyNames();
			while (props.hasMoreElements()) {
				String prop_name = String.valueOf(props.nextElement());
				if (prop_name.startsWith("server_")) {
					l_result.add(config.get(prop_name));
				}
			}
			String[] result = new String[l_result.size()];
			return l_result.toArray(result);
		}
	}
}
