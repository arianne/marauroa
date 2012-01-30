/***************************************************************************
 *                   (C) Copyright 2003-2009 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * This is a convenience class for initializing log4j
 * 
 * Please when using Log4j follow the next rules:<ul>
 * <li><b>debug</b> just debug info that may be need to find a bug.
 * <li><b>info</b> is just important information that we should be aware to spot behaviors on application.
 * <li><b>warn</b> is a problem that application will handle itself
 * <li><b>error</b> is a big problem on the application that it can't handle.
 * <li><b>fatal</b> is such a problem that the application will stop.
 * </ul>
 * 
 * @author Matthias Totz
 */
public class Log4J {

	private static final String LOG4J_PROPERTIES = "log4j.properties";

	/** default properties */
	private static final String DEFAULT_PROPERTIES = "log4j.rootLogger=INFO, Console\n"
	        + "log4j.appender.Console=org.apache.log4j.ConsoleAppender\n"
	        + "log4j.appender.Console.layout=org.apache.log4j.PatternLayout\n"
	        + "log4j.appender.Console.threshold=INFO\n"
	        + "log4j.appender.Console.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n\n";

	/** flag indicating a successful configuration */
	private static boolean configured = false;

	/** initializes log4j with the log4j.properties file. */
	public static void init() {
		init(LOG4J_PROPERTIES);
	}

	/**
	 * initializes log4j with a custom properties file.
	 *
	 * @param filename log4j.properties
	 */
	public static void init(String filename) {
		if (configured) {
			return;
		}

		InputStream propsFile = Log4J.class.getClassLoader().getResourceAsStream(filename);
		try {
			Properties props = new Properties();
			if (propsFile == null) {
				System.err.println("Cannot find " + filename + " in classpath. Using default properties.");
				props.load(new ByteArrayInputStream(DEFAULT_PROPERTIES.getBytes()));
			} else {
				System.out.println("Configuring Log4J using " + filename);
				props.load(propsFile);
			}
			PropertyConfigurator.configure(props);
			configured = true;
		} catch (IOException ioe) {
			System.err.println("cannot read property-file " + LOG4J_PROPERTIES + " because "
			        + ioe.getMessage());
		}
	}

	/**
	 * returns a logger for the given class. Use this function instead of
	 * <code>Logger.getLogger(clazz);</code>. If the logging mechanism
	 * changes it will be done here and not in every class using a logger.
	 *
	 * @param clazz
	 *            the Class requesting a logger
	 * @return the logger instance
	 */
	public static marauroa.common.Logger getLogger(Class<?> clazz) {
		return new marauroa.common.Logger(clazz);
	}
}
