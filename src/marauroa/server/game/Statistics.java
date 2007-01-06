/* $Id: Statistics.java,v 1.14.2.1 2007/01/06 05:25:05 nhnb Exp $ */
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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import marauroa.common.Configuration;
import marauroa.common.Log4J;

import org.apache.log4j.Logger;

/**
 * This class encapsulate everything related to the statistics recollection and
 * storage.
 */
public class Statistics implements StatisticsMBean {
	/** the logger instance. */
	private static final Logger logger = Log4J.getLogger(Statistics.class);

	static class Variables implements Iterable<String> {
		Map<String, Long> content;

		public Variables() {
			content = new HashMap<String, Long>();
		}

		public void clear() {
			content.clear();
		}

		public void put(String type, long value) {
			content.put(type, value);
		}

		public void add(String type, long value) {
			if (!content.containsKey(type)) {
				put(type, value);
			} else {
				content.put(type, content.get(type) + value);
			}
		}

		public long get(String type) {
			return content.get(type);
		}

		public Iterator<String> iterator() {
			return content.keySet().iterator();
		}

		public void add(Variables var) {
			for (String type : var) {
				add(type, var.get(type));
			}
		}

		public void print(PrintWriter out, double diff) {
			for (String type : content.keySet()) {
				out.println("<attrib name=\"" + escapeML(type) + "\" value=\""
						+ content.get(type) + "\" />");
			}
		}

		public void print(PrintStream out, double diff) {
			out.println("Statistics: " + content.size());
			for (String type : content.keySet()) {
				out.println("<attrib name=\"" + escapeML(type) + "\" value=\""
						+ content.get(type) + "\" />");
			}
		}
	}

	Variables now;

	Variables sinceStart;

	private Date startTime;

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

	public static Statistics getStatistics() {
		if (stats == null) {
			stats = new Statistics();
		}

		return stats;
	}

	public void set(String type, int value) {
		now.put(type, value);
		sinceStart.put(type, value);
	}

	public void add(String type, int value) {
		now.add(type, value);
		sinceStart.add(type, value);
	}

	public void print() {
		try {
			Configuration conf = Configuration.getConfiguration();
			String webfolder = conf.get("server_stats_directory");

			Date actualTime = new Date();
			double diff = (actualTime.getTime() - startTime.getTime()) / 1000;

			if ((actualTime.getTime() - lastStatisticsEventAdded.getTime()) > 60000) {
				lastStatisticsEventAdded = actualTime;

				JDBCPlayerDatabase database = (JDBCPlayerDatabase) JDBCPlayerDatabase
						.getDatabase();
				Transaction transaction = database.getTransaction();

				database.addStatisticsEvent(transaction, now);
				transaction.commit();

				now.clear();
				init();
			}

			PrintWriter out = new PrintWriter(new FileOutputStream(webfolder
					+ "server_stats.xml"));
			out.println("<statistics time=\"" + (actualTime.getTime() / 1000)
					+ "\">");
			out.println("  <uptime value=\"" + diff + "\"/>");

			long totalMemory = Runtime.getRuntime().totalMemory() / 1024;
			long usedMemory = totalMemory
					- (Runtime.getRuntime().freeMemory() / 1024);

			out.println("  <memory total=\"" + totalMemory + "\" used=\""
					+ usedMemory + "\"/>");
			logger.info("Total/Used memory: " + totalMemory + "/" + usedMemory);

			sinceStart.print(out, diff);
			out.println("</statistics>");
			out.close();
		} catch (Exception e) {
			logger.error("error while printing statistics", e);
		}
	}

	public long get(String type) {
		return sinceStart == null ? -1 : sinceStart.get(type);
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
