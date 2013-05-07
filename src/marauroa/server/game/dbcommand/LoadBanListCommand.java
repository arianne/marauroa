/***************************************************************************
 *                     (C) Copyright 2013 - Marauroa                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.dbcommand;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import marauroa.server.db.DBTransaction;
import marauroa.server.game.db.BanListDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.messagehandler.DelayedEventHandler;
import marauroa.server.net.validator.InetAddressMask;

/**
 * asynchronously loads the ip ban list
 *
 * @author hendrik
 */
public class LoadBanListCommand  extends DBCommandWithCallback {
	private List<InetAddressMask> permanentBans;

	/**
	 * Creates a new LoadBanListCommand
	 *
	 * @param callback DelayedEventHandler
	 */
	public LoadBanListCommand(DelayedEventHandler callback) {
		super(callback, 0, null, 0);
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		permanentBans = DAORegister.get().get(BanListDAO.class).getBannedAddresses(transaction);
	}

	/**
	 * Gets the characters map
	 *
	 * @return characters
	 */
	public List<InetAddressMask> getPermanentBans() {
		return new LinkedList<InetAddressMask>(permanentBans);
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "LoadBanListCommand";
	}
}
