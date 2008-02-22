/* $Id: Persistence.java,v 1.9 2008/02/22 10:28:32 arianne_rpg Exp $ */
/***************************************************************************
 *						(C) Copyright 2003 - Marauroa					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package marauroa.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

import org.apache.log4j.Logger;

/**
 * Allows transparent access to files. Subclasses implement Persistence for
 * normal and webstart environment.
 * 
 * @author hendrik
 */
public abstract class Persistence {

	private final static Logger logger = Logger.getLogger(Persistence.class);

	private static Persistence instance = null;

	private static boolean WEB_START_SANDBOX = false;

	/**
	 * Returns the Persistence manager for this environmen
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
					// we use reflection to prevent any runtime dependency on
					// jnlp.jar
					// outside webstart. So we do not have to distribute
					// jnlp.jar
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
	 *             on io error
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
	 *             on io error
	 */
	public abstract OutputStream getOutputStream(boolean relativeToHome, String basedir,
	        String filename) throws IOException;

}
