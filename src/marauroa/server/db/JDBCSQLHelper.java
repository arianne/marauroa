/* $Id: JDBCSQLHelper.java,v 1.2 2009/07/19 14:35:34 nhnb Exp $ */
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
	public boolean runDBScript(DBTransaction transaction, String file) {
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
					String query = is.toString();
					logger.debug("runDBScript is executing query " + query);
					transaction.execute(query, null);
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

}
