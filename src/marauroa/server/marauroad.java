/* $Id: marauroad.java,v 1.94.2.1 2010/07/20 18:45:49 nhnb Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.server;

// marauroa stuff
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.Logger;
import marauroa.common.crypto.RSAKey;
import marauroa.server.db.DatabaseConnectionException;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.Statistics;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.net.INetworkServerManager;

/**
 * The launcher of the whole Marauroa Server.<br>
 * Marauroa is an arianne server application with an TCP transport.<br>
 * Marauroa works by loading core class from your game server.
 * <p>
 * Marauroa server is and it is built using threads. marauroad has the following
 * set of threads:
 * <ul>
 * <li> 1 thread to receive data from clients
 * <li> 1 thread to send data to clients
 * <li> 1 thread to handle the data into server actions
 * <li> 1 thread to handle RPG itself.
 * </ul>
 *
 * To denote the active behavior of the thread classes their names include the
 * word Manager.<br>
 * So marauroad has:
 * <ul>
 * <li> NetworkManager
 * <li> GameManager
 * <li> RPManager
 * </ul>
 *
 * NetworkManager is the active thread that handles messages that come from the
 * clients and converts them from a stream of bytes to a real Message object.
 * See the Message Types document to understand what each message is for.<br>
 * The pseudo code behind NetworkManager is:
 *
 * <pre>
 *	 forever
 *	 {
 *	 Read stream from network
 *	 Convert to a Message
 *	 store in our queue
 *	 }
 * </pre>
 *
 * One level (conceptually) over NetworkManager is the GameManager, this is the
 * part of the server that handles everything so that we can make the server
 * work. Its main task is to process messages from clients and modify the state
 * on the server to reflect the reply to that action, mainly related to:
 * <ul>
 * <li> Login
 * <li> Logout
 * <li> ChooseCharacter
 * <li> Actions
 * <li> Transfer Content
 * </ul>
 *
 * See GameManager for a deeper understanding about what it does exactly.<br>
 * The hardest part of the Manager is to consider all the special cases and all
 * the exceptions that can happen. The main pseudo code of the GameManager, if
 * we skip exceptions, is:
 *
 * <pre>
 *	 forever
 *	 {
 *	 Wait for Message to be available
 *
 *	 if(Message is Login)
 *	 {
 *	 check player.
 *	 ask for character
 *	 }
 *
 *	 if(Message is Choose Character)
 *	 {
 *	 check character
 *	 add to game
 *	 }
 *
 *	 if(Message is Action)
 *	 {
 *	 add action to game
 *	 }
 *
 *	 if(Message is Transfer Request ACK)
 *	 {
 *	 send client the content requested
 *	 }
 *
 *	 if(Message is Logout)
 *	 {
 *	 remove from game
 *	 }
 *	 }
 * </pre>
 *
 * And finally RPManager is the active thread that keeps executing actions.<br>
 * Marauroa is, as you know, turn based, so actions when received are queued for
 * the next turn, and when that turn is reached all the actions pending on that
 * turn are executed.
 * <p>
 *
 * The idea in RPManager is to split up complexity as much as possible: we have
 * 2 entities to help it: Scheduler and RuleManager.
 *
 * <pre>
 *	 forever
 *	 {
 *	 for each action scheduled for this turn
 *	 {
 *	 run action in RuleManager
 *	 }
 *
 *	 Send Perceptions
 *
 *	 wait until turn is completed
 *	 next turn
 *	 }
 * </pre>
 *
 * Scheduler handles the actions as they are sent by the GameManager.<br>
 * RuleManager is a class that encapsulates all the implementation related to
 * rules.
 *
 */
public class marauroad extends Thread {

	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(marauroad.class);

	/** Which marauroa version are we running */
	private static final String VERSION = "3.8.1";

	/** Marauroa is a singleton. */
	private static marauroad marauroa;

	/** A network manager object to handle network events */
	private INetworkServerManager netMan;

	/** A game manager object to handle server glue logic and database stuff */
	private GameServerManager gameMan;

	/** Finally a rp game object that is coded on game's server plugin. */
	private RPServerManager rpMan;

	private static void setArguments(String[] args) {
		int i = 0;

		while (i != args.length) {
			if (args[i].equals("-c")) {
				Configuration.setConfigurationFile(args[i + 1]);
			} else if (args[i].equals("-h")) {
				System.out.println("Marauroa - an open source multiplayer online framework for game development -");
				System.out.println("Running on version " + VERSION);
				System.out.println("(C) 1999-2009 Miguel Angel Blanch Lardin and the Arianne project");
				System.out.println();
				System.out.println("usage: [-c gamefile]");
				System.out
						.println("\t-c: to choose a configuration file different of marauroa.ini or to use a");
				System.out.println("\t    different location to the file.");
				System.out.println("\t-h: print this help message");
				System.exit(0);
			}
			++i;
		}
	}

	/**
	 * Entry point
	 *
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		System.out.println("Marauroa - arianne's open source multiplayer online framework for game development -");
		System.out.println("Running on version " + VERSION);
		System.out.println("(C) 1999-2009 Miguel Angel Blanch Lardin and the Arianne project");
		System.out.println();
		System.out.println("This program is free software; you can redistribute it and/or modify");
		System.out.println("it under the terms of the GNU General Public License as published by");
		System.out.println("the Free Software Foundation; either version 2 of the License, or");
		System.out.println("(at your option) any later version.");
		System.out.println();
		System.out.println("This program is distributed in the hope that it will be useful,");
		System.out.println("but WITHOUT ANY WARRANTY; without even the implied warranty of");
		System.out.println("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		System.out.println("GNU General Public License for more details.");
		System.out.println();
		System.out.println("You should have received a copy of the GNU General Public License");
		System.out.println("along with this program; if not, write to the Free Software");
		System.out.println("Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA");
		System.out.println();

		marauroad.setArguments(args);
		
		String log4jConfiguration = null;

		try {
			Configuration conf = Configuration.getConfiguration();
			log4jConfiguration = conf.get("log4j_url");
		} catch (IOException e) {
			System.out.println("ERROR: Marauroa can't find configuration file.");
			System.out.println("Run game configuration to get a valid \"server.ini\" file");
			System.exit(1);
		}
		
		if(log4jConfiguration==null) {
			log4jConfiguration="marauroa/server/log4j.properties";
		}
		
		// Initialize Loggging
		try {
		  Log4J.init(log4jConfiguration);
		} catch(Exception e) {
			System.out.println("ERROR: Marauroa can't initialize logging.");
			System.out.println("Verify you have created log/ directory.");
			System.exit(1);
		}
		
		// Check access to database is possible.
		try {
			new DatabaseFactory().initializeDatabase();
		} catch (DatabaseConnectionException e) {
			System.out.println("ERROR: Marauroa can't connect to database");
			System.out.println("Verify \"server.ini\" file to make sure access to database is possible.");
			System.exit(1);
		}
		
		marauroad.getMarauroa().start();
	}

	private void createBeanForStatistics() {
		// Adding a Bean for statistical access using jmanager
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			// Unique identification of MBeans
			Statistics statBean = Statistics.getStatistics();
			// Uniquely identify the MBeans and register them with the platform
			// MBeanServer
			ObjectName statName = new ObjectName("marauroad:name=Statistics");
			mbs.registerMBean(statBean, statName);
			logger.debug("Statistics bean registered.");
		} catch (Exception e) {
			logger.error("cannot register statistics bean, continuing anyway.", e);
		}
	}

	@Override
	public synchronized void run() {
		long startTime = System.currentTimeMillis();
		logger.debug("marauroad thread started");

		createBeanForStatistics();

		boolean finish = false;

		if (!init()) {
			// initialize failed
			System.exit(-1);
		}

		double startupTime = (System.currentTimeMillis() - startTime) / 1000.0;

		logger.info("marauroa is up and running... (startup time: " + (long)(startupTime*10)/10. + " s)");
		while (!finish) {
			try {
				Statistics.getStatistics().print();
				wait(Statistics.DATABASE_STATISTICS_LAPSUS);
			} catch (InterruptedException e) {
				finish = true;
			}
		}

		finish();
		logger.debug("exiting marauroad thread");
	}

	/**
	 * Constructor
	 */
	protected marauroad() {
		super("marauroad");
	}

	/**
	 * returns the marauroad object
	 *
	 * @return marauroad
	 */
	public static marauroad getMarauroa() {
		if (marauroa == null) {
			marauroa = new marauroad();
		}

		return marauroa;
	}

	/**
	 * Initializes the game. Returns true when all is OK, else false (this may
	 * terminate the server).
	 *
	 * @return true, in case the startup was successful, false otherwise
	 */
	public boolean init() {
		logger.debug("staring initialize");
		MarauroaUncaughtExceptionHandler.setup();

		try {
			netMan = new marauroa.server.net.nio.NIONetworkServerManager();
			netMan.start();
		} catch (Exception e) {
			logger.error("Marauroa can't create NetworkServerManager.\n" + "Reasons:\n"
					+ "- You are already running a copy of Marauroa on the same TCP port\n"
					+ "- You haven't specified a valid configuration file\n"
					+ "- You haven't create database\n"
					+ "- You have invalid username and password to connect to database\n", e);
			return false;
		}

		try {
			rpMan = new RPServerManager(netMan);
			rpMan.start();
		} catch (Exception e) {
			logger.error(
							"Marauroa can't create RPServerManager.\n"
									+ "Reasons:\n"
									+ "- You haven't specified a valid configuration file\n"
									+ "- You haven't correctly filled the values related to game configuration. Use generateini application to create a valid configuration file.\n"
									+ "- There may be an error in the Game startup method.\n", e);
			return false;
		}

		try {
			RSAKey key = new RSAKey(new BigInteger(Configuration.getConfiguration().get("n")),
					new BigInteger(Configuration.getConfiguration().get("d")), new BigInteger(
							Configuration.getConfiguration().get("e")));

			gameMan = new GameServerManager(key, netMan, rpMan);			
			gameMan.start();
		} catch (Exception e) {
			logger.error(
							"Marauroa can't create GameServerManager.\n"
									+ "Reasons:\n"
									+ "- You haven't specified a valid configuration file\n"
									+ "- You haven't correctly filled the values related to server information configuration. Use generateini application to create a valid configuration file.\n",
							e);
			return false;
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			private Logger log = Log4J.getLogger(marauroad.class);

			@Override
			public void run() {
				log.warn("User requesting shutdown");
				finish();
				log.info("Shutdown completed. See you later!");
			}
		});

		logger.debug("initialize finished");
		return true;
	}

	/**
	 * shuts down Marauroa
	 */
	public void finish() {
		netMan.finish();
		gameMan.finish();
		DBCommandQueue.get().finish();
	}
}
