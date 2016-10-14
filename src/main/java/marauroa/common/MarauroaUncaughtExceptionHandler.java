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


/**
 * logs uncaught exceptions to the logging system before
 * the exception is propagated to the Java VM which will
 * kill the thread. The Java VM only logs to stderr so
 * there would be no information in the logfile without
 * this class.
 */
public class MarauroaUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static Logger logger = Log4J.getLogger(MarauroaUncaughtExceptionHandler.class);

	private Thread.UncaughtExceptionHandler next;
	private boolean killOnError = false;

	/**
	 * creates a new MarauroaUncaughtExceptionHandler
	 *
	 * @param killOnError should the VM be killed?
	 */
	public MarauroaUncaughtExceptionHandler(boolean killOnError) {
		this.killOnError = killOnError;
	}

	public void uncaughtException(Thread thread, Throwable exception) {
		logger.error("Exception in thread " + thread.getName(), exception);
		System.err.println("Exception in thread " + thread.getName());
		exception.printStackTrace();
		if (next != null) {
			next.uncaughtException(thread, exception);
		}

		// kill the server
		// we did no have a single case where an exception causing a thead to die
		//  did not have serious impact on the server
		if (killOnError) {
			System.exit(1);
		}
	}

	/**
	 * installs this uncaught exception handler
	 *
	 * @param killOnError should the VM be killed?
	 */
	public static void setup(boolean killOnError) {
		MarauroaUncaughtExceptionHandler handler = new MarauroaUncaughtExceptionHandler(killOnError);
		handler.next = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(handler);
	}

}
