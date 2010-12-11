/* $Id: StoreZoneCommand.java,v 1.1 2010/12/11 08:46:19 nhnb Exp $ */
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

import marauroa.common.game.IRPZone;
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

	/**
	 * Asynchronously stores a zone
	 * 
	 * @param zone  IRPZone
	 */
	public StoreZoneCommand(IRPZone zone) {
		this.zone = zone;
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		DAORegister.get().get(RPZoneDAO.class).storeRPZone(transaction, zone);
	}
}
