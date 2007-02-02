/* $Id: Transaction.java,v 1.2 2007/02/02 19:40:57 arianne_rpg Exp $ */
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
 * This class represents a transaction which can be used to
 * retrieve/store/change in PlayerDatabase. Different PlayerDatabase
 * implementaions may requiere different implementations of this class. This
 * dummy implementation can only be used with MemoryPlayerDatabase(which does
 * not support transactions anyway). JDBCPlayerDatabase needs an instance of
 * JDBCTransaction in order to work properly
 */
public class Transaction {
	/** Starts a transaction 
	 * @throws SQLException */
	public void begin() throws SQLException {
	}

	/**
	 * commits the changes made to backstore.
	 * 
	 * @exception TransactionException
	 *                if the underlaying backstore throws an Exception
	 * @throws SQLException 
	 */
	public void commit() throws SQLException {
	}

	/** Makes previous changes to backstore invalid */
	public void rollback()  throws SQLException {
	}
}
