/* $Id: Statistics.java,v 1.29 2007/06/13 15:35:14 arianne_rpg Exp $ */
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

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.server.game.db.JDBCDatabase;
import marauroa.server.game.db.Transaction;

/**
 * This class encapsulate everything related to the statistics recollection and
 * storage.
 */
public class Statistics implements StatisticsMBean {

	public static final int DATABASE_STATISTICS_LAPSUS = 60000;

	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(Statistics.class);

	/**
	 * This class is very similar to a Map<String, Long> with the extra that
	 * adds some comodity methods like:
	 * <ul>
	 * <li>add
	 * <li>print
	 * </ul>
	 *
	 * @author miguel
	 */
	public static class Variables implements Iterable<String> {

		private Map<String, Long> content;

		/**
		 * Constructor
		 */
		public Variables() {
			content = new HashMap<String, Long>();
		}

		/**
		 * Clear all the variables
		 */
		public void clear() {
			content.clear();
		}

		/**
		 * Put a new variable at the Map
		 *
		 * @param type
		 *            name of the variable
		 * @param value
		 *            its value
		 */
		public void put(String type, long value) {
			content.put(type, value);
		}

		/**
		 * Add value to previously existing variable.
		 *
		 * @param type
		 *            name of the variable
		 * @param value
		 *            value to add
		 */
		public void add(String type, long value) {
			if (!content.containsKey(type)) {
				put(type, value);
			} else {
				content.put(type, content.get(type) + value);
			}
		}

		/**
		 * Returns a variable value
		 *
		 * @param type
		 *            name of the variable
		 * @return its value
		 */
		public long get(String type) {
			return content.get(type);
		}

		/**
		 * Iterate over the variables
		 *
		 * @return an iterator over the variables
		 */
		public Iterator<String> iterator() {
			return content.keySet().iterator();
		}

		/**
		 * Adds to this instance the instance var
		 *
		 * @param var
		 *            a instance of Variables to add to this one.
		 */
		public void add(Variables var) {
			for (String type : var) {
				add(type, var.get(type));
			}
		}

		/**
		 * Prints the variable
		 *
		 * @param out
		 * @param diff
		 */
		public void print(PrintWriter out, double diff) {
			for (String type : content.keySet()) {
				out.println("<attrib name=\"" + escapeXML(type) + "\" value=\"" + content.get(type)
				        + "\" />");
			}
		}
	}

	/** This is the actual values */
	private Variables now;

	/** This is variables values since the server startup */
	private Variables sinceStart;

	/** Server start time */
	private long startTime;

	/** The date of the last statistics event added to database */
	private long lastStatisticsEventAdded;

	private Statistics() {
		startTime = System.currentTimeMillis();
		lastStatisticsEventAdded = startTime;

		now = new Variables();
		sinceStart = new Variables();

		init();
	}

	private void init() {
		/** we need these for JDBC Database */
		set("Players online", 0);
		add("Players login", 0);
		add("Players logout", 0);
		add("Players timeout", 0);
		add("Players logged", 0);

		add("Bytes send", 0);
		add("Bytes recv", 0);
	}

	private static Statistics stats;

	/**
	 * Returns an unique instance of Statistics. This is a singleton.
	 *
	 * @return a statistics object
	 */
	public static Statistics getStatistics() {
		if (stats == null) {
			stats = new Statistics();
		}

		return stats;
	}

	/**
	 * Sets an attribute
	 *
	 * @param type
	 *            attribute name
	 * @param value
	 *            its value
	 */
	public void set(String type, int value) {
		now.put(type, value);
		sinceStart.put(type, value);
	}

	/**
	 * Adds an attribute to its existing
	 *
	 * @param type
	 * @param value
	 */
	public void add(String type, int value) {
		now.add(type, value);
		sinceStart.add(type, value);
	}

	/**
	 * Return the value of an attribute since the server start. This method is
	 * used by the Bean interface.
	 *
	 * @param type
	 *            the attribute name
	 */
	public long get(String type) {
		return sinceStart == null ? -1 : sinceStart.get(type);
	}

	/**
	 * Print to $statistics_filename file the content of the statistics object.
	 */
	public void print() {
		try {
			Configuration conf = Configuration.getConfiguration();
			String webfolder = conf.get("statistics_filename");

			long actualTime = System.currentTimeMillis();
			/*
			 * Store statistics to database.
			 */
			addStatisticsEventRow(actualTime);

			PrintWriter out = new PrintWriter(new FileOutputStream(webfolder));

			double diff = (actualTime - startTime) / 1000.0;
			out.println("<statistics time=\"" + (actualTime / 1000) + "\">");
			out.println("  <uptime value=\"" + diff + "\"/>");

			long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
			long usedMemory = totalMemory - (Runtime.getRuntime().freeMemory() / 1024);

			out.println("  <memory total=\"" + totalMemory + "\" used=\"" + usedMemory + "\"/>");
			logger.info("Total/Used memory: " + totalMemory + "/" + usedMemory);

			sinceStart.print(out, diff);
			out.println("</statistics>");
			out.close();
		} catch (Exception e) {
			logger.warn("error while printing statistics", e);
		}
	}

	private void addStatisticsEventRow(long actualTime) throws SQLException {
		if ((actualTime - lastStatisticsEventAdded) > DATABASE_STATISTICS_LAPSUS) {
			lastStatisticsEventAdded = actualTime;

			JDBCDatabase database = JDBCDatabase.getDatabase();
			Transaction transaction = database.getTransaction();

			database.addStatisticsEvent(transaction, now);
			transaction.commit();

			now.clear();
			init();
		}
	}

	/**
	 * escapes special characerers in XML hand HTML.
	 *
	 * @param param
	 *            string to escape
	 * @return escaped strings
	 */
	private static String escapeXML(String param) {
		return param.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(
		        ">", "&gt;");
	}
}
