/***************************************************************************
 *                   (C) Copyright 2009-2020 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package marauroa.server.game.rp;

import java.sql.Timestamp;
import java.util.Date;

/**
 * a gameEvent for logging
 */
public class GameEvent {

	private String source;
	private String event;
	private Timestamp timestamp;
	private String[] params;

	/**
	 * create a GameEvent object for logging
	 *
	 * @param source     source of the event (e. g. player name)
	 * @param event      name of event
	 * @param params     parameters
	 */
	public GameEvent(String source, String event, String... params) {
		this(source, event, new Timestamp(new Date().getTime()), params);
	}

	/**
	 * create a GameEvent object for logging
	 *
	 * @param source     source of the event (e. g. player name)
	 * @param event      name of event
	 * @param timestamp  timestamp
	 * @param params     parameters
	 */
	public GameEvent(String source, String event, Timestamp timestamp, String... params) {
		this.source = source;
		this.event = event;
		this.timestamp = timestamp;
		this.params = new String[params.length];
		System.arraycopy(params, 0, this.params, 0, params.length);
	}

	/**
	 * gets the source
	 *
	 * @return source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * gets the event name
	 *
	 * @return event name
	 */
	public String getEvent() {
		return event;
	}

	/**
	 * gets the timestamp
	 *
	 * @return timestamp
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}

	/**
	 * gets the parameters
	 *
	 * @return parameters
	 */
	public String[] getParams() {
		String[] res = new String[this.params.length];
		System.arraycopy(this.params, 0, res, 0, this.params.length);
		return res;
	}
	
}
