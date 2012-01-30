/***************************************************************************
 *                   (C) Copyright 2007-2012 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.container;

/**
 * This enum describe one of the possible state of the client.
 *
 * When the client starts it has no state, once it connects to server it is
 * assigned CONNECTION_ACCEPTED state until it logins successfully so it is moved
 * to LOGIN_COMPLETE.
 *
 * As soon as it chooses a character it is moved to GAME_BEGIN state which it
 * will only leave when logout is accepted, LOGOUT_ACCEPTED.
 */
public enum ClientState {

	/** Connection is accepted but login stage is not completed. */
	CONNECTION_ACCEPTED,

	/** Login identification is completed but still choosing character- */
	LOGIN_COMPLETE,

	/** Client is already playing. */
	GAME_BEGIN,

	/** The client has requested logout and the petition is accepted */
	LOGOUT_ACCEPTED
}