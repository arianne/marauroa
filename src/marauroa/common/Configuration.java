/* $Id: Configuration.java,v 1.9 2006/12/18 20:08:13 arianne_rpg Exp $ */
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
package marauroa.common;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import marauroa.common.io.Persistence;

/** This class is a basic configuration file manager */
public class Configuration {
	/** the logger instance. */
	private static final org.apache.log4j.Logger logger = Log4J
			.getLogger(Configuration.class);

	private static boolean relativeToHome = false;
	private static String basedir = "";
	private static String configurationFile = "marauroa.ini";

	private Properties properties;

	private static Configuration configuration = null;

	private static boolean persistence = true;

	/**
	 * This method defines the default configuration file for all the instances
	 * of Configuration
	 * 
	 * @param conf the location of the file
	 */
	public static void setConfigurationFile(String conf) {
		relativeToHome = false;
		basedir = "";
		configurationFile = conf;
	}

	/**
	 * This method defines the default configuration file for all the instances
	 * of Configuration
	 * 
	 * @param relativeToHome should this file be placed below the users home directory?
	 * @param basedir directory prefix which is ignore in webstart environment
	 * @param conf the location of the file
	 */
	public static void setConfigurationFile(boolean relativeToHome, String basedir, String conf) {
		Configuration.relativeToHome = relativeToHome;
		Configuration.basedir = basedir;
		configurationFile = conf;
	}

	/**
	 * Should the configuration be read from and write to a file?
	 * 
	 * @param persistence true to use files, false otherwise
	 */
	public static void setConfigurationPersitance(boolean persistence) {
		Configuration.persistence = persistence;
	}

	public static String getConfigurationFile() {
		return configurationFile;
	}

	private Configuration() throws FileNotFoundException {
		Log4J.startMethod(logger, "Configuration");
		try {
			properties = new Properties();

			if (persistence) {
				InputStream is =  Persistence.get().getInputStream(relativeToHome, basedir, configurationFile);
				properties.load(is);
				is.close();
			}
		} catch (FileNotFoundException e) {
			logger
					.warn("Configuration file not found: " + configurationFile,
							e);
			throw e;
		} catch (IOException e) {
			logger.warn("Error loading Configuration file", e);
			throw new FileNotFoundException(configurationFile);
		} finally {
			Log4J.finishMethod(logger, "Configuration");
		}
	}

	/**
	 * This method returns an instance of Configuration
	 * 
	 * @return a shared instance of Configuration
	 */
	public static Configuration getConfiguration() throws FileNotFoundException {
		Log4J.startMethod(logger, "getConfiguration");
		try {
			if (configuration == null) {
				configuration = new Configuration();
			}
			return configuration;
		} finally {
			Log4J.finishMethod(logger, "getConfiguration");
		}
	}

	/**
	 * This method returns a String with the value of the property.
	 * 
	 * @param property
	 *            the property we want the value
	 * @return a string containing the value of the propierty
	 * @exception PropertyNotFound
	 *                if the property is not found.
	 */
	public String get(String property) throws PropertyNotFoundException {
		Log4J.startMethod(logger, "get");
		try {
			String result = properties.getProperty(property);

			if (result == null) {
				logger.debug("Property [" + property + "] not found");
				throw new PropertyNotFoundException(property);
			}

			logger.debug("Property [" + property + "]=" + result);
			return result;
		} finally {
			Log4J.finishMethod(logger, "get");
		}
	}

	public boolean has(String property) {
		Log4J.startMethod(logger, "has");
		try {
			String result = properties.getProperty(property);

			if (result == null) {
				return false;
			}

			return true;
		} finally {
			Log4J.finishMethod(logger, "has");
		}
	}

	/**
	 * This method set a property with the value we pass as parameter
	 * 
	 * @param property
	 *            the property we want to set the value
	 * @param value
	 *            the value to set
	 */
	public void set(String property, String value) {
		Log4J.startMethod(logger, "set");
		try {
			logger.debug("Property [" + property + "]=" + value);
			properties.put(property, value);

			if (persistence) {
				OutputStream os = Persistence.get().getOutputStream(relativeToHome, basedir, configurationFile);
				properties.store(os, null);
				os.close();
			}
		} catch (FileNotFoundException e) {
			logger.error("Configuration file not found: " + relativeToHome + " " + basedir + " " + configurationFile,
					e);
		} catch (IOException e) {
			logger.error("Error storing Configuration file", e);
		} finally {
			Log4J.finishMethod(logger, "set");
		}
	}

	/**
	 * This method returns an enumeration of the propierties that the file
	 * contains
	 */
	public Enumeration propertyNames() {
		return properties.propertyNames();
	}
}
