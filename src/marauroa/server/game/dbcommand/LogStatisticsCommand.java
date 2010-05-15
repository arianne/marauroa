/***************************************************************************
 *                   (C) Copyright 2009-2010 - Marauroa                    *
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

import java.sql.SQLException;

import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.Statistics.Variables;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.StatisticsDAO;


/**
 * logs statistics.
 *
 * @author hendrik
 */
public class LogStatisticsCommand extends AbstractDBCommand {
	private Variables frozenNow;

	/**
	 * creates a new LogStatisticsCommand
	 *
	 * @param now current state of statistic information
	 */
	public LogStatisticsCommand(Variables now) {
		this.frozenNow = (Variables) now.clone();
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException {
		DAORegister.get().get(StatisticsDAO.class).addStatisticsEvent(transaction, frozenNow);
	}

}
