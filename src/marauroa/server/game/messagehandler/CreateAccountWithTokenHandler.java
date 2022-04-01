/***************************************************************************
 *                   (C) Copyright 2003-2022 - Marauroa                    *
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
import marauroa.common.net.message.MessageC2SCreateAccountWithToken;
import marauroa.common.net.message.MessageS2CCreateAccountACK;
import marauroa.common.net.message.MessageS2CCreateAccountNACK;

/**
 * This is a create account request.
 * The difference between this class and CreateAccountHandler.class
 * is in that this class is used for cases where we don't use have a password, just a token,
 * for example, google authentication.
 * It just creates an account, it doesn't login us into it.
 *
 * @author maxgmer
 */
class CreateAccountWithTokenHandler extends MessageHandler {
    /** the logger instance. */
    private static final marauroa.common.Logger logger = Log4J.getLogger(CreateAccountWithTokenHandler.class);


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
                logger.error("Unexpected null value in CreateAccountWithTokenHandler.process: rpMan=" + rpMan + " msg=" + message);
                if (message != null) {
                    logger.error("address=" + message.getAddress());
                }
                return;
            }

            String username = null;
            String tokenType = null;
            String token = null;
            String address = null;

            if (message instanceof MessageC2SCreateAccountWithToken) {
                MessageC2SCreateAccountWithToken msg = (MessageC2SCreateAccountWithToken) message;
                username = msg.getUsername();
                tokenType = msg.getTokenType();
                token = msg.getToken();
                address = msg.getAddress().getHostAddress();
            }

            /*
             * We request RP Manager to create an account for our player. This
             * will return a <b>result</b> of the operation.
             */
            AccountResult val = rpMan.createAccountWithToken(username, tokenType, token, address);
            Result result = val.getResult();

            if (result == Result.OK_CREATED) {
                /*
                 * If result is OK then the account was created and we notify
                 * player about that.
                 */
                logger.debug("Account (" + username + ") created.");
                MessageS2CCreateAccountACK msgCreateAccountACK = new MessageS2CCreateAccountACK(
                        message.getChannel(), val.getUsername());
                msgCreateAccountACK.setProtocolVersion(message.getProtocolVersion());
                netMan.sendMessage(msgCreateAccountACK);
            } else {
                /*
                 * Account creation may also fail. So expose the reasons of the
                 * failure.
                 */
                MessageS2CCreateAccountNACK msgCreateAccountNACK = new MessageS2CCreateAccountNACK(
                        message.getChannel(), username, result);
                msgCreateAccountNACK.setProtocolVersion(message.getProtocolVersion());
                netMan.sendMessage(msgCreateAccountNACK);
            }
        } catch (Exception e) {
            logger.error("Unable to create an account", e);
        }
    }

}