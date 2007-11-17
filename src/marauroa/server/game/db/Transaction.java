/* $Id: Transaction.java,v 1.10 2007/11/17 13:01:50 martinfuchs Exp $ */
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

package marauroa.server.game.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class represents a transaction which can be used to
 * retrieve/store/change in Database.
 * <p>
 * Different Database implementations may require different implementations of
 * this class.
 */
public interface Transaction {

	/**
	 * Returns Connection
	 *
	 * @return a Connection
	 */
	public Connection getConnection();

	/**
	 * Starts a transaction
	 *
	 * @throws SQLException
	 */
	public void begin() throws SQLException;

	/**
	 * commits the changes made to backstore.
	 *
	 * @throws SQLException
	 *             if the underlaying backstore throws an Exception
	 */
	public void commit() throws SQLException;

	/**
	 * Makes previous changes to backstore invalid
	 *
	 * @throws SQLException
	 *             if the underlaying backstore throws an Exception
	 */
	public void rollback() throws SQLException;

	/**
	 * Returns a helper object to access the database
	 *
	 * @return Accessor
	 */
	public Accessor getAccessor();
}
