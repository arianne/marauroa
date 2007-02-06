/* $Id: marauroad.java,v 1.48 2007/02/06 20:56:46 arianne_rpg Exp $ */
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
package marauroa.server;

// marauroa stuff
import java.lang.management.ManagementFactory;
import java.math.BigInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.crypto.RSAKey;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.Statistics;
import marauroa.server.game.rp.RPServerManager;
import marauroa.server.net.INetworkServerManager;

/** The launcher of the whole Marauroa Server. 
 *  Run this class to make your game server run.
 *  Marauroa works by loading core class from your game server.
 */
public class marauroad extends Thread {
	/** the logger instance. */
	private static final org.apache.log4j.Logger logger = Log4J.getLogger(marauroad.class);

	/** Which marauroa version are we running */
	private static final String VERSION = "2.00";

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
				try {
					Configuration.getConfiguration();
				} catch (Exception e) {
					logger.fatal("Can't find configuration file: "+ args[i + 1], e);
					System.exit(1);
				}
			} else if (args[i].equals("-h")) {
				System.out.println("Marauroa - an open source multiplayer online framework for game development -");
				System.out.println("Running on version " + VERSION);
				System.out.println("(C) 1999-2007 Miguel Angel Blanch Lardin");
				System.out.println();
				System.out.println("usage: [-c gamefile] [-l]");
				System.out.println("\t-c: to choose a configuration file different of marauroa.ini or to use a");
				System.out.println("\t    different location to the file.");
				System.out.println("\t-h: print this help message");
				System.exit(0);
			}
			++i;
		}
	}

	public static void main(String[] args) {
		System.out.println("Marauroa - arianne's open source multiplayer online framework for game development -");
		System.out.println("Running on version " + VERSION);
		System.out.println("(C) 1999-2007 Miguel Angel Blanch Lardin");
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

		// Initialize Loggging
		Log4J.init("marauroa/server/log4j.properties");
		marauroad.setArguments(args);

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
			logger.error("cannot register statistics bean, continuing anyway.",	e);
		}
	}

	@Override
	public synchronized void run() {
		logger.debug("marauroad thread started");
		
		createBeanForStatistics();

		boolean finish = false;
		marauroad instance = marauroad.getMarauroa();

		if (!instance.init()) {
			// initialize failed
			System.exit(-1);
		}

		logger.info("marauroa is up and running...");
		while (!finish) {
			try {
				Statistics.getStatistics().print();
				wait(30000);
			} catch (InterruptedException e) {
				finish = true;
			}
		}

		instance.finish();
		logger.debug("exiting marauroad thread");
	}

	private marauroad() {
		super("marauroad");
	}

	public static marauroad getMarauroa() {
		if (marauroa == null) {
			marauroa = new marauroad();
		}

		return marauroa;
	}

	/**
	 * initializes the game. returns true when all is ok, else false (this may
	 * terminate the server).
	 */
	public boolean init() {
		logger.debug("staring initialize");
		try {
			netMan = new marauroa.server.net.nio.NIONetworkServerManager();
			netMan.start();
		} catch (Exception e) {
			logger.fatal(
					"Marauroa can't create NetworkServerManager.\n"
					+ "Reasons:\n"
					+ "- You are already running a copy of Marauroa on the same TCP port\n"
					+ "- You haven't specified a valid configuration file\n"
					+ "- You haven't create database\n"
					+ "- You have invalid username and password to connect to database\n",
					e);
			return false;
		}

		try {
			rpMan = new RPServerManager(netMan);
			rpMan.start();
		} catch (Exception e) {
			logger.fatal(
					"Marauroa can't create RPServerManager.\n"
					+ "Reasons:\n"
					+ "- You haven't specified a valid configuration file\n"
					+ "- You haven't correctly filled the values related to game configuration. Use generateini application to create a valid configuration file.\n"
					+ "- There may be an error in the Game startup method.\n",
					e);
			return false;
		}

		try {
			RSAKey key = new RSAKey(
					new BigInteger(Configuration.getConfiguration().get("n")), 
					new BigInteger(Configuration.getConfiguration().get("d")), 
					new BigInteger(Configuration.getConfiguration().get("e")));
			
			gameMan = new GameServerManager(key, netMan, rpMan);
			gameMan.start();
		} catch (Exception e) {
			logger.fatal(
					"Marauroa can't create GameServerManager.\n"
					+ "Reasons:\n"
					+ "- You haven't specified a valid configuration file\n"
					+ "- You haven't correctly filled the values related to server information configuration. Use generateini application to create a valid configuration file.\n",
					e);
			return false;
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Note: Log4J ist shutdown already at this point
				logger.warn("User requesting shutdown");
				finish();
				logger.warn("Shutdown completed. See you later");
			}
		});

		logger.debug("initialize finished");
		return true;
	}

	public void finish() {
		netMan.finish();
		gameMan.finish();
	}
}
