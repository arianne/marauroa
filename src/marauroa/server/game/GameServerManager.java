/* $Id: GameServerManager.java,v 1.142 2010/05/09 19:42:51 nhnb Exp $ */
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
package marauroa.server.game;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import marauroa.common.Log4J;
import marauroa.common.crypto.RSAKey;
import marauroa.common.net.message.Message;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.messagehandler.MessageDispatcher;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.net.IDisconnectedListener;
import marauroa.server.net.INetworkServerManager;

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

	/** The thread will be running while keepRunning is true */
	private boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private boolean isfinished;
	
	private DisconnectPlayers disconnectThread;
	
	private MessageDispatcher messageDispatcher;

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

		this.netMan = netMan;
		this.rpMan = rpMan;
		

		netMan.registerDisconnectedListener(this);

		playerContainer = PlayerEntryContainer.getContainer();
		stats = Statistics.getStatistics();
		
		disconnectThread= new DisconnectPlayers();

		messageDispatcher = new MessageDispatcher();
		messageDispatcher.init(netMan, rpMan, playerContainer, stats, key);
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
					messageDispatcher.dispatchMessage(msg);
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
				stats.set("Ips online", playerContainer.countUniqueIps());
			}
		} catch (Throwable e) {
			logger.error("Unhandled exception, server will shut down.", e);
		}

		isfinished = true;
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

}
