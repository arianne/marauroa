/* $Id: MySQLDatabaseAdapter.java,v 1.10 2009/12/29 00:12:48 nhnb Exp $ */
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
package marauroa.server.db.adapter;

import java.util.Properties;

import marauroa.server.db.DatabaseConnectionException;

/**
 * abstracts from MySQL specifica
 *
 * @author hendrik
 */
public class MySQLDatabaseAdapter extends AbstractDatabaseAdapter {

	/**
	 * creates a new MySQLDatabaseAdapter
	 *
	 * @param connInfo parmaters specifying the
	 * @throws DatabaseConnectionException if the connection cannot be established.
	 */
	public MySQLDatabaseAdapter(Properties connInfo) throws DatabaseConnectionException {
		super(connInfo);
	}
}
