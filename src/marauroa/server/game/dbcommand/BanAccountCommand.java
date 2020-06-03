/***************************************************************************
 *                   (C) Copyright 2010-2020 - Marauroa                    *
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
import java.sql.Timestamp;

import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.DAORegister;

/**
 * bans an account
 *
 * @author hendrik
 */
public class BanAccountCommand  extends AbstractDBCommand {
	private String username;
	private String reason;
	private Timestamp expire;

	
	/**
	 * creates a BanAccountCommand
	 *
	 * @param username username to ban
	 * @param reason reason for the an
	 * @param expire expire timestamp
	 */
	public BanAccountCommand(String username, String reason, Timestamp expire) {
		this.username = username;
		this.reason = reason;
		this.expire = expire;
	}



	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		DAORegister.get().get(AccountDAO.class).addBan(transaction, username, reason, expire);
	}

}
