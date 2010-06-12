/* $Id: ChangeRequest.java,v 1.8 2010/06/12 15:09:47 nhnb Exp $ */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.net.nio;

import java.nio.channels.SocketChannel;

/**
 * This class notify the NIO Server about a change request on one of the
 * sockets.
 * 
 * @author miguel
 * 
 */
public class ChangeRequest {

	public static final int REGISTER = 1;

	public static final int CHANGEOPS = 2;

	public static final int CLOSE = 3;

	/** Associated socket channel for this request. */
	public SocketChannel socket;

	/** Type of request */
	public int type;

	/** Extra params */
	public int ops;

	/** Constructor */
	public ChangeRequest(SocketChannel socket, int type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}
