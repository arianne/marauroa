/* $Id: Transaction.java,v 1.6 2007/03/11 20:59:20 nhnb Exp $ */
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

import java.sql.SQLException;

/**
 * This class represents a transaction which can be used to retrieve/store/change in Database.
 * <p>
 * Different Database implementaions may requiere different implementations of this class.
 */
public interface Transaction {
	/**
	 * Returns Connection
	 *
	 * @return a Connection
	 */
	public java.sql.Connection getConnection();

	/**
	 * Starts a transaction
	 * @throws SQLException
	 */
	public void begin() throws SQLException;

	/**
	 * commits the changes made to backstore.
	 *
	 * @throws SQLException if the underlaying backstore throws an Exception
	 */
	public void commit() throws SQLException;

	/**
	 * Makes previous changes to backstore invalid
	 *
	 * @throws SQLException if the underlaying backstore throws an Exception
	 */
	public void rollback() throws SQLException;
}
