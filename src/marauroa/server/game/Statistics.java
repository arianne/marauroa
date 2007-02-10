/* $Id: Statistics.java,v 1.20 2007/02/10 18:13:39 arianne_rpg Exp $ */
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.server.game.db.JDBCDatabase;
import marauroa.server.game.db.JDBCTransaction;

import org.apache.log4j.Logger;

/**
 * This class encapsulate everything related to the statistics recollection and
 * storage.
 */
public class Statistics implements StatisticsMBean {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Statistics.class);

	/**
	 * This class is very similar to a Map<String, Long> with the extra that 
	 * adds some comodity methods like:<ul> 
	 * <li>add
	 * <li>print
	 * </ul> 
	 * @author miguel
	 */
	public static class Variables implements Iterable<String> {
		private Map<String, Long> content;

		/** Constructor */
		public Variables() {
			content = new HashMap<String, Long>();
		}

		/** Clear all the variables */
		public void clear() {
			content.clear();
		}

		/**
		 * Put a new variable at the Map
		 * @param type name of the variable
		 * @param value its value
		 */
		public void put(String type, long value) {
			content.put(type, value);
		}

		/**
		 * Add value to previously existing variable.
		 * @param type name of the variable
		 * @param value value to add
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
		 * @param type name of the variable
		 * @return its value
		 */
		public long get(String type) {
			return content.get(type);
		}

		/** Iterate over the variables 
		 *  @return an iterator over the variables
         */
		public Iterator<String> iterator() {
			return content.keySet().iterator();
		}

		/** 
		 * Adds to this instance the instance var 
		 * @param var a instance of Variables to add to this one.
		 */
		public void add(Variables var) {
			for (String type : var) {
				add(type, var.get(type));
			}
		}

		/**
		 * Prints the variable 
		 * @param out
		 * @param diff
		 */
		public void print(PrintWriter out, double diff) {
			for (String type : content.keySet()) {
				out.println("<attrib name=\"" + escapeML(type) + "\" value=\""
						+ content.get(type) + "\" />");
			}
		}
	}

	/** This is the actual values */
	private Variables now;

	/** This is variables values since the server startup */
	private Variables sinceStart;

	/** Server start time */
	private Date startTime;

	/** The date of the last statistics event added to database */
	private Date lastStatisticsEventAdded;

	private Statistics() {
		startTime = new Date();

		lastStatisticsEventAdded = new Date();

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
	 * Returns an unique instance of Statistics.
	 * This is a singleton. 
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
	 * @param type attribute name
	 * @param value its value
	 */
	public void set(String type, int value) {
		now.put(type, value);
		sinceStart.put(type, value);
	}

	/**
	 * Adds an attribute to its existing 
	 * @param type
	 * @param value
	 */
	public void add(String type, int value) {
		now.add(type, value);
		sinceStart.add(type, value);
	}

	/** 
	 * Return the value of an attribute since the server start.
	 * This method is used by the Bean interface.
	 * @param type the attribute name
	 */
	public long get(String type) {
		return sinceStart == null ? -1 : sinceStart.get(type);
	}

	/** 
	 * Print to $(webfolder)/server_stats.xml file the content of the statistics object.  
	 */
	public void print() {
		try {
			Configuration conf = Configuration.getConfiguration();
			String webfolder = conf.get("server_stats_directory");

			Date actualTime = new Date();
			double diff = (actualTime.getTime() - startTime.getTime()) / 1000;

			if ((actualTime.getTime() - lastStatisticsEventAdded.getTime()) > 60000) {
				lastStatisticsEventAdded = actualTime;

				JDBCDatabase database = JDBCDatabase.getDatabase();
				JDBCTransaction transaction = database.getTransaction();

				database.addStatisticsEvent(transaction, now);
				transaction.commit();

				now.clear();
				init();
			}

			PrintWriter out = new PrintWriter(new FileOutputStream(webfolder+ "server_stats.xml"));
			out.println("<statistics time=\"" + (actualTime.getTime() / 1000)+ "\">");
			out.println("  <uptime value=\"" + diff + "\"/>");

			long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
			long usedMemory = totalMemory- (Runtime.getRuntime().freeMemory() / 1024);

			out.println("  <memory total=\"" + totalMemory + "\" used=\""+ usedMemory + "\"/>");
			logger.info("Total/Used memory: " + totalMemory + "/" + usedMemory);

			sinceStart.print(out, diff);
			out.println("</statistics>");
			out.close();
		} catch (Exception e) {
			logger.error("error while printing statistics", e);
		}
	}

	/**
	 * escapes special characerers in XML hand HTML.
	 * 
	 * @param param
	 *            string to escape
	 * @return escaped strings
	 */
	private static String escapeML(String param) {
		return param.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
	}
}
