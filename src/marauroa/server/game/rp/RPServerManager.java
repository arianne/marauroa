/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.rp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.IRPZone;
import marauroa.common.game.Perception;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObjectInvalidException;
import marauroa.common.game.RPObjectNotFoundException;
import marauroa.common.game.Result;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.MessageS2CTransferREQ;
import marauroa.common.net.message.TransferContent;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.ActionInvalidException;
import marauroa.server.game.Statistics;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.net.INetworkServerManager;
import marauroa.server.net.validator.ConnectionValidator;

/**
 * This class is responsible for adding actions to scheduler, and to build and
 * sent perceptions.
 * <p>
 * The goal of the RP Manager is to handle the RP of the game. This means:
 * <ul>
 * <li>run RPActions from clients
 * <li>manage RPWorld
 * <li>control triggers for events
 * <li>control AI
 * </ul>
 * <p>
 * This is a HUGE task that is very complex.<br>
 * Hence we split that behaviour in different class:
 * <ul>
 * <li>IRuleProcessor will handle al the RP logic and run actions from client.
 * This class will also implement AI, triggers, events, rules, etc...
 * <li>RPWorld will handle all the world storage and management logic.
 * </ul>
 *
 * @author miguel
 */
public class RPServerManager extends Thread {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(RPServerManager.class);

	/** The thread will be running while keepRunning is true */
	private volatile boolean keepRunning;

	/** isFinished is true when the thread has really exited. */
	private volatile boolean isfinished;

	/** The time elapsed between 2 turns. */
	private long turnDuration;

	/** The number of the turn that we are executing now */
	private int turn;

	/** The scheduler needed to organize actions */
	private RPScheduler scheduler;

	/** The ruleProcessor that the scheduler will use to execute the actions */
	private IRPRuleProcessor ruleProcessor;

	/** The place where the objects are stored */
	private RPWorld world;

	private Statistics stats;

	/** The networkServerManager so that we can send perceptions */
	private INetworkServerManager netMan;

	/** The PlayerEntryContainer so that we know where to send perceptions */
	private PlayerEntryContainer playerContainer;

	private List<PlayerEntry> playersToRemove;

	private Map<RPObject, List<TransferContent>> contentsToTransfer;

	/**
	 * Constructor
	 *
	 * @param netMan
	 *            the NetworkServerManager so that we can send message
	 * @throws Exception in case of an unexpected error
	 */
	public RPServerManager(INetworkServerManager netMan) throws Exception {
		super("RPServerManager");

		try {
			stats = Statistics.getStatistics();
			keepRunning = true;
			isfinished = false;

			scheduler = new RPScheduler();
			contentsToTransfer = new HashMap<RPObject, List<TransferContent>>();
			playerContainer = PlayerEntryContainer.getContainer();

			playersToRemove = new LinkedList<PlayerEntry>();
			this.netMan = netMan;

			Configuration conf = Configuration.getConfiguration();
			/*
			 * This method loads the extensions that implement the game server
			 * code.
			 */
			initializeExtensions(conf);

			String duration = conf.get("turn_length");
			turnDuration = Long.parseLong(duration);
			turn = 0;
		} catch (Exception e) {
			logger.warn("ABORT: Unable to create RPZone, RPRuleProcessor or RPAIManager instances",
			        e);
			throw e;
		}
	}

	/**
	 * This method loads the extensions: IRPRuleProcessor and IRPWorld that are
	 * going to be used to implement your game. This method loads these class
	 * from the class names passed as arguments in Configuration
	 *
	 * @param conf
	 *            the Configuration class
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	protected void initializeExtensions(Configuration conf) throws ClassNotFoundException,
			IllegalArgumentException, SecurityException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {

		Class<?> worldClass = Class.forName(conf.get("world", "marauroa.server.game.rp.RPWorld"));
		// call the get() method without parameters to retrieve the singleton
		// instance
		world = (RPWorld) worldClass.getDeclaredMethod("get", new Class[0]).invoke(null, (Object[]) null);
		RPWorld.set(world);
		world.onInit();

		Class<?> ruleProcessorClass = Class.forName(conf.get("ruleprocessor", "marauroa.server.game.rp.RPRuleProcessorImpl"));
		// call the get() method without parameters to retrieve the singleton
		// instance
		ruleProcessor = (IRPRuleProcessor) ruleProcessorClass.getDeclaredMethod("get", new Class[0]).invoke(null, (Object[]) null);
		ruleProcessor.setContext(this);
	}


	/**
	 * This method returns the actual turn number.
	 *
	 * @return actual turn number
	 */
	public int getTurn() {
		return turn;
	}

	/**
	 * gets the duration of a turn
	 *
	 * @return duration
	 */
	public long getTurnDuration() {
		return turnDuration;
	}

	/**
	 * This method finishes the thread that runs the RPServerManager. It calls the
	 * RPWorld.onFinish() method.
	 */
	public void finish() {
		keepRunning = false;

		while (isfinished == false) {
			Thread.yield();
		}

		try {
			world.onFinish();
		} catch (Exception e) {
			logger.error("error while finishing RPServerManager", e);
		}
	}

	/**
	 * Adds an action for the next turn
	 *
	 * @param object
	 *            the object that casted the action
	 * @param action
	 *            the action itself
	 * @throws ActionInvalidException
	 */
	public void addRPAction(RPObject object, RPAction action) throws ActionInvalidException {
		if (logger.isDebugEnabled()) {
			logger.debug("Added action: " + action);
		}

		scheduler.addRPAction(object, action, ruleProcessor);
	}

	/**
	 * This method decide if an client runs a compatible version of the game
	 *
	 * @param game
	 *            the game name
	 * @param version
	 *            the game version as a string
	 * @return true if it is compatible.
	 */
	public boolean checkGameVersion(String game, String version) {
		return ruleProcessor.checkGameVersion(game, version);
	}

	/**
	 * Creates an account for a player in the game.
	 *
	 * @param username
	 *            player's username
	 * @param password
	 *            player's password
	 * @param email
	 *            player's email
	 * @param address
	 *            ip address of client
	 * @return a Result indicating if account creation was done successfully or not.
	 */
	public AccountResult createAccount(String username, String password, String email, String address) {
		try {
			if (!Boolean.parseBoolean(Configuration.getConfiguration().get("allow_account_creation", "true"))) {
				return new AccountResult(Result.FAILED_CREATE_ON_MAIN_INSTEAD, username);
			}
		} catch (IOException e) {
			logger.error(e, e);
		}

		// check account creation limits
		try {
			if (DAORegister.get().get(AccountDAO.class).isAccountCreationLimitReached(address)) {
				return new AccountResult(Result.FAILED_TOO_MANY, username);
			}
		} catch (SQLException e) {
			logger.error(e, e);
			return new AccountResult(Result.FAILED_EXCEPTION, username);
		} catch (IOException e) {
			logger.error(e, e);
			return new AccountResult(Result.FAILED_EXCEPTION, username);
		}

		// forward the creation request to the game
		return ruleProcessor.createAccount(username, password, email);
	}

	/**
	 * Creates a character for a account of a player
	 *
	 * @param username
	 *            player's username
	 * @param character
	 * @param template
	 *            the template we are going to use to create the object.
	 * @param address
	 *            ip address of client
	 * @return a Result indicating if account creation was done successfully or
	 *         if it is not the cause.
	 */
	public CharacterResult createCharacter(String username, String character, RPObject template, String address) {
		try {
			if (!Boolean.parseBoolean(Configuration.getConfiguration().get("allow_account_creation", "true"))) {
				return new CharacterResult(Result.FAILED_CREATE_ON_MAIN_INSTEAD, character, template);
			}
		} catch (IOException e) {
			logger.error(e, e);
		}

		// check account creation limits
		try {
			if (DAORegister.get().get(CharacterDAO.class).isCharacterCreationLimitReached(username, address)) {
				return new CharacterResult(Result.FAILED_TOO_MANY, character, template);
			}
		} catch (SQLException e) {
			logger.error(e, e);
			return new CharacterResult(Result.FAILED_EXCEPTION, character, template);
		} catch (IOException e) {
			logger.error(e, e);
			return new CharacterResult(Result.FAILED_EXCEPTION, character, template);
		}
		return ruleProcessor.createCharacter(username, character, template);
	}

	private Perception getPlayerPerception(PlayerEntry entry) {
		Perception perception = null;

		IRPZone.ID id = new IRPZone.ID(entry.object.get("zoneid"));
		IRPZone zone = world.getRPZone(id);

		if (entry.requestedSync == false) {
			perception = zone.getPerception(entry.object, Perception.DELTA);
		} else {
			entry.requestedSync = false;
			perception = zone.getPerception(entry.object, Perception.SYNC);
		}

		return perception;
	}

	private void sendPlayerPerception(PlayerEntry entry, Perception perception, RPObject playerObject) {
		if (perception == null) {
			/** Until player enters game perception is null */
			return;
		}

		MessageS2CPerception messages2cPerception = new MessageS2CPerception(entry.channel,
		        perception);

		stats.add("Perceptions " + (perception.type == 0 ? "DELTA" : "SYNC"), 1);

		/*
		 * The perception is build of two parts: the general information and the
		 * private information about our object. This private information
		 * consists only of attributes that are not visible to every player but
		 * the owner, because visible attributes are already stored in the
		 * perception.
		 */

		if (perception.type == Perception.SYNC) {
			RPObject copy = new RPObject();
			copy.fill(playerObject);
			if (!playerObject.isHidden()) {
				copy.clearVisible(true);
			}
			messages2cPerception.setMyRPObject(copy, null);
		} else {
			RPObject added = new RPObject();
			RPObject deleted = new RPObject();

			try {
				playerObject.getDifferences(added, deleted);
				if (!playerObject.isHidden()) {
					added.clearVisible(false);
					deleted.clearVisible(false);
				}

				if (added.size() == 0) {
					added = null;
				}

				if (deleted.size() == 0) {
					deleted = null;
				}
			} catch (Exception e) {
				logger.error("Error getting object differences", e);
				logger.error(playerObject);
				added = null;
				deleted = null;
			}
			messages2cPerception.setMyRPObject(added, deleted);
		}

		messages2cPerception.setClientID(entry.clientid);
		messages2cPerception.setPerceptionTimestamp(entry.getPerceptionTimestamp());
		messages2cPerception.setProtocolVersion(entry.getProtocolVersion());

		netMan.sendMessage(messages2cPerception);
	}

	private void buildPerceptions() {
		playersToRemove.clear();

		/** We reset the cache at Perceptions */
		MessageS2CPerception.clearPrecomputedPerception();

		for (PlayerEntry entry : playerContainer) {
			try {
				// Before creating the perception we check the player is still there.
				if(entry.isTimeout()) {
					logger.info("Request (TIMEOUT) disconnection of Player " + entry.channel.socket().getRemoteSocketAddress());
					playersToRemove.add(entry);
					continue;
				}

				if (entry.state == ClientState.GAME_BEGIN) {
					Perception perception = getPlayerPerception(entry);
					sendPlayerPerception(entry, perception, entry.object);
				}
			} catch (Exception e) {
				logger.error("Removing player(" + entry.clientid + ") because it caused a Exception while contacting it", e);
				playersToRemove.add(entry);
			}

		}

		for (PlayerEntry entry : playersToRemove) {
			logger.warn("RP Disconnecting entry: " + entry);
			netMan.disconnectClient(entry.channel);
		}
	}

	/** This method is called when a player is added to the game */
	public boolean onInit(RPObject object) throws RPObjectInvalidException {
		return ruleProcessor.onInit(object);
	}

	/** This method is called when a player leaves the game */
	public boolean onExit(RPObject object) throws RPObjectNotFoundException {
		scheduler.clearRPActions(object);
		contentsToTransfer.remove(object);

		return ruleProcessor.onExit(object);
	}

	/** This method is called when connection to client is closed */
	public void onTimeout(RPObject object) throws RPObjectNotFoundException {
		scheduler.clearRPActions(object);
		contentsToTransfer.remove(object);

		ruleProcessor.onTimeout(object);
	}

	private void deliverTransferContent() {
		synchronized (contentsToTransfer) {
			for (Map.Entry<RPObject, List<TransferContent>> val : contentsToTransfer.entrySet()) {
				RPObject target = val.getKey();
				List<TransferContent> content = val.getValue();

				PlayerEntry entry = playerContainer.get(target);
				if(entry==null) {
					logger.warn("Entry for player ("+target+") does not exist: " + playerContainer, new Throwable());
					continue;
				}


				if (content == null) {
					logger.warn("content is null");
				}
				if (!entry.contentToTransfer.isEmpty()) {
					// prevent DoS if the client never confirms the Transfer offer
					if (entry.contentToTransfer.size() > 30) {
						synchronized (entry.contentToTransfer) {
							for (int i = 0; i < 10; i++) {
								entry.contentToTransfer.remove(0);
							}
						}
					}
					logger.warn("Adding to existing contentToTransfer for player " + entry.character + " old: " + entry.contentToTransfer + " added " + content);
				}
				entry.contentToTransfer.addAll(content);

				MessageS2CTransferREQ mes = new MessageS2CTransferREQ(entry.channel, content);
				mes.setClientID(entry.clientid);
				mes.setProtocolVersion(entry.getProtocolVersion());

				netMan.sendMessage(mes);
			}

			contentsToTransfer.clear();
		}
	}

	/** This method is triggered to send content to the clients */
	public void transferContent(RPObject target, List<TransferContent> content) {
		synchronized (contentsToTransfer) {
			contentsToTransfer.put(target, content);
		}
	}

	/** This method is triggered to send content to the clients */
	public void transferContent(RPObject target, TransferContent content) {
		List<TransferContent> list = new LinkedList<TransferContent>();
		list.add(content);

		transferContent(target, list);
	}

	@Override
	public void run() {
		try {
			long start = System.nanoTime();
			long stop;
			long delay;
			long timeStart = 0;
			long[] timeEnds = new long[12];

			while (keepRunning) {
				stop = System.nanoTime();

				logger.debug("Turn time elapsed: " + ((stop - start) / 1000) + " microsecs");
				delay = turnDuration - ((stop - start) / 1000000);
				if (delay < 0) {
					StringBuilder sb = new StringBuilder();
					for (long timeEnd : timeEnds) {
						sb.append(" " + (timeEnd - timeStart));
					}

					logger.warn("Turn duration overflow by " + (-delay) + " ms: "
					        + sb.toString());
				} else if (delay > turnDuration) {
					logger.error("Delay bigger than Turn duration. [delay: " + delay
					        + "] [turnDuration:" + turnDuration + "]");
					delay = 0;
				}

				// only sleep when the turn delay is > 0
				if (delay > 0) {
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						// ignore
					}
				}

				start = System.nanoTime();
				timeStart = System.currentTimeMillis();

				playerContainer.getLock().requestWriteLock();

				try {
					timeEnds[0] = System.currentTimeMillis();

					/* Get actions that players send */
					scheduler.nextTurn();
					timeEnds[1] = System.currentTimeMillis();

					/* Execute them all */
					scheduler.visit(ruleProcessor);
					timeEnds[2] = System.currentTimeMillis();

					/* Compute game RP rules to move to the next turn */
					ruleProcessor.endTurn();
					timeEnds[3] = System.currentTimeMillis();

					/* Send content that is waiting to players */
					deliverTransferContent();
					timeEnds[4] = System.currentTimeMillis();

					/* Tell player what happened */
					buildPerceptions();
					timeEnds[5] = System.currentTimeMillis();

					/* save players regularly to the db */
					savePlayersPeriodicly();
					timeEnds[6] = System.currentTimeMillis();

					/* Move zone to the next turn */
					world.nextTurn();
					timeEnds[7] = System.currentTimeMillis();

					turn++;

					ruleProcessor.beginTurn();
					timeEnds[8] = System.currentTimeMillis();
				} finally {
					playerContainer.getLock().releaseLock();
					timeEnds[9] = System.currentTimeMillis();
				}
				try {
					stats.set("Objects now", world.size());
				} catch ( ConcurrentModificationException e) {
					//TODO: size is obviously not threadsafe as it asks the underlying zone.objects for its sizes, which are not threadsafe.
				}
				timeEnds[10] = System.currentTimeMillis();
				TransactionPool.get().kickHangingTransactionsOfThisThread();
				timeEnds[11] = System.currentTimeMillis();
			}
		} catch (Throwable e) {
			logger.error("Unhandled exception, server will shut down.", e);
		} finally {
			isfinished = true;
		}
	}

	private void savePlayersPeriodicly() {
		for (PlayerEntry entry : playerContainer) {
			try {
				// do not use = 0 because we need a little time until the
				// player object is fully initialized (e. g. has a charname)
				if (entry.getThisPerceptionTimestamp() % 2000 == 1999) {
					entry.storeRPObject(entry.object);
				}
			} catch(Exception e) {
				String name = "null";
				if (entry != null) {
					name = entry.character;
				}
				logger.error("Error while storing player " + name, e);
			}
		}
	}

	/**
	 * This method disconnects a player from the server.
	 *
	 * @param object
	 *            the player object that we want to disconnect from world
	 */
	public void disconnectPlayer(RPObject object) {
		PlayerEntry entry = playerContainer.get(object);
		if (entry == null) {
			/*
			 * There is no player entry for such channel This is not necesaryly
			 * an error, as the connection could be anything else but an arianne
			 * client or we are just disconnecting a player that logout
			 * correctly.
			 */
			logger.warn("There is no PlayerEntry associated to this RPObject.");
			return;
		}

		netMan.disconnectClient(entry.channel);
	}

	/**
	 * This method exposes network layer connection validator so game logic can
	 * handle it.
	 *
	 * @return the connection validator
	 */
	public ConnectionValidator getValidator() {
		return netMan.getValidator();
	}
}
