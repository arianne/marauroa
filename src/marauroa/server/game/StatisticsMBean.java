/* $Id: StatisticsMBean.java,v 1.5 2009/07/18 11:20:35 nhnb Exp $ */
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
package marauroa.server.game;

/** statistics interface for the java management bean */
public interface StatisticsMBean {

    /**
     * gets statistics for the specified type
     *
     * @param type name of statistics type to return
     * @return value
     */
	public long get(String type);
}
