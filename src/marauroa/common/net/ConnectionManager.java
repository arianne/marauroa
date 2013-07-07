/* $Id: IDisconnectedListener.java,v 1.6 2007/04/09 14:40:01 arianne_rpg Exp $ */
/***************************************************************************
 *                      (C) Copyright 2011 - Marauroa                      *
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

	void finish();

	boolean isFinished();

	void send(Object internalChannel, Message msg, boolean isPerceptionRequired);

	void close(Object internalChannel);

	/**
	 * activates ssl
	 *
	 * @param internalChannel intenral channel
	 */
	void activateSsl(Object internalChannel);

}
