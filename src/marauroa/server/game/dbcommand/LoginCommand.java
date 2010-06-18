/* $Id: LoginCommand.java,v 1.3 2010/06/18 16:28:35 nhnb Exp $ */
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
import java.nio.channels.SocketChannel;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.container.PlayerEntry.SecuredLoginInfo;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.LoginEventDAO;
import marauroa.server.game.messagehandler.DelayedEventHandler;
import marauroa.server.game.messagehandler.DelayedEventHandlerThread;


/**
 * verifies the login using the database
 */
public class LoginCommand extends DBCommandWithCallback {
	private SecuredLoginInfo info;
	private MessageS2CLoginNACK.Reasons failReason = null;
	private String failMessage = null;
	private List<String> previousLogins;


	/**
	 * creates a new LoginCommand
	 *
	 * @param info SecuredLoginInfo
	 */
	public LoginCommand(SecuredLoginInfo info) {
		this.info = info;
	}

	/**
	 * creates a new LoginCommand.
	 *
	 * @param info SecuredLoginInfo
	 * @param callback DelayedEventHandler
	 * @param clientid optional parameter available to the callback
	 * @param channel optional parameter available to the callback
	 * @param protocolVersion protocolVersion
	 */
	public LoginCommand(SecuredLoginInfo info, DelayedEventHandler callback, int clientid,
			SocketChannel channel, int protocolVersion) {
		super(callback, clientid, channel, protocolVersion);
		this.info = info;
	}

	@Override
	public void execute(DBTransaction transaction) throws SQLException, IOException {
		if (info.isBlocked()) {
			failReason = MessageS2CLoginNACK.Reasons.TOO_MANY_TRIES;
			callback();
			return;
		}

		if (!info.verify()) {
			if (info.reason == null) {
				info.reason = MessageS2CLoginNACK.Reasons.USERNAME_WRONG;
			}
			failReason = info.reason;
			info.addLoginEvent(info.address, false);
			callback();
			return;
		}

		String accountStatus = info.getStatus();
		if (accountStatus != null) {
			failMessage = accountStatus;
			callback();
			return;
		}

		/* Successful login */
		previousLogins = DAORegister.get().get(LoginEventDAO.class).getLoginEvents(info.username, 1);
		info.addLoginEvent(info.address, true);

		callback();
	}

	private void callback() {
		/* notify callback */
		if (callback != null) {
			DelayedEventHandlerThread.get().addDelayedEvent(callback, this);
		}
	}

	/**
	 * gets the SecuredLoginInfo object
	 *
	 * @return SecuredLoginInfo
	 */
	public SecuredLoginInfo getInfo() {
		return info;
	}

	/**
	 * gets the Reason enum if the login failed
	 * @return MessageS2CLoginNACK.Reasons or <code>null</code> 
	 * in case the login did not fail (was succesful).
	 */
	public MessageS2CLoginNACK.Reasons getFailReason() {
		return failReason;
	}

	/**
	 * gets the message if the login failed
	 *
	 * @return error message or <code>null</code> 
	 * in case the login did not fail (was succesful).
	 */
	public String getFailMessage() {
		return failMessage;
	}

	/**
	 * gets a list of previous logins so that the player can 
	 * notice possible account hacks.
	 *
	 * @return list of last logins
	 */
	public List<String> getPreviousLogins() {
		return new LinkedList<String>(previousLogins);
	}

}