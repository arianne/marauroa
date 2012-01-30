/***************************************************************************
 *                   (C) Copyright 2003-2011 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import marauroa.common.Log4J;
import marauroa.common.Logger;


/**
 * Allows transparent access to files. Subclasses implement Persistence for
 * normal and webstart environment.
 * 
 * @author hendrik
 */
public abstract class Persistence {

	private final static Logger logger = Log4J.getLogger(Persistence.class);

	private static Persistence instance = null;

	private static boolean WEB_START_SANDBOX = false;

	/**
	 * Returns the Persistence manager for this environment
	 * 
	 * @return Persistence
	 */
	public static Persistence get() {
		if (instance == null) {
			try {
				System.getProperty("user.home");
			} catch (AccessControlException e) {
				WEB_START_SANDBOX = true;
			}

			if (WEB_START_SANDBOX) {
				try {
					// We use reflection to prevent any runtime dependency on jnlp.jar
					// outside webstart. So we do not have to distribute jnlp.jar.
					Class<?> clazz = Class.forName("marauroa.common.io.WebstartPersistence");
					instance = (Persistence) clazz.newInstance();
				} catch (Exception e) {
					e.printStackTrace(System.err);
					logger.error(e, e);
				}
			} else {
				instance = new FileSystemPersistence();
			}
		}
		return instance;
	}

	/**
	 * Gets an input stream to this "virtual" file
	 * 
	 * @param relativeToHome
	 *            should this file be placed below the users home directory?
	 * @param basedir
	 *            directory prefix which is ignore in webstart environment
	 * @param filename
	 *            filename (without path)
	 * @return InputStream
	 * @throws IOException
	 *             on IO error
	 */
	public abstract InputStream getInputStream(boolean relativeToHome, String basedir,
	        String filename) throws IOException;

	/**
	 * Gets an output stream to this "virtual" file
	 * 
	 * @param relativeToHome
	 *            should this file be placed below the users home directory?
	 * @param basedir
	 *            directory prefix which is ignore in webstart environment
	 * @param filename
	 *            filename (without path)
	 * @return OutputStream
	 * @throws IOException
	 *             on IO error
	 */
	public abstract OutputStream getOutputStream(boolean relativeToHome, String basedir,
	        String filename) throws IOException;

}
