/* $Id: createaccount.java,v 1.14.6.4 2007/07/01 19:44:08 nhnb Exp $ */
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

import java.util.LinkedList;
import java.util.List;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.crypto.Hash;
import marauroa.common.game.AttributeNotFoundException;
import marauroa.common.game.RPObject;
import marauroa.server.game.IPlayerDatabase;
import marauroa.server.game.JDBCPlayerDatabase;
import marauroa.server.game.PlayerDatabaseFactory;
import marauroa.server.game.Transaction;

import org.apache.log4j.Logger;

/**
 * This is the base class to extend in order to create an account. The class
 * provide a few methods of which you inherit and implement.
 */
public abstract class createaccount {

	private static final Logger logger = Log4J.getLogger(createaccount.class);

	public enum Result {
		OK_ACCOUNT_CREATED, FAILED_EMPTY_STRING, FAILED_INVALID_CHARACTER_USED, FAILED_STRING_SIZE, FAILED_PLAYER_EXISTS, FAILED_EXCEPTION
	}

	/**
	 * This class store some basic information about the type of param, its name
	 * it default value and the allowed size ( > min and < max )
	 */
	public static class Information {

		public String param;

		public String name;

		public String value;

		public int min;

		public int max;

		public Information(String param, String name) {
			this.param = param;
			this.name = name;
			this.value = "";
			this.max = 256;
		}

		public Information(String param, String name, int min, int max) {
			this.param = param;
			this.name = name;
			this.value = "";
			this.min = min;
			this.max = max;
		}
	}

	protected List<Information> information;

	public createaccount() {
		information = new LinkedList<Information>();

		information.add(new Information("-u", "username", 4, 20));
		information.add(new Information("-p", "password", 4, 256));
		information.add(new Information("-e", "email"));
		information.add(new Information("-c", "character", 4, 20));
	}

	protected String get(String name) throws AttributeNotFoundException {
		for (Information item : information) {
			if (item.name.equals(name)) {
				return item.value;
			}
		}

		throw new AttributeNotFoundException(name);
	}

	/**
	 * Implement this method on the subclass in order to create an object that
	 * will be inserted into the database by the createaccount class. You are
	 * given a playerDatabase instance so that you can get valid rpobjects' ids
	 */
	public abstract RPObject populatePlayerRPObject(IPlayerDatabase playerDatabase) throws Exception;

	protected Result run(String[] args) {
		int i = 0;
		String iniFile = "marauroa.ini";

		while (i != args.length) {
			for (Information item : information) {
				if (args[i].equals(item.param)) {
					item.value = args[i + 1];
					if (!"password".equals(item.name)) {
						logger.info(item.name + "=" + item.value);
					}
					break;
				}
			}

			if (args[i].equals("-i")) {
				iniFile = args[i + 1];
			}

			if (args[i].equals("-h")) {
				logger.info("createaccount application for Marauroa");
				for (Information item : information) {
					logger.info(item.param + " to use/add " + item.name);
				}

				logger.info("-i" + " to to define .ini file");

				return Result.FAILED_EMPTY_STRING;
			}

			++i;
		}

		Transaction trans = null;

		try {
			Configuration.setConfigurationFile(iniFile);
			logger.info("Trying to create username(" + get("username") + "), character(" + get("character") + ")");

			JDBCPlayerDatabase playerDatabase = (JDBCPlayerDatabase) PlayerDatabaseFactory.getDatabase();
			trans = playerDatabase.getTransaction();

			trans.begin();

			logger.info("Checking for null/empty string");
			for (Information item : information) {
				if (item.value.equals("")) {
					logger.info("String is empty or null: " + item.name);
					return Result.FAILED_EMPTY_STRING;
				}
			}

			logger.info("Checking for valid string");
			for (Information item : information) {
				if (!playerDatabase.validString(item.value)) {
					logger.info("String not valid: " + item.name);
					return Result.FAILED_INVALID_CHARACTER_USED;
				}
			}

			logger.info("Checking string size");
			for (Information item : information) {
				if ((item.value.length() > item.max) || (item.value.length() < item.min)) {
					logger.info("String size not valid: " + item.name);
					return Result.FAILED_STRING_SIZE;
				}
			}

			logger.info("Checking impersonation");
			String name = get("username").trim().toLowerCase();
			name = name.replaceAll("[ _.,;.\\-\\\\ \"§$%&/()='<>|*+~#]", " ");
			if (name.startsWith(" ") || name.endsWith(" ") || (name.indexOf("gm ") > -1) || (name.indexOf(" gm") > -1)
			        || name.startsWith("gm") || name.endsWith("gm") || (name.indexOf("  ") > -1)) {
				logger.warn("Possible impersonation: " + get("username"));
				return Result.FAILED_INVALID_CHARACTER_USED;
			}

			logger.info("Checking if player exists");
			if (playerDatabase.hasPlayer(trans, get("username"))) {
				logger.info("ERROR: Player exists");
				return Result.FAILED_PLAYER_EXISTS;
			}

			logger.info("Adding player");
			playerDatabase.addPlayer(trans, get("username"), Hash.hash(get("password")), get("email"));

			RPObject object = populatePlayerRPObject(playerDatabase);

			playerDatabase.addCharacter(trans, get("username"), get("character"), object);
			logger.info("Correctly created");

			trans.commit();
		} catch (Exception e) {
			logger.error("Failed: " + e.getMessage(), e);

			try {
				trans.rollback();
			} catch (Exception ae) {
				logger.error("Failed Rollback: " + ae.getMessage());
			}
			return Result.FAILED_EXCEPTION;
		}

		return Result.OK_ACCOUNT_CREATED;
	}
}
