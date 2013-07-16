/***************************************************************************
 *                    (C) Copyright 2011-2013 - Marauroa                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.common.net;

import marauroa.common.net.message.Message;

/**
 * a server for a specific protocol
 *
 * @author hendrik
 */
public interface ConnectionManager {

	/**
	 * shuts the server down
	 */
	public void finish();

	/**
	 * was the server shutdown?
	 *
	 * @return true, if the server was shut down; false otherwise
	 */
	boolean isFinished();

	/**
	 * sends a message
	 *
	 * @param internalChannel channel to send the message to
	 * @param msg message to send
	 * @param isPerceptionRequired is the next perception unskipable?
	 */
	void send(Object internalChannel, Message msg, boolean isPerceptionRequired);

	/**
	 * closes a channel
	 *
	 * @param internalChannel channel to close
	 */
	void close(Object internalChannel);

}
