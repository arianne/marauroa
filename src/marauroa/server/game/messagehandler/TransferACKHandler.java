/* $Id: TransferACKHandler.java,v 1.3 2010/06/22 18:17:00 nhnb Exp $ */
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
import marauroa.common.net.message.Message;
import marauroa.common.net.message.MessageC2STransferACK;
import marauroa.common.net.message.MessageS2CTransfer;
import marauroa.common.net.message.TransferContent;
import marauroa.server.game.GameServerManager;
import marauroa.server.game.container.ClientState;
import marauroa.server.game.container.PlayerEntry;

/**
 * This message is received when client get data at
 * server request and it confirms the data to be sent.
 */
class TransferACKHandler extends MessageHandler {
	/** the logger instance. */
	private static final marauroa.common.Logger logger = Log4J.getLogger(GameServerManager.class);


	
	/**
	 * This message is send from client to server to notify server which of the
	 * proposed transfer has to be done.
	 *
	 * @param message
	 *            the transfer ACK message
	 */
	@Override
	public void process(Message message) {
		MessageC2STransferACK msg = (MessageC2STransferACK) message;
		try {
			int clientid = msg.getClientID();

			PlayerEntry entry = playerContainer.get(clientid);

			// verify event
			if (!isValidEvent(msg, entry, ClientState.GAME_BEGIN)) {
				return;
			}

			/*
			 * Handle Transfer ACK here. We iterate over the contents and send
			 * them to client for those of them which client told us ACK.
			 */
			for (TransferContent content : msg.getContents()) {
				if (content.ack == true) {
					logger.debug("Trying transfer content " + content);

					/*
					 * We get the content from those of that this client are
					 * waiting for being sent to it.
					 */
					TransferContent contentToTransfer = entry.getContent(content.name);
					if (contentToTransfer != null) {
						stats.add("Transfer content", 1);
						stats.add("Tranfer content size", contentToTransfer.data.length);

						logger.debug("Transfering content " + contentToTransfer);

						MessageS2CTransfer msgTransfer = new MessageS2CTransfer(entry.channel,
								contentToTransfer);
						msgTransfer.setClientID(clientid);
						msgTransfer.setProtocolVersion(msg.getProtocolVersion());
						netMan.sendMessage(msgTransfer);
					} else {
						logger.info("CAN'T transfer content (" + content.name
						        + ") because it is null");
					}
				} else {
					stats.add("Transfer content cache", 1);
				}
			}

			/*
			 * We clear the content pending to be sent
			 */
			entry.clearContent();
		} catch (Exception e) {
			logger.error("error while processing TransferACK", e);
		}
	}

}
