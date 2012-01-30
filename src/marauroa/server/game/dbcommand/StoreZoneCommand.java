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
package marauroa.server.game.dbcommand;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.RPZoneDAO;

/**
 * Asynchronously stores a zone.
 *
 * @author hendrik
 */
public class StoreZoneCommand extends AbstractDBCommand {
	private IRPZone zone;
	private List<RPObject> frozenContent;

	/**
	 * Asynchronously stores a zone
	 * 
	 * @param zone  IRPZone
	 * @param frozenContent the content of the 
	 */
	public StoreZoneCommand(IRPZone zone, List<RPObject> frozenContent) {
		this.zone = zone;
		this.frozenContent = frozenContent;
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		DAORegister.get().get(RPZoneDAO.class).storeRPZone(transaction, zone, frozenContent);
	}

	/**
	 * returns a string suitable for debug output of this DBCommand.
	 *
	 * @return debug string
	 */
	@Override
	public String toString() {
		return "StoreZoneCommand [zone=" + zone + ", frozenContent.size()=" + frozenContent.size() + "]";
	}
}
