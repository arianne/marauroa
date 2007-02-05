/* $Id: GameServerManager.java,v 1.41 2007/02/05 18:07:39 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.PropertyNotFoundException;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAKey;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.Message;
import marauroa.common.net.MessageC2SAction;
import marauroa.common.net.MessageC2SChooseCharacter;
import marauroa.common.net.MessageC2SCreateAccount;
import marauroa.common.net.MessageC2SLoginRequestKey;
import marauroa.common.net.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.MessageC2SLoginSendPromise;
import marauroa.common.net.MessageC2SLogout;
import marauroa.common.net.MessageC2SOutOfSync;
import marauroa.common.net.MessageC2STransferACK;
import marauroa.common.net.MessageS2CCharacterList;
import marauroa.common.net.MessageS2CChooseCharacterACK;
import marauroa.common.net.MessageS2CChooseCharacterNACK;
import marauroa.common.net.MessageS2CCreateAccountACK;
import marauroa.common.net.MessageS2CCreateAccountNACK;
import marauroa.common.net.MessageS2CLoginACK;
import marauroa.common.net.MessageS2CLoginNACK;
import marauroa.common.net.MessageS2CLoginSendKey;
import marauroa.common.net.MessageS2CLoginSendNonce;
import marauroa.common.net.MessageS2CLogoutACK;
import marauroa.common.net.MessageS2CLogoutNACK;
import marauroa.common.net.MessageS2CServerInfo;
import marauroa.common.net.MessageS2CTransfer;
import marauroa.common.net.TransferContent;
import marauroa.server.createaccount.Result;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.container.PlayerEntry.SecuredLoginInfo;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.net.IDisconnectedListener;
import marauroa.server.net.INetworkServerManager;

import org.apache.log4j.Logger;

/**
 * The GameServerManager is a active entity of the marauroa.game package, it is
 * in charge of processing all the messages and modify PlayerEntry Container
 * accordingly.
 */
public final class GameServerManager extends Thread implements IDisconnectedListener {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(GameServerManager.class);

	/** We need network server manager to be able to send messages */
	private INetworkServerManager netMan;

	/** We need rp manager to run the messages and actions from players */
	private RPServerManager rpMan;

	/** The playerContainer handles all the player management */
	private PlayerEntryContainer playerContainer;

	/** Statistics about actions runs */
	private Statistics stats;

	/** The server RSA Key */
	private RSAKey key;

	/** The thread will be running while keepRunning is true */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isfinished;

	/**
	 * Constructor that initialize also the RPManager
	 * 
	 * @param netMan a NetworkServerManager instance.
	 */
	public GameServerManager(RSAKey key, INetworkServerManager netMan, RPServerManager rpMan) throws Exception {
		super("GameServerManager");
		keepRunning = true;
		isfinished=false;
		
		this.key = key;
		this.netMan = netMan;
		this.rpMan = rpMan;
		
		netMan.registerDisconnectedListener(this);
		
		playerContainer = PlayerEntryContainer.getContainer();
		stats = Statistics.getStatistics();
	}

	/** 
	 * This method request the active object to finish its execution and store all
	 * the players back to database.
	 */
	public void finish() {
		rpMan.finish();
		keepRunning = false;
		while (isfinished == false) {
			Thread.yield();
		}

		/* We store all the players when we are requested to exit */
		storeConnectedPlayers();
	}

	private void storeConnectedPlayers() {
		for(PlayerEntry entry: playerContainer) {
			rpMan.disconnect(entry);
		}
	}

	@Override
	public void run() {
		try {
			while (keepRunning) {
				Message msg = netMan.getMessage(TimeoutConf.GAMESERVER_MESSAGE_GET_TIMEOUT);

				if (msg != null) {
					playerContainer.getLock().requestWriteLock();
					switch (msg.getType()) {
					case C2S_LOGIN_REQUESTKEY:
						logger.debug("Processing C2S Login Request Key Message");
						processLoginRequestKey(msg);
						break;
					case C2S_LOGIN_SENDPROMISE:
						logger.debug("Processing C2S Login Send Promise Message");
						processLoginSendPromise(msg);
						break;
					case C2S_LOGIN_SENDNONCENAMEANDPASSWORD:
						logger.debug("Processing C2S Secured Login Message");
						processSecuredLoginEvent(msg);
						break;
					case C2S_CHOOSECHARACTER:
						logger.debug("Processing C2S Choose Character Message");
						processChooseCharacterEvent((MessageC2SChooseCharacter) msg);
						break;
					case C2S_LOGOUT:
						logger.debug("Processing C2S Logout Message");
						processLogoutEvent((MessageC2SLogout) msg);
						break;
					case C2S_ACTION:
						logger.debug("Processing C2S Action Message");
						processActionEvent((MessageC2SAction) msg);
						break;
					case C2S_OUTOFSYNC:
						logger.debug("Processing C2S Out Of Sync Message");
						processOutOfSyncEvent((MessageC2SOutOfSync) msg);
						break;
					case C2S_TRANSFER_ACK:
						logger.debug("Processing C2S Transfer ACK Message");
						processTransferACK((MessageC2STransferACK) msg);
						break;
					case C2S_CREATEACCOUNT:
						logger.debug("Processing C2S Create Account Message");
						processCreateAccount((MessageC2SCreateAccount) msg);
						break;
					default:
						logger.debug("Unknown Message[" + msg.getType() + "]");
						break;
					}
					playerContainer.getLock().releaseLock();
				}
				stats.set("Players online", playerContainer.size());
			}
		} catch (Throwable e) {
			logger.fatal("Unhandled exception, server will shut down.", e);
		}

		isfinished = true;
	}

	/**
	 * This checks if the message is valid to trigger the event. The player has
	 * to:
	 * <ul>
	 * <li>Be known to the Server (logged in)</li>
	 * <li>Completed the login procedure</li>
	 * <li>Must have the correct IP<->clientid relation </li>
	 * </ul>
	 * 
	 * @param msg
	 *            the message to check
	 * @return true, the event is valid, else false
	 */
	private boolean isValidEvent(Message msg, PlayerEntry entry, ClientState state) {
		if(entry==null) {
			/* Error: Player didn't login. */
			logger.warn("Client(" + msg.getAddress()+ ") has not login yet");
			return false;
		}
				
		if (entry.state != state) {
			/*
			 * Error: Player has not completed login yet, or he/she has logout
			 * already.
			 */
			logger.warn("Client(" + msg.getAddress()+ ") is not in the required state (" + state + ")");
			return false;
		}
		
		if (entry.channel!=msg.getSocketChannel()) {
			/* Info: Player is using a different socket to communicate with server. */
			logger.info("Client(" + msg.getAddress()+ ") has not correct IP<->clientid relation");
			return false;
		}
		
		return true;
	}

	/**
	 * This methods handles the logic when a Choose Character message is recieved from
	 * client, checking the message and choosing the character.
	 * This method will send also the reply ACK or NACK to the message.
	 * @param msg The ChooseCharacter message
	 */
	private void processChooseCharacterEvent(MessageC2SChooseCharacter msg) {
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry=playerContainer.get(clientid);
			
			/* verify event so that we can trust that it comes from our player and
			   that it has completed the login stage. */
			if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE)) {
				return;
			}

			/* We check if this account has such player. */
			if (entry.hasCharacter(msg.getCharacter())) {
				/* We set the character in the entry info */
				entry.character=msg.getCharacter();

				/* We restore back the character to the world */
				RPObject object = entry.loadRPObject();

				/*
				 * We set the clientid attribute to link easily the object with
				 * is player runtime information
				 */
				object.put("clientid", clientid);

				/* We ask RP Manager to initialize the object */
				rpMan.onInit(object);
				
				/* And finally sets this connection state to GAME_BEGIN */
				entry.state=ClientState.GAME_BEGIN;

				/* Correct: Character exist */
				MessageS2CChooseCharacterACK msgChooseCharacterACK = new MessageS2CChooseCharacterACK(msg.getSocketChannel());
				msgChooseCharacterACK.setClientID(clientid);
				netMan.sendMessage(msgChooseCharacterACK);
			} else {
				/** This account doesn't own that character */
				logger.debug("Client(" + msg.getAddress().toString()+ ") hasn't character(" + msg.getCharacter() + ")");
				entry.state=ClientState.LOGIN_COMPLETE;

				/* Error: There is no such character */
				MessageS2CChooseCharacterNACK msgChooseCharacterNACK = new MessageS2CChooseCharacterNACK(msg.getSocketChannel());

				msgChooseCharacterNACK.setClientID(clientid);
				netMan.sendMessage(msgChooseCharacterNACK);
			}
		} catch (Exception e) {
			logger.error("error when processing character event", e);
		}
	}

	/**
	 * This method is called when server recieves a logout message from a player.
	 * It handles all the logic to effectively logout the player and free the associated resources.
	 * @param msg the logout message
	 */
	private void processLogoutEvent(MessageC2SLogout msg) {
		try {
			int clientid = msg.getClientID();
			
			PlayerEntry entry=playerContainer.get(clientid);

			/* verify event so that we can trust that it comes from our player and
			   that it has completed the login stage. */
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			RPObject object = entry.object;

			/* We request to logout of game */
			if (rpMan.onExit(object)) {
				/* NOTE: Set the Object so that it is stored in Database */
				entry.storeRPObject(object);

				stats.add("Players logout", 1);
				playerContainer.remove(clientid);

				/* Send Logout ACK message */
				MessageS2CLogoutACK msgLogout = new MessageS2CLogoutACK(msg.getSocketChannel());

				msgLogout.setClientID(clientid);
				netMan.sendMessage(msgLogout);
				netMan.disconnectClient(msg.getSocketChannel());
			} else {
				/* If RPManager returned false, that means that logout is not allowed right now, so
				 * player request is rejected.
				 * This can be useful to disallow logout on some situations.
				 */
				MessageS2CLogoutNACK msgLogout = new MessageS2CLogoutNACK(msg.getSocketChannel());
				msgLogout.setClientID(clientid);
				netMan.sendMessage(msgLogout);
				return;
			}
		} catch (Exception e) {
			logger.error("error while processing LogoutEvent", e);
		}
	}

	/** 
	 * This method is called by network manager when a client connection is lost
	 * or even when the client logout correctly.
	 */
	public void onDisconnect(SocketChannel channel) {
		/* We need to adquire the lock because this is handle by another thread */
		playerContainer.getLock().requestWriteLock();

		try{
			PlayerEntry entry=playerContainer.get(channel);		
			rpMan.disconnect(entry);
		} finally {
			playerContainer.getLock().releaseLock();
		}
	}

	static int lastActionIdGenerated = 0;

	/** 
	 * This method process actions send from client.
	 * In fact, the action is passed to RPManager that will, when the turn arrives, execute it.
	 * @param msg the action message
	 */
	private void processActionEvent(MessageC2SAction msg) {
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry=playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			/* Send the action to RP Manager */
			RPAction action = msg.getRPAction();

			/* We tag each action with an unique action id. */
			if (!action.has("action_id")) {
				action.put("action_id", ++lastActionIdGenerated);
			}

			/* TODO: These are action attributes that are important for RP functionality.
			 *  Tag them in such way that it is not possible to change them on a buggy
			 *  RP implementation.
			 */
			RPObject object=entry.object;
			action.put("sourceid", object.get("sourceid"));
			action.put("zoneid", object.get("zoneid"));
			action.put("when", rpMan.getTurn());

			stats.add("Actions added", 1);

			if (action.has("type")) {
				stats.add("Actions " + action.get("type"), 1);
			} else {
				stats.add("Actions invalid", 1);
			}

			rpMan.addRPAction(object, action);
		} catch (Exception e) {
			stats.add("Actions invalid", 1);
			logger.error("error while processing ActionEvent", e);
		}
	}

	/** 
	 * This method handles the initial login of the client into the server.
	 * First, it compares the game and version that client is running and if they match
	 * request the client to send the key to continue login process.
	 * Otherwise, it rejects client because game is incompatible.
	 * 
	 * @param msg the login message.
	 */
	private void processLoginRequestKey(Message msg) {
		MessageC2SLoginRequestKey msgRequest = (MessageC2SLoginRequestKey) msg;
		if (rpMan.checkGameVersion(msgRequest.getGame(), msgRequest.getVersion())) {
			MessageS2CLoginSendKey msgLoginSendKey = new MessageS2CLoginSendKey(msg.getSocketChannel(), key);
			msgLoginSendKey.setClientID(Message.CLIENTID_INVALID);
			netMan.sendMessage(msgLoginSendKey);
		} else {
			/* Error: Incompatible game version. Update client */
			logger.debug("Client is running an incompatible game version. Client("+ msg.getAddress().toString() + ") can't login");

			/* Notify player of the event. */
			MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(), MessageS2CLoginNACK.Reasons.GAME_MISMATCH);
			netMan.sendMessage(msgLoginNACK);
		}
	}

	/** 
	 * This message is used to create a game account.
	 * It may fail if the player already exists or if any of the fields are empty.
	 * 
	 * @param msg The create account message.
	 */
	private void processCreateAccount(MessageC2SCreateAccount msg) {
		try {
			Result result = rpMan.createAccount(msg.getUsername(), msg.getPassword(), msg.getEmail(), msg.getTemplate());
			
			if (result == Result.OK_ACCOUNT_CREATED) {
				logger.debug("Account (" + msg.getUsername() + ") created.");
				MessageS2CCreateAccountACK msgCreateAccountACK = new MessageS2CCreateAccountACK(msg.getSocketChannel());
				netMan.sendMessage(msgCreateAccountACK);
			} else {
				MessageS2CCreateAccountNACK.Reasons reason;

				if (result == Result.FAILED_PLAYER_EXISTS) {
					reason = MessageS2CCreateAccountNACK.Reasons.USERNAME_EXISTS;
				} else {
					reason = MessageS2CCreateAccountNACK.Reasons.FIELD_TOO_SHORT;
				}

				MessageS2CCreateAccountNACK msgCreateAccountNACK = new MessageS2CCreateAccountNACK(msg.getSocketChannel(), reason);
				netMan.sendMessage(msgCreateAccountNACK);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * This method is part of the crypto process to login the player.
	 * It creates a place for the player on the server, so we should handle some timeout here
	 * to avoid DOS attacks.
	 * 
	 * @param msg the promise message.
	 */
	private void processLoginSendPromise(Message msg) {
		try {
			/* TODO: Fix me. This limit leads easily to a DOS attack */
			if (playerContainer.size() == GameConst.MAX_NUMBER_PLAYERS) {
				/* Error: Too many clients logged on the server. */
				logger.warn("Server is full, Client("+ msg.getAddress().toString() + ") can't login");

				/* Notify player of the event. */
				MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(), MessageS2CLoginNACK.Reasons.SERVER_IS_FULL);
				netMan.sendMessage(msgLoginNACK);
				return;
			}

			MessageC2SLoginSendPromise msgLoginSendPromise = (MessageC2SLoginSendPromise) msg;
			
			byte[] nonce = Hash.random(Hash.hashLength());
			
			PlayerEntry entry=playerContainer.add(msg.getSocketChannel());
			entry.loginInformations = new PlayerEntry.SecuredLoginInfo(key,msgLoginSendPromise.getHash(),nonce);

			MessageS2CLoginSendNonce msgLoginSendNonce = new MessageS2CLoginSendNonce(msg.getSocketChannel(), nonce);
			msgLoginSendNonce.setClientID(entry.clientid);
			netMan.sendMessage(msgLoginSendNonce);
		} catch (Exception e) {
			logger.error("client not found", e);
		}
	}

	/** 
	 * This last method completes the login process.
	 * 
	 * @param msg
	 */
	private void processSecuredLoginEvent(Message msg) {
		try {
			MessageC2SLoginSendNonceNameAndPassword msgLogin = (MessageC2SLoginSendNonceNameAndPassword) msg;

			int clientid = msg.getClientID();
			PlayerEntry entry=playerContainer.get(clientid);
			
			// verify event
			if (!isValidEvent(msg, entry, ClientState.CONNECTION_ACCEPTED)) {
				return;
			}
			
			SecuredLoginInfo info=entry.loginInformations;

			info.clientNonce = msgLogin.getHash();
			info.username = msgLogin.getUsername();
			info.password = msgLogin.getPassword();

			/* We verify the username and the password to make sure player is who he/she says he/she is. */
			if (!info.verify()) {
				/* If the verification fails we send player a NACK and record the event */
				logger.info("Incorrect username/password for player "+ info.username);
				stats.add("Players invalid login", 1);
				info.addLoginEvent(msg.getAddress(), false);

				/* Send player the Login NACK message */
				MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(),
						MessageS2CLoginNACK.Reasons.USERNAME_WRONG);

				netMan.sendMessage(msgLoginNACK);
				playerContainer.remove(clientid);
				return;
			}
			
			/* Now we check if this player is previously logged in */
			PlayerEntry existing=playerContainer.get(msgLogin.getUsername());
			
			if(existing!=null) {
				/* Warning: Player is alreay logged. So we proceed to store it and remove from game. */
				logger.warn("Client(" + msg.getAddress().toString()	+ ") trying to login twice");

				if (existing.state == ClientState.GAME_BEGIN) {
					RPObject object=existing.object;
					
					if (rpMan.onExit(object)) {
						/* NOTE: Set the Object so that it is stored in Database */
						existing.storeRPObject(object);
					}
				} else {
					logger.info("Player trying to logout without choosing character");
				}

				playerContainer.remove(existing.clientid);
			}

			logger.debug("Correct username/password");

			/* Correct: The login is correct */
			entry.username = info.username;
			
			/* Now we store at database the login event */
			info.addLoginEvent(msg.getAddress(), true);

			/* We clean the login information as it is not longer useful. */
			entry.loginInformations = null;			

			stats.add("Players login", 1);

			/* Send player the Login ACK message */
			MessageS2CLoginACK msgLoginACK = new MessageS2CLoginACK(msg.getSocketChannel());
			msgLoginACK.setClientID(clientid);
			netMan.sendMessage(msgLoginACK);
			
			/* TODO: Return also player a list of previous login attemps */

			/* Send player the ServerInfo */
			MessageS2CServerInfo msgServerInfo = new MessageS2CServerInfo(msg.getSocketChannel(), ServerInfo.get());
			msgServerInfo.setClientID(clientid);
			netMan.sendMessage(msgServerInfo);

			/* Build player character list and send it to client */
			String[] characters = entry.getCharacters().toArray(new String[0]);
			MessageS2CCharacterList msgCharacters = new MessageS2CCharacterList(msg.getSocketChannel(), characters);
			msgCharacters.setClientID(clientid);
			netMan.sendMessage(msgCharacters);
			
			entry.state=ClientState.LOGIN_COMPLETE;
		} catch (Exception e) {
			logger.error("error while processing SecuredLoginEvent", e);
		}
	}

	/** 
	 * This message is send from client to notify that client suffered a network problem
	 * and request data to be resend again to it.
	 * @param msg the out of sync message
	 */
	private void processOutOfSyncEvent(MessageC2SOutOfSync msg) {
		try {
			int clientid = msg.getClientID();
			PlayerEntry entry=playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			/** Notify Player Entry that this player is out of Sync */
			entry.requestSync();
		} catch (Exception e) {
			logger.error("error while processing OutOfSyncEvent", e);
		}
	}

	/**
	 * This message is send from client to server to notify server which of the proposed 
	 * transfer has to be done.
	 * @param msg the transfer ACK message
	 */
	private void processTransferACK(MessageC2STransferACK msg) {
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry=playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			/** Handle Transfer ACK here */
			for (TransferContent content : msg.getContents()) {
				if (content.ack == true) {
					logger.debug("Trying transfer content " + content);

					content = entry.getContent(content.name);
					if (content != null) {
						stats.add("Transfer content", 1);
						stats.add("Tranfer content size", content.data.length);

						logger.debug("Transfering content " + content);

						MessageS2CTransfer msgTransfer = new MessageS2CTransfer(entry.channel, content);
						msgTransfer.setClientID(clientid);
						netMan.sendMessage(msgTransfer);
					} else {
						logger.debug("CAN'T transfer content " + content);
					}
				} else {
					stats.add("Transfer content cache", 1);
				}
			}

			/** We clear the content pending to be sent */
			entry.clearContent();
		} catch (Exception e) {
			logger.error("error while processing TransferACK", e);
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
		static Configuration config;
		static {
			try {
				config = Configuration.getConfiguration();
				// just check if mandatory properties are set
				config.get("server_typeGame");
				config.get("server_name");
				config.get("server_version");
				config.get("server_contact");
			} catch (Exception e) {
				logger.error("ERROR: Unable to load Server info", e);
			}
		}

		/** This method builds a String[] from the properties used in Server Info */
		public static String[] get() {
			List<String> l_result = new ArrayList<String>();

			Enumeration props = config.propertyNames();
			while (props.hasMoreElements()) {
				String prop_name = String.valueOf(props.nextElement());
				if (prop_name.startsWith("server_")) {
					try {
						l_result.add(config.get(prop_name));
					} catch (PropertyNotFoundException pnfe) {
						// cant be. only in multithreaded emvironment possible
						logger.debug("Property " + prop_name+ " is not set???");
					}
				}
			}
			String[] result = new String[l_result.size()];
			return l_result.toArray(result);
		}
	}
}
