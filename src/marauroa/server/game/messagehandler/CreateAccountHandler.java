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

import marauroa.common.Configuration;
import marauroa.common.Log4J;
import marauroa.common.game.AccountResult;
import marauroa.common.game.Result;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SCreateAccount;
import marauroa.common.net.message.MessageP2SCreateAccount;
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
		try {
			// the rpMan.createAccount line threw an NPE during a test without the
			// PlayerEntryContainer lock.
			if ((rpMan == null) || (message == null) || (message.getAddress() == null)) {
				logger.error("Unexpected null value in CreateAccountHandler.process: rpMan=" + rpMan + " msg=" + message);
				if (message != null) {
					logger.error("addres=" + message.getAddress());
				}
				return;
			}
			
			String username = null;
			String pasword = null;
			String email = null;
			String address = null;

			if (message instanceof MessageC2SCreateAccount) {
				MessageC2SCreateAccount msg = (MessageC2SCreateAccount) message;
				username = msg.getUsername();
				pasword = msg.getPassword();
				email = msg.getEmail();
				address = msg.getAddress().getHostAddress();
			} else {
				MessageP2SCreateAccount msg = (MessageP2SCreateAccount) message;
				if ((msg.getCredentials() == null) || !(msg.getCredentials().equals(Configuration.getConfiguration().get("proxy_credentials")))) {
					logger.warn("Invalid credentials for proxy method.");
					return;
				}
				username = msg.getUsername();
				pasword = msg.getPassword();
				email = msg.getEmail();
				address = msg.getForwardedFor();
			}

			/*
			 * We request RP Manager to create an account for our player. This
			 * will return a <b>result</b> of the operation.
			 */
			AccountResult val = rpMan.createAccount(username, pasword, email, address);
			Result result = val.getResult();

			if (result == Result.OK_CREATED) {
				/*
				 * If result is OK then the account was created and we notify
				 * player about that.
				 */
				logger.debug("Account (" + username + ") created.");
				MessageS2CCreateAccountACK msgCreateAccountACK = new MessageS2CCreateAccountACK(
						message.getSocketChannel(), val.getUsername());
				msgCreateAccountACK.setProtocolVersion(message.getProtocolVersion());
				netMan.sendMessage(msgCreateAccountACK);
			} else {
				/*
				 * Account creation may also fail. So expose the reasons of the
				 * failure.
				 */
				MessageS2CCreateAccountNACK msgCreateAccountNACK = new MessageS2CCreateAccountNACK(
						message.getSocketChannel(), result);
				msgCreateAccountNACK.setProtocolVersion(message.getProtocolVersion());
				netMan.sendMessage(msgCreateAccountNACK);
			}
		} catch (Exception e) {
			logger.error("Unable to create an account", e);
		}
	}

}
