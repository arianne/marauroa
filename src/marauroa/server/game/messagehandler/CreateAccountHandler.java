/* $Id: CreateAccountHandler.java,v 1.5 2010/06/29 21:29:21 nhnb Exp $ */
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
package marauroa.server.game.messagehandler;

import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.Result;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SCreateAccount;
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;

/**
 * This is a create account request. It just create the
 * account, it doesn't login us into it.
 */
class CreateAccountHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(CreateAccountHandler.class);


	/**
	 * This message is used to create a game account. It may fail if the player
	 * already exists or if any of the fields are empty. This method does not
	 * make room for the player at the server.
	 *
	 * @param message
	 *            The create account message.
	 */
	@Override
	public void process(Message message) {
		MessageC2SCreateAccount msg = (MessageC2SCreateAccount) message;
		try {
			// the rpMan.createAccount line threw an NPE during a test without the
			// PlayerEntryContainer lock.
			if ((rpMan == null) || (msg == null) || (msg.getAddress() == null)) {
				logger.error("Unexpected null value in CreateAccountHandler.process: rpMan=" + rpMan + " msg=" + msg);
				if (msg != null) {
					logger.error("addres=" + msg.getAddress());
				}
				return;
			}

			/*
			 * We request RP Manager to create an account for our player. This
			 * will return a <b>result</b> of the operation.
			 */
			AccountResult val = rpMan.createAccount(msg.getUsername(), msg.getPassword(), msg.getEmail(), msg.getAddress().getHostAddress());
			Result result = val.getResult();

			if (result == Result.OK_CREATED) {
				/*
				 * If result is OK then the account was created and we notify
				 * player about that.
				 */
				logger.debug("Account (" + msg.getUsername() + ") created.");
				MessageS2CCreateAccountACK msgCreateAccountACK = new MessageS2CCreateAccountACK(msg
				        .getSocketChannel(), val.getUsername());
				msgCreateAccountACK.setProtocolVersion(msg.getProtocolVersion());
				netMan.sendMessage(msgCreateAccountACK);
			} else {
				/*
				 * Account creation may also fail. So expose the reasons of the
				 * failure.
				 */
				MessageS2CCreateAccountNACK msgCreateAccountNACK = new MessageS2CCreateAccountNACK(
				        msg.getSocketChannel(), result);
				msgCreateAccountNACK.setProtocolVersion(msg.getProtocolVersion());
				netMan.sendMessage(msgCreateAccountNACK);
			}
		} catch (Exception e) {
			logger.error("Unable to create an account", e);
		}
	}

}
