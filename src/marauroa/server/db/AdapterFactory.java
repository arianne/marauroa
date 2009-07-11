/* $Id: AdapterFactory.java,v 1.3 2009/07/11 14:49:06 nhnb Exp $ */
/***************************************************************************
 *                   (C) Copyright 2007-2009 - Marauroa                    *
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

import java.util.Properties;

import marauroa.server.db.adapter.DatabaseAdapter;
import marauroa.server.db.adapter.MySQLDatabaseAdapter;

/**
 * creates DatabaseAdapters for the configured database system
 *
 * @author hendrik
 */
class AdapterFactory {

	private Properties connInfo;

	/**
	 * creates a new AdapterFactory
	 *
	 * @param connInfo
	 */
	public AdapterFactory(Properties connInfo) {
		this.connInfo = connInfo;
	}

	/**
	 * creates a DatabaseAdapter
	 *
	 * @return DatabaseAdapter for the specified database
	 */
	public DatabaseAdapter create() {
		// TODO: make this configureable
		return new MySQLDatabaseAdapter(connInfo);
	}
}
