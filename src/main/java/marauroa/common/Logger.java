/***************************************************************************
 *                   (C) Copyright 2003-2010 - Marauroa                    *
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

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A wrapper around Log4J logger so that it is easy for us to replace it in
 * the future.
 *
 * @author durkham
 *
 */
public class Logger {

	/** The log4j instance */
	org.apache.log4j.Logger _logger;

	Logger(Class<?> arg0) {

		_logger = org.apache.log4j.Logger.getLogger(arg0);
	}

	public synchronized void addAppender(Appender arg0) {

		_logger.addAppender(arg0);
	}

	public void assertLog(boolean arg0, String arg1) {

		_logger.assertLog(arg0, arg1);
	}

	public void callAppenders(LoggingEvent arg0) {

		_logger.callAppenders(arg0);
	}

	public void debug(Object arg0, Throwable arg1) {

		_logger.debug(arg0, arg1);
	}

	public void debug(Object arg0) {

		_logger.debug(arg0);
	}

	public void error(Object arg0, Throwable arg1) {

		_logger.error(arg0, arg1);
	}

	public void error(Object arg0) {

		_logger.error(arg0);
	}

	public void fatal(Object arg0, Throwable arg1) {

		_logger.fatal(arg0, arg1);
	}

	public void fatal(Object arg0) {

		_logger.fatal(arg0);
	}

	public boolean getAdditivity() {

		return _logger.getAdditivity();
	}

	public synchronized Enumeration<?> getAllAppenders() {

		return _logger.getAllAppenders();
	}

	public synchronized Appender getAppender(String arg0) {

		return _logger.getAppender(arg0);
	}

	public Level getEffectiveLevel() {

		return _logger.getEffectiveLevel();
	}

	public LoggerRepository getLoggerRepository() {

		return _logger.getLoggerRepository();
	}

	public ResourceBundle getResourceBundle() {

		return _logger.getResourceBundle();
	}

	public void info(Object arg0, Throwable arg1) {

		_logger.info(arg0, arg1);
	}

	public void info(Object arg0) {

		_logger.info(arg0);
	}

	public boolean isAttached(Appender arg0) {

		return _logger.isAttached(arg0);
	}

	public boolean isDebugEnabled() {

		return _logger.isDebugEnabled();
	}

	public boolean isEnabledFor(Priority arg0) {

		return _logger.isEnabledFor(arg0);
	}

	public boolean isInfoEnabled() {

		return _logger.isInfoEnabled();
	}

	public void l7dlog(Priority arg0, String arg1, Object[] arg2, Throwable arg3) {

		_logger.l7dlog(arg0, arg1, arg2, arg3);
	}

	public void l7dlog(Priority arg0, String arg1, Throwable arg2) {

		_logger.l7dlog(arg0, arg1, arg2);
	}

	public void log(Priority arg0, Object arg1, Throwable arg2) {

		_logger.log(arg0, arg1, arg2);
	}

	public void log(Priority arg0, Object arg1) {

		_logger.log(arg0, arg1);
	}

	public void log(String arg0, Priority arg1, Object arg2, Throwable arg3) {

		_logger.log(arg0, arg1, arg2, arg3);
	}

	public synchronized void removeAllAppenders() {

		_logger.removeAllAppenders();
	}

	public synchronized void removeAppender(Appender arg0) {

		_logger.removeAppender(arg0);
	}

	public synchronized void removeAppender(String arg0) {

		_logger.removeAppender(arg0);
	}

	public void setAdditivity(boolean arg0) {

		_logger.setAdditivity(arg0);
	}

	public void setLevel(Level arg0) {

		_logger.setLevel(arg0);
	}

	public void setResourceBundle(ResourceBundle arg0) {

		_logger.setResourceBundle(arg0);
	}

	public void warn(Object arg0, Throwable arg1) {

		_logger.warn(arg0, arg1);
	}

	public void warn(Object arg0) {

		_logger.warn(arg0);
	}

	static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz);
	}

}
