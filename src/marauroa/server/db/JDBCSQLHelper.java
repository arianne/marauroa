/* $Id: JDBCSQLHelper.java,v 1.4 2009/07/30 19:48:08 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2007 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;

import marauroa.common.Log4J;

/**
 * A helper class that runs SQL scripts.
 * 
 * @author miguel
 * 
 */
public class JDBCSQLHelper {

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(JDBCSQLHelper.class);
	private DBTransaction transaction;
	private String command;
	private String commandLower;

	/**
	 * creates a new JDBCSQLHelper
	 *
	 * @param transaction DBTransaction
	 */
	public JDBCSQLHelper(DBTransaction transaction) {
		this.transaction = transaction;
	}

	/**
	 * This method runs a SQL file using the given transaction. You are
	 * responsible of beginning the transaction and commiting the changes or
	 * rollback on error.
	 * 
	 * @param transaction
	 *            The JDBC Transaction that we are going to use.
	 * @param file
	 *            The file name that contains the SQL commands.
	 * @return true if the whole file was executed or false in any other error.
	 */
	public boolean runDBScript(String file) {
		boolean ret = true;
		BufferedReader in = null;

		try {

			InputStream init_file = getClass().getClassLoader().getResourceAsStream(file);
			in = new BufferedReader(new InputStreamReader(init_file));

			StringBuffer is = new StringBuffer();

			String line;
			while ((line = in.readLine()) != null) {
				is.append(line);
				if (line.indexOf(';') != -1) {
					command = is.toString().trim();
					rewriteAndExecuteQuery();
					is = new StringBuffer();
				}
			}

			return ret;
		} catch (SQLException e) {
			logger.error("error running SQL Script (file: " + file + ")", e);
			return false;
		} catch (IOException e) {
			logger.error("error reading SQL Script (file: " + file + ")", e);
			return false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				logger.error(e, e);
			}
		}
	}

	private void rewriteAndExecuteQuery() throws SQLException {
		logger.debug("runDBScript is parsing sql query " + command);
		/*
		commandLower = command.toLowerCase();
		if (commandLower.startsWith("create table ")) {
			rewriteCreateTableStatement();
		}
		*/
		if (command != null) {
			transaction.execute(command, null);
		}
	}

	private void rewriteCreateTableStatement() throws SQLException {

		// handle "if not exists"
		int pos = commandLower.indexOf("if not exists");
		if (pos > -1) {
			String table = commandLower.substring(pos + 13);
			int pos2 = table.indexOf("(");
			table = table.substring(0, pos2 - 1).trim();

			if (transaction.doesTableExist(table)) {
				command = null;
				return;
			} else {
				command = command.replaceAll("[iI][fF] [nN][oO][tT] [eE][xX][iI][sS][tT][sS]", "");
				commandLower = commandLower.replaceAll("if not exists", "");
			}

		}
	}
}
