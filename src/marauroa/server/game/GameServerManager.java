/* $Id: GameServerManager.java,v 1.134 2010/02/22 18:09:27 nhnb Exp $ */
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

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.TimeoutConf;
import marauroa.common.crypto.Hash;
import marauroa.common.crypto.RSAKey;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.Result;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;
import marauroa.common.net.message.MessageC2SChooseCharacter;
import marauroa.common.net.message.MessageC2SCreateAccount;
import marauroa.common.net.message.MessageC2SCreateCharacter;
import marauroa.common.net.message.MessageC2SKeepAlive;
import marauroa.common.net.message.MessageC2SLoginRequestKey;
import marauroa.common.net.message.MessageC2SLoginSendNonceNameAndPassword;
import marauroa.common.net.message.MessageC2SLoginSendNonceNamePasswordAndSeed;
import marauroa.common.net.message.MessageC2SLoginSendPromise;
import marauroa.common.net.message.MessageC2SLogout;
import marauroa.common.net.message.MessageC2SOutOfSync;
import marauroa.common.net.message.MessageC2STransferACK;
import marauroa.common.net.message.MessageS2CCharacterList;
import marauroa.common.net.message.MessageS2CChooseCharacterACK;
import marauroa.common.net.message.MessageS2CChooseCharacterNACK;
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;
import marauroa.common.net.message.MessageS2CCreateCharacterACK;
import marauroa.common.net.message.MessageS2CCreateCharacterNACK;
import marauroa.common.net.message.MessageS2CLoginACK;
import marauroa.common.net.message.MessageS2CLoginMessageNACK;
import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.common.net.message.MessageS2CLoginSendKey;
import marauroa.common.net.message.MessageS2CLoginSendNonce;
import marauroa.common.net.message.MessageS2CLogoutACK;
import marauroa.common.net.message.MessageS2CLogoutNACK;
import marauroa.common.net.message.MessageS2CServerInfo;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.TransferContent;
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
 * <p>
 * The logic is similar to this:
 *
 * <pre>
 *   GameManager
 *   {
 *   NetworkManager read Message
 *
 *   switch(Message type)
 *   {
 *   case ...;
 *   }
 *   }
 * </pre>
 *
 * So let's define the reply to each message. First, let's clarify that the best
 * way of modelling this system is using finite automates, (a finite state
 * machine) where, based on the input, we change the state we are currently in
 * and produce an output.
 * <p>
 * Login
 *
 * <pre>
 *   Process C2S Login ( STATE_BEGIN_LOGIN )
 *   Precondition: The state MUST be NULL
 *
 *   Test if there is room for more players.
 *   if there is no more room
 *   {
 *   reply S2C Login NACK( SERVER_FULL )
 *   state = NULL
 *   }
 *
 *   if check username, password in database is correct
 *   {
 *   create clientid
 *   add PlayerEntry
 *   notify database
 *
 *   reply S2C Login ACK
 *
 *   get characters list of the player
 *   reply S2C CharacterList
 *
 *   state = STATE_LOGIN_COMPLETE
 *   }
 *   else
 *   {
 *   notify database
 *
 *   reply S2C Login NACK( LOGIN_INCORRECT )
 *   state = NULL
 *   }
 *
 *   Postcondition: The state MUST be NULL or STATE_LOGIN_COMPLETE
 *   and a we have created a PlayerEntry for this player with a unique clientid.
 * </pre>
 *
 * Choose Character
 *
 * <pre>
 *   Process C2S ChooseCharacter ( STATE_LOGIN_COMPLETE )
 *   Precondition: The state MUST be STATE_LOGIN_COMPLETE
 *
 *   if character exists in database
 *   {
 *   add character to Player's PlayerEntry
 *   add character to game
 *   reply S2C Choose Character ACK
 *
 *   state = STATE_GAME_BEGIN
 *   }
 *   else
 *   {
 *   reply S2C Choose Character NACK
 *   state = STATE_LOGIN_COMPLETE
 *   }
 *
 *   Postcondition: The state MUST be STATE_GAME_BEGIN and the PlayerStructure
 *   should be completely filled or if the character choise was wrong the state is STATE_LOGIN_COMPLETE
 * </pre>
 *
 * Logout stage
 *
 * <pre>
 *   Process C2S Logout ( STATE_GAME_END )
 *   Precondition: The state can be anything but STATE_LOGIN_BEGIN
 *
 *   if( rpEngine allows player to logout )
 *   {
 *   reply S2C Logout ACK
 *   state = NULL
 *
 *   store character in database
 *   remove character from game
 *   delete PlayerEntry
 *   }
 *   else
 *   {
 *   reply S2C Logout NACK
 *   }
 *
 *   Postcondition: Either the same as the input state or the state currently in
 * </pre>
 *
 * @author miguel
 */
public final class GameServerManager extends Thread implements IDisconnectedListener {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);

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
	
	private DisconnectPlayers disconnectThread;

	/** maxium number of accepted players */
	private int maxNumberOfPlayers = 128;

	/**
	 * Constructor that initialize also the RPManager
	 *
	 * @param key
	 *            the server private key
	 * @param netMan
	 *            a NetworkServerManager instance.
	 * @param rpMan
	 *            a RPServerManager instance.
	 * @throws Exception
	 *             is there is any problem.
	 */
	public GameServerManager(RSAKey key, INetworkServerManager netMan, RPServerManager rpMan)
	        throws Exception {
		super("GameServerManager");
		keepRunning = true;
		isfinished = false;

		this.key = key;
		this.netMan = netMan;
		this.rpMan = rpMan;
		
		String temp = Configuration.getConfiguration().get("max_number_of_players");
		if (temp != null) {
			this.maxNumberOfPlayers  = Integer.parseInt(temp.trim());
		}
		
		netMan.registerDisconnectedListener(this);

		playerContainer = PlayerEntryContainer.getContainer();
		stats = Statistics.getStatistics();
		
		disconnectThread= new DisconnectPlayers();
	}
	
	/**
	 * Starting this thread makes it to start the thread that disconnect players.
	 */
	@Override
	public synchronized void start() {
		super.start();
		disconnectThread.start();
	}

	/**
	 * Thread that disconnect players.
	 * It has to be done this way because we can't run it on the main loop of GameServerManager,
	 * because it locks waiting for new messages to arrive, so the player keeps unremoved until a 
	 * message is recieved.
	 * 
	 * This way players are removed as they are requested to be.
	 * 
	 * @author miguel
	 *
	 */
	class DisconnectPlayers extends Thread {
		BlockingQueue<SocketChannel> players;
		
		/**
		 * Constructor.
		 * It just gives a nice name to the thread.
		 */
		public DisconnectPlayers() {
			super("GameServerManagerDisconnectPlayers");
			players=new LinkedBlockingQueue<SocketChannel>();
		}
		
		/**
		 * This method is used mainly by onDisconnect and RPServerManager to force
		 * the disconnection of a player entry.
		 *
		 * @param channel
		 *            the socket channel of the player entry to remove.
		 */
		public void disconnect(SocketChannel channel) {
			try {
				players.put(channel);
			} catch (InterruptedException e) {
				/*
				 * Not really instereted in.
				 */
			}
		}

		@Override
		public void run() {
			while (keepRunning) {
				SocketChannel channel = null;

				/*
				 * We keep waiting until we are signaled to remove a player.
				 * This way we avoid wasting CPU cycles.
				 */
				try {
					channel = players.take();
				} catch (InterruptedException e1) {
					/*
					 * Not interested.
					 */
				}

				playerContainer.getLock().requestWriteLock();

				PlayerEntry entry = playerContainer.get(channel);
				if (entry != null) {
					/*
					 * First we remove the entry from the player container.
					 */
					playerContainer.remove(entry.clientid);

					/*
					 * If client is still login, don't notify RP as it knows nothing about
					 * this client. That means state != of GAME_BEGIN
					 */
					if (entry.state == ClientState.GAME_BEGIN) {
						/*
						 * If client was playing the game request the RP to disconnected it.
						 */
						try {
							rpMan.onTimeout(entry.object);
							entry.storeRPObject(entry.object);
						} catch (Exception e) {
							logger.error("Error disconnecting player" + entry, e);
						}
					}

					/*
					 * We set the entry to LOGOUT_ACCEPTED state so it can also be freed by
					 * GameServerManager to make room for new players.
					 */
					entry.state = ClientState.LOGOUT_ACCEPTED;
				} else {
					/*
					 * Player may have logout correctly or may have even not started.
					 */
					logger.debug("No player entry for channel: " + channel);
				}

				playerContainer.getLock().releaseLock();
			}
		}
	}

	/**
	 * This method request the active object to finish its execution and store
	 * all the players back to database.
	 */
	public void finish() {
		/* We store all the players when we are requested to exit */
		storeConnectedPlayers();
		
		rpMan.finish();
		keepRunning = false;
		
		interrupt();
		disconnectThread.interrupt();
		
		while (isfinished == false) {
			Thread.yield();
		}
	}

	/*
	 * Disconnect any connected player and if the player has already login and
	 * was playing game it is stored back to database.
	 */
	private void storeConnectedPlayers() {
		/*
		 * We want to avoid concurrentComodification of playerContainer.
		 */
		List<PlayerEntry> list=new LinkedList<PlayerEntry>();
		for (PlayerEntry entry : playerContainer) {			
			list.add(entry);
		}
		
		/*
		 * Now we iterate the list and remove characters.
		 */		
		for (PlayerEntry entry : list) {
			logger.info("STORING ("+entry.username+") :"+entry.object);
			/*
			 * It may be a bit slower than disconnecting here, but server is
			 * going down so there is no hurry.
			 */
			onDisconnect(entry.channel);
		}
	}

	/**
	 * Runs the game glue logic. This class is responsible of receiving messages
	 * from clients and instruct RP about actions clients did.
	 */
	@Override
	public void run() {
		try {
			while (keepRunning) {
				Message msg = netMan.getMessage();

				if (msg != null) {
					// TODO: Bootleneck because of synchronization.
					playerContainer.getLock().requestWriteLock();
					@SuppressWarnings("unused")
					long startTime = System.currentTimeMillis();
					switch (msg.getType()) {
						case C2S_LOGIN_REQUESTKEY:
							/*
							 * Process the C2S Login request key message.
							 */
							logger.debug("Processing C2S Login Request Key Message");
							processLoginRequestKey(msg);
							break;
						case C2S_LOGIN_SENDPROMISE:
							/*
							 * Receive the client hash promise of the password. Now
							 * client has a PlayerEntry container.
							 */
							logger.debug("Processing C2S Login Send Promise Message");
							processLoginSendPromise(msg);
							break;
						case C2S_LOGIN_SENDNONCENAMEANDPASSWORD:
							/*
							 * Complete the login stage. It will either success and
							 * game continue or fail and resources for this player
							 * are freed.
							 */
							logger.debug("Processing C2S Secured Login Message");
							processSecuredLoginEvent(msg);
							break;
						case C2S_LOGIN_SENDNONCENAMEPASSWORDANDSEED:
							/*
							 * Complete the login stage. It will either success and
							 * game continue or fail and resources for this player
							 * are freed.
							 */
							logger.debug("Processing C2S Secured Login Message");
							processSecuredLoginEvent(msg);
							break;
						case C2S_CHOOSECHARACTER:
							/*
							 * Process the choose character message from client.
							 * This message is the one that move the player from
							 * login stage to game stage.
							 */
							logger.debug("Processing C2S Choose Character Message");
							processChooseCharacterEvent((MessageC2SChooseCharacter) msg);
							break;
						case C2S_LOGOUT:
							/*
							 * Request server to exit and free resources associated.
							 * It may fail if RP decides not to allow player logout.
							 */
							logger.debug("Processing C2S Logout Message");
							processLogoutEvent((MessageC2SLogout) msg);
							break;
						case C2S_ACTION:
							/*
							 * Process an action received from client and pass it
							 * directly to RP manager.
							 */
							logger.debug("Processing C2S Action Message");
							processActionEvent((MessageC2SAction) msg);
							break;
						case C2S_OUTOFSYNC:
							// TODO: Consider removing this message as with TCP it
							// won't fail.
							/*
							 * When client get out of sync because it has lost part
							 * of the messages stream, it can request a
							 * synchronization to continue game.
							 */
							logger.debug("Processing C2S Out Of Sync Message");
							processOutOfSyncEvent((MessageC2SOutOfSync) msg);
							break;
						case C2S_KEEPALIVE:
							/*
							 * Recieve keep alive messages from client.
							 */
							logger.debug("Processing C2S Keep alive Message");
							processKeepAliveEvent((MessageC2SKeepAlive) msg);
							break;
						case C2S_TRANSFER_ACK:
							/*
							 * This message is received when client get data at
							 * server request and it confirms the data to be sent.
							 */
							logger.debug("Processing C2S Transfer ACK Message");
							processTransferACK((MessageC2STransferACK) msg);
							break;
						case C2S_CREATEACCOUNT:
							/*
							 * This is a create account request. It just create the
							 * account, it doesn't login us into it.
							 */
							logger.debug("Processing C2S Create Account Message");
							processCreateAccount((MessageC2SCreateAccount) msg);
							break;
						case C2S_CREATECHARACTER:
							/*
							 * This is a create character request. It require that
							 * client has correctly logged to server. Once client
							 * create character a new Choose Character message is
							 * sent.
							 */
							logger.debug("Processing C2S Create Character Message");
							processCreateCharacter((MessageC2SCreateCharacter) msg);
							break;
						default:
							logger.debug("Unknown Message[" + msg.getType() + "]");
							break;
					}
					playerContainer.getLock().releaseLock();
					/*long time = System.currentTimeMillis() - startTime;
					if (time > 50) {
						logger.warn("Processing client message took " + time + " ms: " + msg);
					}*/
				}

				/*
				 * Finally store stats about logged players.
				 */
				logger.debug("PlayerEntryContainer size: " + playerContainer.size());
				stats.set("Players online", playerContainer.size());
			}
		} catch (Throwable e) {
			logger.error("Unhandled exception, server will shut down.", e);
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
	private boolean isValidEvent(Message msg, PlayerEntry entry, ClientState... states) {
		if (entry == null) {
			/*
			 * Error: Player didn't login.
			 */
			logger.warn("Client(" + msg.getAddress() + ") has not login yet");
			return false;
		}

		/*
		 * Now we check if client is in any of the valid states
		 */
		boolean isInCorrectState = false;
		for (ClientState state : states) {
			if (entry.state == state) {
				isInCorrectState = true;
			}
		}

		/*
		 * And it it is not in the correct state, return false.
		 */
		if (!isInCorrectState) {
			StringBuffer statesString = new StringBuffer();
			for (ClientState state : states) {
				statesString.append(state + " ");
			}

			logger.warn("Client(" + msg.getAddress() + ") is not in the required state ("
			        + statesString.toString() + ")");
			return false;
		}

		/*
		 * Finally we check if another player is trying to inject messages into
		 * a different player avatar.
		 */
		if (entry.channel != msg.getSocketChannel()) {
			/*
			 * Info: Player is using a different socket to communicate with
			 * server.
			 */
			logger.warn("Client(" + msg.getAddress() + ") has not correct IP<->clientid relation");
			return false;
		}

		/*
		 * If nothing of the above happens, it means this event is valid and we
		 * keep processing it.
		 */
		return true;
	}

	/**
	 * This methods handles the logic when a Choose Character message is
	 * received from client, checking the message and choosing the character.
	 *
	 * This method will send also the reply ACK or NACK to the message.
	 *
	 * @param msg
	 *            The ChooseCharacter message
	 */
	private void processChooseCharacterEvent(MessageC2SChooseCharacter msg) {
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			/*
			 * verify event so that we can trust that it comes from our player
			 * and that it has completed the login stage.
			 */
			if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE)) {
				return;
			}

			/* We check if this account has such player. */
			if (entry.hasCharacter(msg.getCharacter())) {
				/* We set the character in the entry info */
				entry.character = msg.getCharacter();

				/* We restore back the character to the world */
				RPObject object = entry.loadRPObject();

				if (object != null) {
    				/*
    				 * We set the clientid attribute to link easily the object with
    				 * is player runtime information
    				 */
    				object.put("#clientid", clientid);
				} else {
					logger.warn("could not load object for character("+entry.character+") from database");
				}

				/* We ask RP Manager to initialize the object */
				if(rpMan.onInit(object)) {
					/* And finally sets this connection state to GAME_BEGIN */
					entry.state = ClientState.GAME_BEGIN;

					/* Correct: Character exist */
					MessageS2CChooseCharacterACK msgChooseCharacterACK = new MessageS2CChooseCharacterACK(
							msg.getSocketChannel());
					msgChooseCharacterACK.setClientID(clientid);
					netMan.sendMessage(msgChooseCharacterACK);
					return;
				} else {
					/* This account doesn't own that character */
					logger.warn("RuleProcessor rejected character(" + msg.getCharacter()+")");
				}
			} else {
				/* This account doesn't own that character */
				logger.warn("Client(" + msg.getAddress().toString() + ") hasn't character("
						+ msg.getCharacter() + ")");
			}

			/*
			 * If the account doesn't own the character OR if the rule processor rejected it.
			 * So we return it back to login complete stage.
			 */
			entry.state = ClientState.LOGIN_COMPLETE;

			/* Error: There is no such character */
			MessageS2CChooseCharacterNACK msgChooseCharacterNACK = new MessageS2CChooseCharacterNACK(
					msg.getSocketChannel());

			msgChooseCharacterNACK.setClientID(clientid);
			netMan.sendMessage(msgChooseCharacterNACK);

		} catch (Exception e) {
			logger.error("error when processing character event", e);
		}
	}

	/**
	 * This method is called when server receives a logout message from a
	 * player. It handles all the logic to effectively logout the player and
	 * free the associated resources.
	 *
	 * @param msg
	 *            the logout message
	 */
	private void processLogoutEvent(MessageC2SLogout msg) {
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			/*
			 * verify event so that we can trust that it comes from our player
			 * and that it has completed the login stage.
			 */
			if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE, ClientState.GAME_BEGIN)) {
				return;
			}

			RPObject object = entry.object;

			boolean shouldLogout = true;

			/*
			 * We request to logout of game to RP Manager If may be succesful or
			 * fail and we keep on game.
			 */
			if (entry.state == ClientState.GAME_BEGIN) {
				if (rpMan.onExit(object)) {
					/* NOTE: Set the Object so that it is stored in Database */
					entry.storeRPObject(object);
				} else {
					/*
					 * If RPManager returned false, that means that logout is
					 * not allowed right now, so player request is rejected.
					 * This can be useful to disallow logout on some situations.
					 */
					shouldLogout = false;
				}
			}

			if (shouldLogout) {
				stats.add("Players logout", 1);
				logger.info("Logging out correctly channel: "+entry.channel);
				playerContainer.remove(clientid);

				/* Send Logout ACK message */
				MessageS2CLogoutACK msgLogout = new MessageS2CLogoutACK(msg.getSocketChannel());

				msgLogout.setClientID(clientid);
				netMan.sendMessage(msgLogout);

				entry.state = ClientState.LOGOUT_ACCEPTED;
			} else {
				MessageS2CLogoutNACK msgLogout = new MessageS2CLogoutNACK(msg.getSocketChannel());
				msgLogout.setClientID(clientid);
				netMan.sendMessage(msgLogout);
			}
		} catch (Exception e) {
			logger.error("error while processing LogoutEvent", e);
		}
	}

	/**
	 * This method is called by network manager when a client connection is lost
	 * or even when the client logout correctly.
	 *
	 * @param channel
	 *            the channel that was closed.
	 */
	public void onDisconnect(SocketChannel channel) {
		logger.info("GAME Disconnecting " + channel);		
		disconnectThread.disconnect(channel);
	}

	/**
	 * This method process actions send from client. In fact, the action is
	 * passed to RPManager that will, when the turn arrives, execute it.
	 *
	 * @param msg
	 *            the action message
	 */
	private void processActionEvent(MessageC2SAction msg) {
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			/*
			 * verify event
			 */
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}
			
			/*
			 * Update timeout timestamp on player.
			 */
			entry.update();

			/* Send the action to RP Manager */
			RPAction action = msg.getRPAction();

			/*
			 * NOTE: These are action attributes that are important for RP
			 * functionality. Tag them in such way that it is not possible to
			 * change them on a buggy RP implementation or it will cause
			 * problems at server.
			 */
			RPObject object = entry.object;
			action.put("sourceid", object.get("id"));
			action.put("zoneid", object.get("zoneid"));

			stats.add("Actions added", 1);

			/*
			 * Log the action into statistics system. Or if the action didn't
			 * have type, log it as an invalid action.
			 */
			if (action.has("type")) {
				stats.add("Actions " + action.get("type"), 1);
			} else {
				stats.add("Actions invalid", 1);
			}

			/*
			 * Finally pass the action to the RP Manager
			 */
			rpMan.addRPAction(object, action);
		} catch (Exception e) {
			stats.add("Actions invalid", 1);
			logger.error("error while processing ActionEvent", e);
		}
	}

	/**
	 * This message is used to create a game account. It may fail if the player
	 * already exists or if any of the fields are empty. This method does not
	 * make room for the player at the server.
	 *
	 * @param msg
	 *            The create account message.
	 */
	private void processCreateAccount(MessageC2SCreateAccount msg) {
		try {
			/*
			 * We request RP Manager to create an account for our player. This
			 * will return a <b>result</b> of the operation.
			 */
			AccountResult val = rpMan.createAccount(msg.getUsername(), msg.getPassword(), msg.getEmail(), msg.getAddress().getHostAddress());
			Result result = val.getResult();

			if (result == Result.OK_CREATED) {
				/*
				 * If result is OK then the account was created and we notify
				 * player about that.
				 */
				logger.debug("Account (" + msg.getUsername() + ") created.");
				MessageS2CCreateAccountACK msgCreateAccountACK = new MessageS2CCreateAccountACK(msg
				        .getSocketChannel(), val.getUsername());
				netMan.sendMessage(msgCreateAccountACK);
			} else {
				/*
				 * Account creation may also fail. So expose the reasons of the
				 * failure.
				 */
				MessageS2CCreateAccountNACK msgCreateAccountNACK = new MessageS2CCreateAccountNACK(
				        msg.getSocketChannel(), result);
				netMan.sendMessage(msgCreateAccountNACK);
			}
		} catch (Exception e) {
			logger.error("Unable to create an account", e);
		}
	}

	/**
	 * This message is used to create a character in a game account. It may fail
	 * if the player already exists or if any of the fields are empty.
	 *
	 * @param msg
	 *            The create account message.
	 */
	private void processCreateCharacter(MessageC2SCreateCharacter msg) {
		try {
			int clientid = msg.getClientID();
			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.LOGIN_COMPLETE)) {
				logger.warn("invalid create character event (client unknown, not logged in or wrong ip-address)");
				return;
			}

			/*
			 * We request the creation of an account for a logged player. It
			 * will also return a result of the character that we must follow to
			 * player.
			 */
			CharacterResult val = rpMan.createCharacter(entry.username, msg.getCharacter(), msg
			        .getTemplate());
			Result result = val.getResult();

			if (result == Result.OK_CREATED) {
				/*
				 * If the character is created notify player and send him a
				 * Character list message.
				 */
				logger.debug("Character (" + msg.getCharacter() + ") created for account "
				        + entry.username);
				MessageS2CCreateCharacterACK msgCreateCharacterACK = new MessageS2CCreateCharacterACK(
				        msg.getSocketChannel(), val.getCharacter(), val.getTemplate());
				msgCreateCharacterACK.setClientID(clientid);
				netMan.sendMessage(msgCreateCharacterACK);

				/*
				 * Build player character list and send it to client
				 */
				String[] characters = entry.getCharacters().toArray(new String[entry.getCharacters().size()]);
				MessageS2CCharacterList msgCharacters = new MessageS2CCharacterList(msg
				        .getSocketChannel(), characters);
				msgCharacters.setClientID(clientid);
				netMan.sendMessage(msgCharacters);
			} else {
				/*
				 * It also may fail to create the character. Explain the reasons
				 * to player.
				 */
				MessageS2CCreateCharacterNACK msgCreateCharacterNACK = new MessageS2CCreateCharacterNACK(
				        msg.getSocketChannel(), result);
				netMan.sendMessage(msgCreateCharacterNACK);
			}
		} catch (Exception e) {
			logger.error("Unable to create a character", e);
		}
	}

	/**
	 * This method handles the initial login of the client into the server.
	 * First, it compares the game and version that client is running and if
	 * they match request the client to send the key to continue login process.
	 *
	 * Otherwise, it rejects client because game is incompatible.
	 *
	 * @param msg
	 *            the login message.
	 */
	private void processLoginRequestKey(Message msg) {
		MessageC2SLoginRequestKey msgRequest = (MessageC2SLoginRequestKey) msg;

		/*
		 * Check game version with data suplied by client. The RP may decide to
		 * deny login to this player.
		 */
		if (rpMan.checkGameVersion(msgRequest.getGame(), msgRequest.getVersion())) {
			/*
			 * If this is correct we send player the server key so it can sign
			 * the password.
			 */
			MessageS2CLoginSendKey msgLoginSendKey = new MessageS2CLoginSendKey(msg
			        .getSocketChannel(), key);
			msgLoginSendKey.setClientID(Message.CLIENTID_INVALID);
			netMan.sendMessage(msgLoginSendKey);
		} else {
			/* Error: Incompatible game version. Update client */
			logger.debug("Client is running an incompatible game version. Client("
			        + msg.getAddress().toString() + ") can't login");

			/* Notify player of the event by denying the login. */
			MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(),
			        MessageS2CLoginNACK.Reasons.GAME_MISMATCH);
			netMan.sendMessage(msgLoginNACK);
		}
	}

	/**
	 * This method is part of the crypto process to login the player. It creates
	 * a place for the player on the server, so we should handle some timeout
	 * here to avoid DOS attacks.
	 *
	 * @param msg
	 *            the promise message.
	 */
	private void processLoginSendPromise(Message msg) {
		try {
			/*
			 * If there is no more room we try to get an idle player that we can
			 * disconnect We can define an idle player entry like a player that
			 * has connected to server and requested to login, but never
			 * actually completed login stage. This client is taking server
			 * resources but doing nothing useful at all.
			 */
			if (playerContainer.size() >= maxNumberOfPlayers) {
				logger.info("Server is full, making room now");
				/* Let's try to make some room for more players. */
				PlayerEntry candidate = playerContainer.getIdleEntry();

				if (candidate != null) {
					/*
					 * Cool!, we got a candidate, so we remove and disconnect
					 * it.
					 */
					netMan.disconnectClient(candidate.channel);

					/*
					 * HACK: Remove the entry now so we can continue.
					 */				
					playerContainer.remove(candidate.clientid);
				}
			}

			/*
			 * We give a new try to see if we can create a entry for this
			 * player.
			 */
			if (playerContainer.size() >= maxNumberOfPlayers) {
				/* Error: Too many clients logged on the server. */
				logger.warn("Server is full, Client(" + msg.getAddress().toString()
				        + ") can't login. You may want to increase max_number_of_players in your server.init. Current value is: " + maxNumberOfPlayers);

				/* Notify player of the event. */
				MessageS2CLoginNACK msgLoginNACK = new MessageS2CLoginNACK(msg.getSocketChannel(),
				        MessageS2CLoginNACK.Reasons.SERVER_IS_FULL);
				netMan.sendMessage(msgLoginNACK);
				return;
			}

			MessageC2SLoginSendPromise msgLoginSendPromise = (MessageC2SLoginSendPromise) msg;

			PlayerEntry entry = playerContainer.add(msgLoginSendPromise.getSocketChannel());

			byte[] serverNonce = Hash.random(Hash.hashLength());
			byte[] clientNonceHash = msgLoginSendPromise.getHash();

			entry.loginInformations = new PlayerEntry.SecuredLoginInfo(key, clientNonceHash,
			        serverNonce, msgLoginSendPromise.getAddress());

			MessageS2CLoginSendNonce msgLoginSendNonce = new MessageS2CLoginSendNonce(msg
			        .getSocketChannel(), serverNonce);
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
	 *            the final message the contains the encrypted password.
	 */
	private void processSecuredLoginEvent(Message msg) {
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
				info.seed = msgLogin.getSeed();
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
				String whiteList = "," + conf.get("account_creation_ip_whitelist", "127.0.0.1") + ",";
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
					RPObject object = existing.object;

					rpMan.onTimeout(object);
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

	/**
	 * This message is send from client to notify that client suffered a network
	 * problem and request data to be resend again to it.
	 *
	 * @param msg
	 *            the out of sync message
	 */
	private void processOutOfSyncEvent(MessageC2SOutOfSync msg) {
		try {
			int clientid = msg.getClientID();
			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			/*
			 * Notify Player Entry that this player is out of Sync
			 */
			entry.requestSync();
		} catch (Exception e) {
			logger.error("error while processing OutOfSyncEvent", e);
		}
	}

	/**
	 * This message is send from client to confirm that he is still alive and has not timeout. 
	 *
	 * @param alive
	 *            the keep alive message
	 */
	private void processKeepAliveEvent(MessageC2SKeepAlive alive) {
		try {
			int clientid = alive.getClientID();
			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(alive, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			entry.update();
		} catch (Exception e) {
			logger.error("error while processing Keep Alive event", e);
		}
	}
	
	/**
	 * This message is send from client to server to notify server which of the
	 * proposed transfer has to be done.
	 *
	 * @param msg
	 *            the transfer ACK message
	 */
	private void processTransferACK(MessageC2STransferACK msg) {
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			/*
			 * Handle Transfer ACK here. We iterate over the contents and send
			 * them to client for those of them which client told us ACK.
			 */
			for (TransferContent content : msg.getContents()) {
				if (content.ack == true) {
					logger.debug("Trying transfer content " + content);

					/*
					 * We get the content from those of that this client are
					 * waiting for being sent to it.
					 */
					TransferContent contentToTransfer = entry.getContent(content.name);
					if (contentToTransfer != null) {
						stats.add("Transfer content", 1);
						stats.add("Tranfer content size", contentToTransfer.data.length);

						logger.debug("Transfering content " + contentToTransfer);

						MessageS2CTransfer msgTransfer = new MessageS2CTransfer(entry.channel,
								contentToTransfer);
						msgTransfer.setClientID(clientid);
						netMan.sendMessage(msgTransfer);
					} else {
						logger.info("CAN'T transfer content (" + content.name
						        + ") because it is null");
					}
				} else {
					stats.add("Transfer content cache", 1);
				}
			}

			/*
			 * We clear the content pending to be sent
			 */
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
