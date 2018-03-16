/***************************************************************************
 *                   (C) Copyright 2003-2012 - Marauroa                    *
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
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2SAction;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;

/**
 * Process an action received from client and pass it
 * directly to RP manager.
 */
class ActionHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(ActionHandler.class);


	/**
	 * This method process actions send from client. In fact, the action is
	 * passed to RPManager that will, when the turn arrives, execute it.
	 *
	 * @param message
	 *            the action message
	 */
	@Override
	public void process(Message message) {
		try {
			MessageC2SAction msg = (MessageC2SAction) message;
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			/*
			 * verify event
			 */
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}
			
			/*
			 * Update timeout timestamp on player.
			 */
			entry.update();

			/* Send the action to RP Manager */
			RPAction action = msg.getRPAction();

			/*
			 * NOTE: These are action attributes that are important for RP
			 * functionality. Tag them in such way that it is not possible to
			 * change them on a buggy RP implementation or it will cause
			 * problems at server.
			 */
			RPObject object = entry.object;
			action.put("sourceid", object.get("id"));
			action.put("zoneid", object.get("zoneid"));

			stats.add("Actions added", 1);

			/*
			 * Log the action into statistics system. Or if the action didn't
			 * have type, log it as an invalid action.
			 */
			String type = action.get("type");
			if (type != null) {
				stats.add("Actions " + type, 1);
			} else {
				stats.add("Actions invalid", 1);
			}

			/*
			 * Finally pass the action to the RP Manager
			 */
			rpMan.addRPAction(object, action);
		} catch (Exception e) {
			stats.add("Actions invalid", 1);
			logger.error("error while processing ActionEvent", e);
		}
	}

}
